package service

import model.*
import mu.KotlinLogging
import org.apache.http.HttpStatus
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

private const val CUCUMBER_HOST = "http://wfm-ci.infor.com:8080"
private const val CUCUMBER_HTML_REPORTS = "cucumber-html-reports"
private const val OVERVIEW_FEATURES = "overview-features.html"
private const val TEST_REPORT = "testReport"
private const val SCENARIO = "Scenario"
private const val BACKGROUND = "Background"
private const val XML_BUILD_SEARCH_QUERY = "xml?tree=builds[building,id,displayName,result,duration,timestamp,actions[causes[userId,userName]]]"

class CucumberReportService {

    val logger = KotlinLogging.logger {}

    fun getBuilds(job: String): List<Build> {
        val jobUrl = "$CUCUMBER_HOST/job/${job}"
        val url = "$jobUrl/api/$XML_BUILD_SEARCH_QUERY"

        val xml = Jsoup.connect(url).maxBodySize(0).get()

        return xml.select("build").map { build ->
            val id = build.select("id").text().toInt()
            val name = build.select("displayName").text()
            val result = getBuildResult(build.select("result").text())
            val duration = build.select("duration").text().toLong()
            val timestamp = build.select("timestamp").text().toLong()
            val finished = build.select("building").text().equals("true", true).not()
            val hasReport = build.select("action[_class='hudson.tasks.junit.TestResultAction']").isNotEmpty()

            val (userId, userName) = build.select("action[_class='hudson.model.CauseAction'] > cause")
                .let { it.select("userId").text() to it.select("userName").text() }

            Build(
                url = "$jobUrl/$id",
                id = id,
                name = name,
                result = result,
                duration = duration,
                timestamp = timestamp,
                finished = finished,
                hasReport = hasReport,
                userId = userId,
                userName = userName
            )
        }
    }

    private fun getBuildResult(result: String): Build.Result {
        return when (result) {
            "SUCCESS" -> Build.Result.SUCCESS
            "FAILURE" -> Build.Result.FAILURE
            "UNSTABLE" -> Build.Result.UNSTABLE
            "ABORTED" -> Build.Result.ABORTED
            else -> throw IllegalArgumentException("Invalid result: $result")
        }
    }

    private fun hasReport(link: String): Boolean {
        val response = Jsoup.connect("$link$TEST_REPORT").ignoreHttpErrors(true).execute()
        return response.statusCode() == HttpStatus.SC_OK
    }

    fun getReport(jobName: String, buildId: Int): Report {
        val buildUrl = "$CUCUMBER_HOST/job/$jobName/$buildId"

        val failedScenarioNamesByFeatureName = getFailedScenarioNamesByFeatureName(buildUrl)
        val reportHtmlByFeature = getReportHtmlByFeature(buildUrl)

        val failedFeatures = mutableListOf<Feature>()

        failedScenarioNamesByFeatureName.forEach { (featureName, failedScenarioNames) ->
            if (reportHtmlByFeature.containsKey(featureName)) {
                val reportUrl = "$buildUrl/$CUCUMBER_HTML_REPORTS/${reportHtmlByFeature[featureName]}"
                val failedFeature = getFailedFeature(reportUrl, featureName, failedScenarioNames)
                failedFeatures.add(failedFeature)
            }
        }

        return Report(jobName, buildId, failedFeatures)
    }

    private fun getFailedScenarioNamesByFeatureName(buildUrl: String): Map<String, List<String>> {
        val doc = Jsoup.connect(buildUrl).maxBodySize(0).get()

        val failedTestNames = doc.body().select("a[href='testReport/']").next().select("li > a")
            .map { it.text().trim() }

        val actualFailedTestNames = failedTestNames
            .groupingBy { it }
            .eachCount()
            .filter { it.value >= 2 }
            .keys

        return actualFailedTestNames.groupBy({ it.substringBefore(".") }, { it.substringAfter(".") })
    }

    private fun getReportHtmlByFeature(buildUrl: String): Map<String, String> {
        val response = Jsoup.connect("$buildUrl/$CUCUMBER_HTML_REPORTS/$OVERVIEW_FEATURES")
            .maxBodySize(0)
            .ignoreHttpErrors(true)
            .execute()

        if (response.statusCode() != HttpStatus.SC_OK) return emptyMap()

        val doc = response.parse()
        return doc.body()
            .select("table[id='tablesorter'] > tbody > tr")
            .map { failedFeatureReport ->
                val tagColumn = failedFeatureReport.child(0)
                tagColumn.text() to tagColumn.select("a").attr("href")
            }.toMap()
    }

