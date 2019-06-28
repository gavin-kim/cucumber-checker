package model

data class Report(
    val job: Job,
    val buildId: Int,
    val failedFeatures: List<Feature>
)