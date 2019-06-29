package model

data class Report(
    val jobName: String,
    val buildId: Int,
    val failedFeatures: List<Feature>
)