package org.solyton.solawi.bid.module.health.routing

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import org.evoleq.exposedx.data.DbEnv
import org.evoleq.ktorx.data.KTorEnv
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.solyton.solawi.bid.module.health.action.checkApplicationHealth
import kotlin.coroutines.CoroutineContext
import kotlin.system.exitProcess

@KtorDsl
@Suppress("UNUSED_PARAMETER")
fun <HealthEnv> Routing.health(
    environment: HealthEnv

) where HealthEnv : KTorEnv, HealthEnv : DbEnv {
    route("/health") {
        get("/check") {
            if (checkApplicationHealth()) {
                call.respondText("Application is healthy", status = HttpStatusCode.OK)
            } else {
                call.respondText("Application is unhealthy", status = HttpStatusCode.InternalServerError)
            }
        }
        get("/ready") {
            try {
                val database = environment.connectToDatabase()
                // simple DB-Check
                newSuspendedTransaction(coroutineContext, database) {
                    val ready = exec("SELECT 1") { rs ->
                        rs.next()
                    }
                    when(ready) {
                        true -> call.respondText("Application is ready", status = HttpStatusCode.OK)
                        false, null -> call.respondText("Application is not ready", status = HttpStatusCode.ServiceUnavailable)
                    }
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.ServiceUnavailable)
            }
        }
        get("/live") {
            call.respondText("Application is live", status = HttpStatusCode.OK)
        }
    }
}
