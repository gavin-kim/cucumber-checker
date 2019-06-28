package model

data class Build(
    val url: String,
    val id: Int,
    val name: String,
    val status: Result,
    val duration: Long,
    val timestamp: Long
) {
    enum class Result {
        SUCCESS,
        FAILURE,
        UNSTABLE,
        ABORTED,
    }
}