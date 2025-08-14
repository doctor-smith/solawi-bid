package org.solyton.solawi.bid.module.navbar.effect


import org.evoleq.device.data.mediaType
import org.evoleq.optics.storage.Action
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.module.error.component.showErrorModal
import org.solyton.solawi.bid.module.error.lang.errorModalTexts
import org.solyton.solawi.bid.module.navbar.data.navbar.NavBar
import org.solyton.solawi.bid.module.navbar.data.navbar.actions
import org.solyton.solawi.bid.module.navbar.data.navbar.deviceData
import org.solyton.solawi.bid.module.navbar.data.navbar.modals

suspend fun < I : Any,O: Any> Storage<NavBar>.trigger(action: Action<NavBar, I, O>) {
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

fun < I : Any,O: Any> trigger(action: Action<NavBar, I, O>): suspend (Storage<NavBar>)->Unit = {
        storage -> storage.trigger(action)
}
