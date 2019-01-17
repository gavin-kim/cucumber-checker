
import org.apache.http.HttpStatus
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.util.*


class CucumberChecker {

    private val cucumberHost = "http://wfm-ci.infor.com:8080"


    fun getFailedScenarios(job: Job, buildId: Int): List<Scenario> {

        val response = Jsoup.connect("$cucumberHost/job/${job.jobName}/$buildId/cucumber-html-reports/overview-features.html")
            .maxBodySize(0)
            .ignoreHttpErrors(true)
            .execute()

        if (response.statusCode() != HttpStatus.SC_OK) return emptyList()
        val doc = response.parse()

        val failedFeatureReports = doc.body().select("table[id='tablesorter'] > tbody > tr")
            .filter { it.child(2).text().toInt() > 0 }

        val failedScenariosByFeature = getFailedScenariosByFeature(job, buildId)

        return failedFeatureReports.flatMap { failedReport ->
            val tagColumn = failedReport.child(0)
            val featureName = tagColumn.text()
            val reportHtml = tagColumn.select("a").attr("href")

            if (failedScenariosByFeature.containsKey(featureName)) {
                val document = Jsoup.connect("$cucumberHost/job/${job.jobName}/$buildId/cucumber-html-reports/$reportHtml").maxBodySize(0).get()
                val failedFeature = getFeature(document, featureName, reportHtml)

                getFailedScenarios(document, failedFeature, failedScenariosByFeature[featureName]!!)
            } else {
                emptyList()
            }
        }
    }

    private fun getFailedScenariosByFeature(job: Job, buildId: Int): Map<String, List<String>> {

        val doc = Jsoup.connect("$cucumberHost/job/${job.jobName}/$buildId").maxBodySize(0).get()

        val failedTestNames = doc.body().select("a[href='testReport/']").next().select("li > a")
            .map { it.text().trim() }

        val actualFailedTestNames = failedTestNames
            .groupingBy { it }
            .eachCount()
            .filter { it.value >= 2 }
            .keys

        return actualFailedTestNames.groupBy({ it.substringBefore(".") }, { it.substringAfter(".") })
    }

    private fun getFeature(document: Document, featureName: String, reportHtml: String): Feature {
        val featureTags = document.body().select("div.feature > div.tags > a").eachText()
        return Feature(featureName, featureTags.toSet(), reportHtml)
    }

    private fun getFailedScenarios(document: Document, feature: Feature, failedScenarioNames: Collection<String>): List<Scenario> {

        val failedScenarioNameSet = failedScenarioNames.toSet()
        val failedScenarios = mutableListOf<Scenario>()

        document.body().select("div.feature > div.elements > div.element").forEach { element ->
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
        val autoTriggeredJobUrl = "$cucumberHost/job/${job.jobName}"
        val failedAutoTriggeredBuildIds = getFailedAutoTriggeredBuildIdsAfterLastSuccess(autoTriggeredJobUrl)

        return failedAutoTriggeredBuildIds.map { failedAutoTriggeredBuildId ->
            val changes = getFailedAutoTriggeredBuildChanges(autoTriggeredJobUrl, failedAutoTriggeredBuildId)
            val failedScenarios = getFailedScenarios(job, failedAutoTriggeredBuildId)

            AutoTriggerBuild(failedAutoTriggeredBuildId, job, changes, failedScenarios)
        }
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

data class AutoTriggerBuild(
    val id: Int,
    val job: Job,
    val changes: List<Change>,
    val failedScenarios: List<Scenario>
)

data class Change(
    val affectedPath: List<String>,
    val user: String,
    val revision: Long,
    val date: Date,
    val message: String
)

data class Scenario(
    val feature: Feature,
    val tags: Set<String>,
    val name: String,
    val failedStep: String,
    val failedReason: String
)

data class Feature(
    val name: String,
    val tags: Set<String>,
    val reportHtml: String
)

enum class Job(val jobName: String) {
    MANUAL_ORACLE_JOB("ExecuteCucumberRun-Oracle-Parallel"),
    MANUAL_SQL_SERVER_JOB("ExecuteCucumberRun--SQLServer-Sequential"),
    BASIC_STEPS_ASV_SQL_SERVER("rCucumber-BasicSteps-ASV-SQLServer-AutoTriggeredOnly"),
    BASIC_STEPS_MR_OTS_SQL_SERVER("rCucumber-BasicSteps-MROTS-SQLServer-AutoTriggeredOnly"),
    BASIC_STEPS_NON_ASV_SQL_SERVER("rCucumber-BasicSteps-NonASV-SQLServer-AutoTriggeredOnly"),
    FULL_QA_REGRESSION_SQL_SERVER("rCucumber-FullQARegression-SQLServer-AutoTriggeredOnly")
}

fun main(args: Array<String>) {

    val cucumberChecker = CucumberChecker()

    val failedScenarios = cucumberChecker.getFailedScenarios(Job.MANUAL_ORACLE_JOB, 12200)
    val failedBuilds = cucumberChecker.getFailedAutoTriggeredBuilds(Job.FULL_QA_REGRESSION_SQL_SERVER)
}