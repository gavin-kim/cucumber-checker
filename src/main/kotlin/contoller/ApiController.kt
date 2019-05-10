package contoller

import service.CucumberReportLoader
import model.Feature
import model.Job
import model.Scenario
import java.util.*

class ApiController {

    fun getCucumberReport(): Map<Feature, List<Scenario>> {
        val startTime = Date()
        val failedScenarios = CucumberReportLoader()
            .getFailedScenariosByFeature(Job.MANUAL_ORACLE_JOB, 15168)
        val endTime = Date()

        println("started at $startTime")
        println("ended at $endTime")

        return failedScenarios
    }
}