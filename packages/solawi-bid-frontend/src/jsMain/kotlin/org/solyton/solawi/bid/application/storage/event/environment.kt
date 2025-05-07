package org.solyton.solawi.bid.application.storage.event

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.evoleq.ktorx.result.on
import org.evoleq.math.Writer
import org.evoleq.math.write
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.env.Environment
import org.solyton.solawi.bid.application.data.env.getEnv

fun Storage<Application>.onReadEnvironment(envSet: Boolean) {
    if (!envSet) CoroutineScope(Job()).launch {
        val env = try {
            getEnv()
        } catch (e: Exception) {
            console.error(e.message)
            Environment(
                true,
                "prod",
                backendUrl = "https://bid.solyton.org",
                backendPort = 8080,
                frontendUrl = "https://solyton.org",
                frontendPort = 80
            )
        }
        this@onReadEnvironment * Writer { envi: Environment ->
            { app: Application -> app.copy(environment = envi) }
        }.write(env) on Unit
    }
}