    private fun getFailedFeature(reportUrl: String, featureName: String, failedScenarioNames: Collection<String>): Feature {

        val doc = Jsoup.connect(reportUrl).maxBodySize(0).get()

        val elements = doc.body().select("div.feature > div.elements > div.element")
        val elementsByKeyword = groupElementsByKeyword(elements)

        val featureTags = doc.body().select("div.feature > div.tags > a").eachText()

        val backgroundSteps =
            if (elementsByKeyword.contains(BACKGROUND)) getSteps(elementsByKeyword.getValue(BACKGROUND).first())
            else emptyList()

        val failedScenarios =
            if (elementsByKeyword.contains(SCENARIO)) getFailedScenarios(elementsByKeyword.getValue(SCENARIO), failedScenarioNames)
            else emptyList()

        return Feature(featureName, featureTags.toSet(), failedScenarios, backgroundSteps)
    }

    private fun groupElementsByKeyword(elements: List<Element>): Map<String, List<Element>> {
        return elements.groupBy { element ->
            element.select("span.collapsable-control > div.brief > span.keyword").text().trim()
        }
    }

    private fun getFailedScenarios(elements: List<Element>, failedScenarioNames: Collection<String>): List<Scenario> {
        val failedScenarioNameSet = failedScenarioNames.toSet()
        val failedScenarios = mutableListOf<Scenario>()

        elements.forEach { element ->
            val scenarioName = element.selectFirst("span.name").text()

            if (failedScenarioNameSet.contains(scenarioName)) {
                val scenarioTags = element.select("div.tags > a").eachText()

                val hooks = getHooks(element)
                val steps = getSteps(element)
                val screenShotLinks = getScreenShotLinks(element)

                val scenario = Scenario(scenarioTags.toSet(), scenarioName, hooks, steps, screenShotLinks)

                failedScenarios.add(scenario)
            }
        }

        return failedScenarios
    }

    private fun getScreenShotLinks(element: Element): List<String> {
        val lastStep = element.select("div.step").last()
        return lastStep.select("div.embeddings div.embedding-content > img").eachAttr("src")
    }

    private fun getHooks(element: Element): List<Hook> {
        return element.select("div.hook").map { hook ->
            val brief = hook.selectFirst("div.brief")

            val type = when (brief.select("keyword").text().trim()) {
                Hook.Type.BEFORE.text -> Hook.Type.BEFORE
                Hook.Type.AFTER.text -> Hook.Type.AFTER
                else -> Hook.Type.UNKNOWN
            }

            val name = brief.select("span.name").text()
            val duration = brief.select("span.duration").text().toLong()
            val result = getResult(brief)

            Hook(type, name, duration, result)
        }
    }

    private fun getSteps(element: Element): List<Step> {
        return element.select("div.step").map { step ->
            val brief = step.selectFirst("div.brief")

            val type = when (brief.select("keyword").text().trim()) {
                Step.Type.GIVEN.text -> Step.Type.GIVEN
                Step.Type.WHEN.text -> Step.Type.WHEN
                Step.Type.AND.text -> Step.Type.AND
                Step.Type.THEN.text -> Step.Type.THEN
                else -> Step.Type.UNKNOWN
            }
            val name = brief.select("span.name").text()
            val duration = brief.select("span.duration").text().toLong()
            val result = getResult(brief)

            val arguments = getStepArguments(step)
            val messages = if (result == Result.FAILED) getStepMessages(step) else emptyList()

            Step(type, name, duration, result, messages, arguments)
        }
    }

    private fun getResult(brief: Element): Result {
        return when {
            brief.hasClass(Result.PASSED.cssClass) -> Result.PASSED
            brief.hasClass(Result.FAILED.cssClass) -> Result.FAILED
            brief.hasClass(Result.UNDEFINED.cssClass) -> Result.UNDEFINED
            brief.hasClass(Result.SKIPPED.cssClass) -> Result.SKIPPED
            else -> Result.UNKNOWN
        }
    }

    private fun getStepMessages(step: Element): List<String> {
        return step.select("div.inner-level > div.message > div[id^='msg'] > pre").eachText()
    }

    private fun getStepArguments(step: Element): List<List<String>> {
        return step.select("table.step-arguments > tbody > tr").map { tr -> tr.select("td").eachText() }
    }

    fun getCucumberJobs(view: View): List<String> {
        val url = "$CUCUMBER_HOST/view/${view.viewName}/api/xml"

        val xml = Jsoup.connect(url).maxBodySize(0).get()

        return xml.select("job")
            .map { it.select("name").text() }
            .filter { it.contains("cucumber", true) }
    }
}

