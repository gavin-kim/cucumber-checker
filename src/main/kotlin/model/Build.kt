package model

data class Build(
    val url: String,
    val id: Int,
    val name: String,
    val result: Result,
    val duration: Long,
    val timestamp: Long,
    val finished: Boolean,
    val hasReport: Boolean,
    val userId: String,
    val userName: String
) {
    enum class Result {
        SUCCESS,
        FAILURE,
        UNSTABLE,
        ABORTED
    }
}