package org.solyton.solawi.bid.module.health.routing

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import org.evoleq.exposedx.data.DbEnv
import org.evoleq.ktorx.data.KTorEnv
import org.solyton.solawi.bid.module.health.action.checkApplicationHealth
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

        // Endpoint to simulate crash
        get("/crash") {
            call.respondText("Application is shutting down...", status = HttpStatusCode.InternalServerError)
            exitProcess(1) // Simulate a crash by exiting the JVM
        }
    }
}
