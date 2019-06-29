package model

data class Hook(
    val type: Type,
    val name: String,
    val duration: Long,
    val result: Result,
    val message: String? = null
) {
    enum class Type(val text: String) {
        BEFORE("Before"),
        AFTER("After"),
        UNKNOWN("")
    }
}