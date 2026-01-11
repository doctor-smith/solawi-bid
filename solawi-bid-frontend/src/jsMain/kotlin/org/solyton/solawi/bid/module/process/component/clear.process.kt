package org.solyton.solawi.bid.module.process.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import kotlinx.browser.window
import org.evoleq.math.dispatch
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.module.process.data.processes.Processes
import org.solyton.solawi.bid.module.process.data.processes.UnRegisterAllOf
import org.w3c.dom.events.Event

/**
 * Clears registered processes for specific `processIds` when a route change or page unload occurs.
 * This function ensures that processes are unregistered on navigation events, such as page reloads,
 * navigation away, or browser tab closing to maintain a clean state.
 *
 * @param processes A storage object containing all registered processes.
 * @param processIds A list of string identifiers representing the processes to be cleared on route change.
 * @param content A composable lambda that represents the UI content to be rendered.
 */
@Composable
@Suppress("FunctionName")
fun ClearProcessOnRouteChange(
    processes: Storage<Processes>,
    processIds: List<String>,
    content: @Composable () -> Unit
) {
    DisposableEffect(Unit) {
        val handler: (Event) -> Unit = {
            processes * UnRegisterAllOf dispatch processIds
        }

        // Triggered frequently on reload/navigation away/tab closing
        window.addEventListener("beforeunload", handler)
        // More reliable than beforeunload on Mobile/Safari
        window.addEventListener("pagehide", handler)

        onDispose {
            window.removeEventListener("beforeunload", handler)
            window.removeEventListener("pagehide", handler)
            handler(Event(""))
        }
    }
    content()
}

/**
 * Clears specified processes when the route changes or the browser triggers events such as page reload,
 * navigation away, or tab close. This function ensures that the state is managed cleanly by unregistering
 * processes associated with certain identifiers during these events.
 *
 * @param processes The storage container holding all registered processes.
 * @param processIds One or more identifiers of the processes to be cleared on route change.
 * @param content The composable lambda function representing the UI content to be displayed.
 */
@Composable
@Suppress("FunctionName")
fun ClearProcessOnRouteChange(processes: Storage<Processes>, vararg processIds: String, content:@Composable () -> Unit) = ClearProcessOnRouteChange(processes, processIds.toList(), content)
