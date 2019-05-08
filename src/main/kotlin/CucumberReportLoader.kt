
import model.*
import org.apache.http.HttpStatus
import org.jsoup.Jsoup
import java.util.*

private const val CUCUMBER_HOST = "http://wfm-ci.infor.com:8080"
private const val CUCUMBER_HTML_REPORTS = "cucumber-html-reports"
private const val OVERVIEW_FEATURES = "overview-features.html"

class CucumberReportLoader {

    fun getFailedScenariosByFeature(job: Job, buildId: Int): Map<Feature, List<Scenario>> {
        val buildUrl = "$CUCUMBER_HOST/job/${job.jobName}/$buildId"

        val failedScenarioNamesByFeatureName = getFailedScenarioNamesByFeatureName(buildUrl)
        val reportHtmlByFeature = getReportHtmlByFeature(buildUrl)

        val failedScenariosByFeature = mutableMapOf<Feature, List<Scenario>>()

        failedScenarioNamesByFeatureName.forEach { (featureName, failedScenarioNames) ->
            if (reportHtmlByFeature.containsKey(featureName)) {
                val reportUrl = "$buildUrl/$CUCUMBER_HTML_REPORTS/${reportHtmlByFeature[featureName]}"
                val failedScenarios = getFailedScenarios(reportUrl, featureName, failedScenarioNames)

                failedScenariosByFeature[failedScenarios.first().feature] = failedScenarios
            }
        }

        return failedScenariosByFeature
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
            .filter { it.child(2).text().toInt() > 0 }
            .map { failedFeatureReport ->
                val tagColumn = failedFeatureReport.child(0)
                tagColumn.text() to tagColumn.select("a").attr("href")
            }.toMap()
    }

    private fun getFailedScenarios(reportUrl: String, featureName: String, failedScenarioNames: Collection<String>): List<Scenario> {

        val doc = Jsoup.connect(reportUrl).maxBodySize(0).get()

        val featureTags = doc.body().select("div.feature > div.tags > a").eachText()
        val feature = Feature(featureName, featureTags.toSet())

        val failedScenarioNameSet = failedScenarioNames.toSet()
        val failedScenarios = mutableListOf<Scenario>()

        doc.body().select("div.feature > div.elements > div.element").forEach { element ->
            val keyword = element.selectFirst("span.keyword").text()
            val isFailed = element.select("div.steps div.brief").hasClass("failed")
            val scenarioName = element.selectFirst("span.name").text()

            if (keyword == "Scenario" && isFailed && failedScenarioNameSet.contains(scenarioName)) {
                val scenarioTags = element.select("div.tags > a").eachText()
                val failedStep = element.select("div.step > div.brief.failed > span.name").text()
                val failedReason = element.select("div.step > div.brief.failed").next().text()
                val scenario = Scenario(feature, scenarioTags.toSet(), scenarioName, failedStep, failedReason)

                failedScenarios.add(scenario)
            }
        }

        return failedScenarios
    }

    fun getFailedAutoTriggeredBuilds(job: Job): List<AutoTriggerBuild> {
        return TODO()
    }

    private fun getFailedAutoTriggeredBuildIdsAfterLastSuccess(autoTriggeredJobUrl: String): List<Int>  {
        val doc = Jsoup.connect("$autoTriggeredJobUrl/rssFailed").maxBodySize(0).get()
        val lastSuccessfulBuildId = getLastSuccessfulBuildId(autoTriggeredJobUrl)

        return doc.select("entry")
            .map { it.select("id").text().substringAfterLast(":").toInt() }
            .filter { it > lastSuccessfulBuildId }
    }

    private fun getFailedAutoTriggeredBuildChanges(autoTriggeredJobUrl: String, buildId: Int): List<Change> {
        val doc = Jsoup.connect("$autoTriggeredJobUrl/$buildId/api/xml").maxBodySize(0).get()

        return doc.select("changeSet > item").map {
            Change(
                it.select("affectedPath").eachText(),
                it.select("user").text(),
                it.select("revision").text().toLong(),
                Date(it.select("timestamp").text().toLong()),
                it.select("msg").text()
            )
        }
    }

    private fun getLastSuccessfulBuildId(autoTriggeredJobUrl: String): Int {
        val doc = Jsoup.connect("$autoTriggeredJobUrl/lastSuccessfulBuild/api/xml").maxBodySize(0).get()
        return doc.selectFirst("workflowRun > id").text().toInt()
    }
}

/*
fun app.main(args: Array<String>) {

    val cucumberReportLoader = CucumberReportLoader()

    val startTime = Date()
    val failedScenarios = cucumberReportLoader.getFailedScenariosByFeature(Job.MANUAL_ORACLE_JOB, 15168)
    val endTime = Date()

    println("started at $startTime")
    println("ended at $endTime")
    // val failedBuilds = cucumberChecker.getFailedAutoTriggeredBuilds(Job.FULL_QA_REGRESSION_SQL_SERVER)


*/
/*    failedScenarios.forEach {
        println(it.feature)
        println(it.failedReason)
    }*//*

}*/
