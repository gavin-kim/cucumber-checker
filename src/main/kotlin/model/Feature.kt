package model

data class Feature(
    val name: String,
    val tags: Set<String>,
    val failedScenarios: List<Scenario>,
    val backgroundSteps: List<Step>
)