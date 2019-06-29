import io.ktor.application.Application

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
