package module

import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.http.content.resource
import io.ktor.http.content.static
import io.ktor.jackson.jackson
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.route
import model.Job
import service.CucumberReportLoader

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

fun Routing.static() {
    static {
        resource("/", "public/index.html")
        resource("*", "public/index.html")
    }
}

fun Routing.api() {
    route("/api") {
        get("/cucumberReport") {
            val jobId = call.parameters["jobId"]?.toIntOrNull()
            val buildId = call.parameters["buildId"]?.toIntOrNull()

            if (jobId == null || buildId == null) {
                call.respondText("Invalid parameters")
            } else {
                val job = Job.values().find { it.id == jobId }!!
                val report = CucumberReportLoader().getFailedScenariosByFeature(job, buildId)
                call.respond(report)
            }
        }
    }
}