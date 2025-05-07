package org.solyton.solawi.bid.application.ui.page.login.effect

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
import org.solyton.solawi.bid.application.ui.page.login.action.isLoggedInAction

@Markup
@Composable
@Suppress("FunctionName")
fun LaunchIsLoggedInEffect(storage: Storage<Application>) {
    LaunchedEffect(Unit) {
        launch {
            (storage * actions).read().emit(isLoggedInAction)
        }
    }
}

@Suppress("FunctionName")
fun TriggerIsLoggedInEffect(storage: Storage<Application>) {
    CoroutineScope(Job()).
        launch {
            (storage * actions).read().emit(isLoggedInAction)
        }
    }
