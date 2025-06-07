package org.solyton.solawi.bid.application.ui.page.user.effect

import org.evoleq.optics.storage.Action
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.module.user.data.Application
import org.solyton.solawi.bid.module.user.data.actions
import org.solyton.solawi.bid.application.data.device.mediaType
import org.solyton.solawi.bid.module.user.data.deviceData
import org.solyton.solawi.bid.module.user.data.modals
import org.solyton.solawi.bid.module.error.component.showErrorModal
import org.solyton.solawi.bid.module.error.lang.errorModalTexts

suspend fun < I : Any,O: Any> Storage<Application>.trigger(action: Action<Application, I, O>) {
    val actions = (this * actions).read()
    try {
        actions.dispatch( action )
    } catch(exception: Exception) {
        (this * modals).showErrorModal(
            errorModalTexts(exception.message?:exception.cause?.message?:"Cannot Emit action '${action.name}'"),
            this * deviceData * mediaType.get
        )
    }
}

fun < I : Any,O: Any> trigger(action: Action<Application, I, O>): suspend (Storage<Application>)->Unit = {
        storage -> storage.trigger(action)
}