package service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import model.View
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

private const val TEST_JOB = "ExecuteCucumberRun-Oracle-Parallel"

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CucumberReportServiceTest {

    private val service = CucumberReportService()
    private val mapper = ObjectMapper().registerKotlinModule().writerWithDefaultPrettyPrinter()

    @Test
    fun `getBuilds`() {
        val builds = service.getBuilds(TEST_JOB)
        prettyPrint(builds)
    }

    @Test
    fun `getBuilds - NoReport`() {
        val builds = service.getBuilds(TEST_JOB)
        val noReportBuilds = builds.filter { it.finished && !it.hasReport }

        prettyPrint(noReportBuilds)
    }

    @Test
    fun `getCucumberJobs - manual trunk`() {
        val jobs = service.getCucumberJobs(View.MANUAL_VALIDATION_ON_TRUNK)
        prettyPrint(jobs)
    }

    @Test
    fun `getCucumberJobs - manual maintenance`() {
        val jobs = service.getCucumberJobs(View.MANUAL_VALIDATION_ON_MAINT)
        prettyPrint(jobs)
    }

    @Test
    fun `getCucumberJobs - auto triggers`() {
        val jobs = service.getCucumberJobs(View.CUCUMBER_UI_AUTOMATION)
        prettyPrint(jobs)
    }

    private fun prettyPrint(any: Any) {
        println(mapper.writeValueAsString(any))
    }
}
