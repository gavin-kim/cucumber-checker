package model

enum class View(val viewName: String) {
    MANUAL_VALIDATION_ON_TRUNK("0. Manual Validation on Trunk"),
    MANUAL_VALIDATION_ON_MAINT("00. Manual Validation on Maint"),
    CUCUMBER_UI_AUTOMATION("3. Cucumber UI Automation")
}