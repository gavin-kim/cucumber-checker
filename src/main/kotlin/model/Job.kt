package model

enum class Job(val id: Int, val jobName: String) {
    MANUAL_ORACLE_JOB(1, "ExecuteCucumberRun-Oracle-Parallel"),
    MANUAL_SQL_SERVER_JOB(2, "ExecuteCucumberRun--SQLServer-Sequential"),
    BASIC_STEPS_ASV_SQL_SERVER(3, "rCucumber-BasicSteps-ASV-SQLServer-AutoTriggeredOnly"),
    BASIC_STEPS_MR_OTS_SQL_SERVER(4, "rCucumber-BasicSteps-MROTS-SQLServer-AutoTriggeredOnly"),
    BASIC_STEPS_NON_ASV_SQL_SERVER(5, "rCucumber-BasicSteps-NonASV-SQLServer-AutoTriggeredOnly"),
    FULL_QA_REGRESSION_SQL_SERVER(6, "rCucumber-FullQARegression-SQLServer-AutoTriggeredOnly")
}