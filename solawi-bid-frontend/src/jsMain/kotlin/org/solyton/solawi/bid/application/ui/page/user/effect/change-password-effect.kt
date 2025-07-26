package org.solyton.solawi.bid.application.ui.page.user.effect

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.evoleq.math.on
import org.evoleq.optics.storage.Storage
import org.solyton.solawi.bid.module.user.data.Application
import org.solyton.solawi.bid.application.ui.page.user.action.changePassword
import org.solyton.solawi.bid.module.user.data.api.ChangePassword


@Suppress("FunctionName")
fun TriggerPasswordChange(user: ChangePassword, storage: Storage<Application>) = CoroutineScope(Job()).launch {
    val action = changePassword(user)
    trigger(action) on storage
}