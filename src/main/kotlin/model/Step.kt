package model

data class Step(
    val type: Type,
    val name: String,
    val duration: Long,
    val result: Result,
    val messages: List<String>,
    val arguments: List<List<String>>
) {
    enum class Type(val text: String) {
        GIVEN("Given"),
        AND("And"),
        WHEN("When"),
        THEN("Then"),
        UNKNOWN("")
    }
}