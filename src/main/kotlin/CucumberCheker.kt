
import org.jsoup.Jsoup

class CucumberChecker(private val resultUrl: String) {

    fun getFailedScenarios(): List<Scenario> {

        val doc = Jsoup.connect("$resultUrl/cucumber-html-reports/overview-features.html").get()
        val featureRows = doc.body().select("table[id='tablesorter'] > tbody > tr")
        val failedFeatureRows = featureRows.filter { it.child(2).text().toInt() > 0 }

        val failedScenariosByFeature = getFailedScnariosByFeature()

        return failedFeatureRows.flatMap {
            val tagColumn = it.child(0)
            val featureName = tagColumn.text()
            val reportHtml = tagColumn.select("a").attr("href")

            getFailedScenarios(featureName, reportHtml, failedScenariosByFeature[featureName]!!)
        }
    }

    private fun getFailedScenarios(featureName: String, reportHtml: String, failedScenarioNames: Collection<String>): List<Scenario> {

        val failedScenarioNameSet = failedScenarioNames.toSet()
        val doc = Jsoup.connect("$resultUrl/cucumber-html-reports/$reportHtml").get()

        val featureTags = doc.body().select("div.feature > div.tags > a").eachText()

        val failedScenarios = mutableListOf<Scenario>()

        doc.body().select("div.feature > div.elements > div.element").forEach { element ->
            val keyword = element.selectFirst("span.keyword").text()
            val isFailed = element.select("div.steps div.brief").hasClass("failed")
            val scenarioName = element.selectFirst("span.name").text()

            if (keyword == "Scenario" && isFailed && failedScenarioNameSet.contains(scenarioName)) {
                val scenarioTags = element.select("div.tags > a").eachText()
                val failedStep = element.select("div.step > div.brief.failed > span.name").text()
                val failedReason = element.select("div.step > div.brief.failed").next().text()

                failedScenarios.add(Scenario(
                    Feature(featureName, featureTags, reportHtml),
                    scenarioTags,
                    scenarioName,
                    failedStep,
                    failedReason
                ))
            }
        }

        return failedScenarios
    }

    private fun getFailedScnariosByFeature(): Map<String, List<String>> {
        val doc = Jsoup.connect(resultUrl).get()

        val failedTestNames = doc.body()
            .select("a[href='testReport/']")
            .next()
            .select("li > a")
            .map { it.text().trim() }

        val actualFailedTestNames = failedTestNames
            .groupBy { it }
            .filter { it.value.size >= 2 }
            .map { it.key }

        return actualFailedTestNames
            .groupBy({ it.substringBefore(".") }, { it.substringAfter(".") })
    }
}

data class Scenario(
    val feature: Feature,
    val tags: List<String>,
    val name: String,
    val failedStep: String,
    val failedReason: String
)

data class Feature(
    val name: String,
    val tags: List<String>,
    val reportHtml: String
)

fun main(args: Array<String>) {
    val sampleUrl = "http://wfm-ci.infor.com:8080/job/ExecuteCucumberRun-Oracle-Parallel/12094"

    val cucumberChecker = CucumberChecker(sampleUrl)

    val failedTests = cucumberChecker.getFailedScenarios()

    println("Number of FailedTests: ${failedTests.size}")
    println(failedTests)
}