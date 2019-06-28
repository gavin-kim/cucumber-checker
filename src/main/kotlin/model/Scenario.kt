package model

data class Scenario(
    val tags: Set<String>,
    val name: String,
    val hooks: List<Hook>,
    val steps: List<Step>,
    val screenShotFiles: List<String>
)