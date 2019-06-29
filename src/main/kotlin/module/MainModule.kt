package module

import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.http.content.*
import io.ktor.jackson.jackson
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.route
import model.View
import service.CucumberReportService
import java.io.File

fun Application.mainModule() {
    install(DefaultHeaders)
    install(CallLogging)
    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
            writerWithDefaultPrettyPrinter()
        }
    }
    install(Routing) {
        static()
        api()
    }
}

fun Route.static() {
    static {
        staticRootFolder = File("src/main/static")
        static("css") {
            files("css")
        }
        static("js") {
            files("js")
        }
        default("index.html")
    }
}

fun Route.resources() {

}

fun Route.api() {
    route("/api") {
        get("/cucumberReport/job/{job}/buildId/{buildId}") {
            val job = call.parameters["job"]
            val buildId = call.parameters["buildId"]?.toIntOrNull()

            if (job.isNullOrBlank() || buildId == null) {
                call.respondText("Invalid parameters. job: $job, buildId: $buildId")
                return@get
            }

            val report = CucumberReportService().getReport(job, buildId)
            call.respond(report)
        }

        get("/cucumberJobs") {
            val jobsByView = View.values()
                .map { view -> view.viewName to CucumberReportService().getCucumberJobs(view) }
                .toMap()

            call.respond(jobsByView)
        }
    }
}