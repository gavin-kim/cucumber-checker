package contoller

import CucumberReportLoader
import model.Feature
import model.Job
import model.Scenario
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
class ApiController {

    @RequestMapping("/", produces = ["application/json"])
    fun getCucReport(): Map<Feature, List<Scenario>> {
        val startTime = Date()
        val failedScenarios = CucumberReportLoader().getFailedScenariosByFeature(Job.MANUAL_ORACLE_JOB, 15168)
        val endTime = Date()

        println("started at $startTime")
        println("ended at $endTime")

        return failedScenarios
    }

    @GetMapping("/getCucumberReport")
    @ResponseBody
    fun getCucumberReport(): Map<Feature, List<Scenario>> {
        val startTime = Date()
        val failedScenarios = CucumberReportLoader().getFailedScenariosByFeature(Job.MANUAL_ORACLE_JOB, 15168)
        val endTime = Date()

        println("started at $startTime")
        println("ended at $endTime")

        return failedScenarios
    }
}