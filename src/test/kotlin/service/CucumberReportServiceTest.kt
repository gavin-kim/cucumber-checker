package service

import model.Job
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CucumberReportServiceTest {

    private val service = CucumberReportService()

    @Test
    fun `getBuilds`() {
        val buildUrls = service.getBuilds(Job.MANUAL_ORACLE_JOB)
        println(buildUrls)
    }

    @Test
    fun `getBuildsUsingRssOnly`() {
        val buildUrls = service.getBuildsUsingRssOnly(Job.MANUAL_ORACLE_JOB)
        println(buildUrls)
    }
}
