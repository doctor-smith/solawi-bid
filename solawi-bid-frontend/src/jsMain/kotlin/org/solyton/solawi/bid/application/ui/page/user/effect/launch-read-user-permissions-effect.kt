package org.solyton.solawi.bid.application.ui.page.user.effect

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.evoleq.compose.Markup
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.actions
import org.solyton.solawi.bid.application.ui.page.user.action.readUserPermissionsAction

@Markup
@Composable
@Suppress("FunctionName")
fun LaunchReadUserPermissionsEffect(storage: Storage<Application>) {
    LaunchedEffect(Unit) {
        (storage * actions).read().emit(readUserPermissionsAction())
    }
}
@Markup
@Suppress("FunctionName")
fun TriggerReadUserPermissions(storage: Storage<Application>) = CoroutineScope(Job()).launch {
    (storage * actions).read().emit(readUserPermissionsAction())
}
