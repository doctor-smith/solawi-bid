package org.solyton.solawi.bid.module.navbar.effect

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.evoleq.math.on
import org.evoleq.optics.storage.Action
import org.evoleq.optics.storage.Storage
import org.solyton.solawi.bid.module.authentication.data.api.Logout
import org.solyton.solawi.bid.module.navbar.data.navbar.NavBar

@Suppress("FunctionName")
fun TriggerLogoutEffect(storage: Storage<NavBar>, logoutAction: Action<NavBar,Logout, Unit >) = CoroutineScope(Job()).launch {
    trigger(logoutAction) on storage
}
