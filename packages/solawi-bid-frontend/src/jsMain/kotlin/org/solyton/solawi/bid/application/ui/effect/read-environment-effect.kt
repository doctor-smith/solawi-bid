package org.solyton.solawi.bid.application.ui.effect

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.launch
import org.evoleq.compose.Markup
import org.evoleq.ktorx.result.on
import org.evoleq.math.Writer
import org.evoleq.math.write
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.env.Environment
import org.solyton.solawi.bid.application.data.env.getEnv

@Markup
@Composable
@Suppress("FunctionName")
fun LaunchReadEnvironmentEffect(storage: Storage<Application>) = LaunchedEffect(Unit) { launch {
    val env = try {
        getEnv().copy(set = true)
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
    (storage * Writer { envi: Environment ->
        { app: Application -> app.copy(environment = envi) }
    }).write(env) on Unit
} }