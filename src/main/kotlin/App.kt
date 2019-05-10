import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.DefaultHeaders
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import module.mainModule

fun main(args: Array<String>) {
    embeddedServer(
        factory = Netty,
        port = 9001,
        watchPaths = listOf("AppKt"),
        module = Application::mainModule
    ).start()
}
