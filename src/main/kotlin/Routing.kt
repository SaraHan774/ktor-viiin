package chic.august

import chic.august.model.Priority
import chic.august.model.Task
import io.ktor.http.*
import io.ktor.serialization.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import model.TaskRepository

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
        staticResources("static", "static")

        route("/tasks") {
            get {
                try {
                    val tasks = TaskRepository.allTasks()
                    call.respond(tasks)
                } catch (e: Exception) {
                    println(e.message)
                }
            }

            get("/byName/{taskName}") {
                val name = call.parameters["taskName"]
                if (name == null) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }

                val task = TaskRepository.taskByName(name)
                if (task == null) {
                    call.respond(HttpStatusCode.NotFound)
                    return@get
                }
                call.respond(task)
            }

            get("/byPriority/{priority}") {
                val priorityAsText = call.parameters["priority"]
                if (priorityAsText == null) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }
                try {
                    val priority = Priority.valueOf(priorityAsText)
                    val tasks = TaskRepository.tasksByPriority(priority)
                    if (tasks.isEmpty()) {
                        // FIXME : message 를 넣지 않으면 response.status 에서 200 을 리턴하는 이유는 ...?
                        call.respond(HttpStatusCode.NotFound, "$priorityAsText Not Found!!!")
                        return@get
                    }
                    call.respond(tasks)
                } catch (ex: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }

            post {
                try {
                    val task = call.receive<Task>()
                    TaskRepository.addTask(task)
                    call.respond(HttpStatusCode.Created)
                } catch (ex: IllegalStateException) {
                    call.respond(HttpStatusCode.BadRequest)
                } catch (ex: JsonConvertException) {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }

            delete("/{taskName}") {
                val name = call.parameters["taskName"]
                if (name == null) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@delete
                }

                if (TaskRepository.removeTask(name)) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
        }
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
