package model

enum class Result(val cssClass: String) {
    PASSED("passed"),
    FAILED("failed"),
    UNDEFINED("undefined"),
    SKIPPED("skipped"),
    UNKNOWN("")
}