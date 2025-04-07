package chic.august

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    install(plugin = StatusPages) {
        exception<IllegalStateException> { call, cause ->
            call.respondText("App in illegal state as ${cause.message}")
        }
        status(HttpStatusCode.NotFound) { call, status ->
            call.respondText("${status.value} not found")
        }
    }
    routing {
        // static resources 추가 후 rebuild 해주어야 반영됨
        staticResources("/content", "mycontent")
        get("/") {
            // throw IllegalStateException("BOOO") -> 등록된 에러 핸들러가 실행된다
            call.respondText("Hello World!")
        }
        get("/test1") {
            val text = "<h1>Hello From Ktor</h1>"
            val type = ContentType.parse("text/html")
            call.respondText(text, type)
        }
        get("/test2") {
            val text = "<a href='https://www.naver.com'>NAVER</a>"
            val type = ContentType.parse("text/html")
            call.respondText(text, type)
        }
    }
}
