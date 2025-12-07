package org.solyton.solawi.bid.application.storage.event

import kotlinx.browser.window
import org.evoleq.compose.routing.navigate
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.context
import org.solyton.solawi.bid.module.context.data.current
import org.solyton.solawi.bid.module.localstorage.api.read
import org.solyton.solawi.bid.module.localstorage.api.write
import org.w3c.dom.AddEventListenerOptions

private const val CONTEXT_ID = "context_id"

fun Storage<Application>.checkContext(){
    val storedContextId: String? = read(CONTEXT_ID)
    when(storedContextId) {
        null -> navigate("/app/dashboard")
        else -> (this * context * current).write(storedContextId)
    }
    window.addEventListener(
        type = "pagehide",
        callback = { write(CONTEXT_ID, (this * context * current).read() )},
        options =  AddEventListenerOptions(once = true)
    )
}
