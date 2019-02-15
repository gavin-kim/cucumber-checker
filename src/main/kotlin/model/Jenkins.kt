package model

import java.util.*

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