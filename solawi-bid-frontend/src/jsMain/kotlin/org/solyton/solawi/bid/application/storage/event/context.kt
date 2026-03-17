package org.solyton.solawi.bid.application.storage.event

import kotlinx.browser.window
import org.evoleq.compose.routing.currentPath
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.context
import org.solyton.solawi.bid.application.service.getContextByName
import org.solyton.solawi.bid.module.application.permission.Context
import org.solyton.solawi.bid.module.context.data.current
import org.solyton.solawi.bid.module.localstorage.api.read
import org.solyton.solawi.bid.module.localstorage.api.write
import org.w3c.dom.AddEventListenerOptions

private const val CONTEXT_ID = "context_id"

private val publicRoutes = listOf("/login","/bid", "/manual")
fun String.isPublicRoute() = publicRoutes.any { this.startsWith(it) }

/**
 * Holds a callback function that is invoked when the pagehide event is triggered.
 * The callback function is optional and takes a dynamically typed parameter.
 * Typically used for handling cleanup or persistence operations before the page is unloaded.
 */
private var pagehideCallback: ((dynamic) -> Unit)? = null

/**
 * Validates and synchronizes the current application context within the storage system.
 *
 * The method performs the following actions:
 * - Reads the current context ID from the storage to determine if it matches the current application state.
 * - Checks if the current path is part of the public routes. If true, no further action is taken.
 * - If no context ID is stored, it redirects the user to the "/app/dashboard" route.
 * - Synchronizes the current context ID to ensure it matches the stored state in the application storage.
 * - Manages the `pagehide` event listener to preserve the current context ID during a page unload.
 *
 * This method ensures that the context remains consistent and redirects users appropriately
 * if the context information is missing or mismatched.
 */
fun Storage<Application>.checkContext() {
    val storedContextId: String? = read(CONTEXT_ID)
    val currentPath = currentPath()

    when {
        currentPath.isPublicRoute() -> return
        storedContextId == null -> with(getContextByName(Context.Application.value)) {
            if(this != null) {
                write(CONTEXT_ID, contextId)
                (this@checkContext * context * current).write(contextId)
            }
            return
        }
        else -> (this * context * current).write(storedContextId)
    }

    // remove previous listener (if any)
    pagehideCallback?.let { window.removeEventListener("pagehide", it) }

    // add current listener
    val cb: (dynamic) -> Unit = {
        write(CONTEXT_ID, (this * context * current).read())
    }
    pagehideCallback = cb
    window.addEventListener("pagehide", cb, AddEventListenerOptions(once = true))
}
