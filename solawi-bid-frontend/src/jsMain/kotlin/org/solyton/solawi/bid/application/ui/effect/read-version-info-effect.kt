package org.solyton.solawi.bid.application.ui.effect

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import org.evoleq.optics.storage.Storage
import org.solyton.solawi.bid.application.data.Application

@Composable
@Suppress("FunctionName", "UnusedParameter")
fun LaunchReadVersionInfoEffect(storage: Storage<Application>) {
    LaunchedEffect(Unit) {
        // val frontendVersionInfo = getVersionInfo()
        // (storage * versionInfo * frontend).write(frontendVersionInfo)

        /*
        launch{

            try{(storage * actions).read().emit ( readBackendVersionInfo())}
            catch (error: Exception){console.error(error)}


            // TODO replace by real info - only for test purposes!!!
            // val frontendVersionInfo = getVersionInfo()
            // (storage * versionInfo * backend).write(frontendVersionInfo)
        }

         */
    }
}
