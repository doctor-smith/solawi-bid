package org.evoleq.compose.routing

import androidx.compose.runtime.*
import kotlinx.browser.window
import org.w3c.dom.Location


/**
 * Global state holding the current path of the application
 */
internal val currentPath: MutableState<String> by lazy{ mutableStateOf(window.location.newPath()) }
fun currentPath(): String = currentPath.value
/**
 * Little helper function to render details og the location object
 */
fun Location.newPath() = "${pathname}${search}"//.ifBlank{ "/" }

/**
 * Navigate to a new location
 */
@RoutingDsl
fun navigate(to: String, title: String = "") {
    window.history.pushState(null, title, to)
    /*
    The history API unfortunately provides no callback to listen for
    [window.history.pushState], so we need to notify subscribers when pushing a new path.
     */
    currentPath.value = window.location.newPath()
}

/**
 * Handle route changes
 */
@RoutingDsl
@Composable
@Suppress("FunctionName")
fun Routing(initPath: String,routes: RoutesConfiguration.()->Unit): Routes = with(routing(routes)){
    @Composable
    fun path(): State<String> {
        LaunchedEffect(Unit) {
            window.onpopstate = {
                currentPath.value = window.location.newPath()
                Unit
            }
        }
        return derivedStateOf { currentPath.value.ifBlank { initPath } }
    }

    if(window.location.pathname.isBlank()) {
        navigate(initPath)
    }
    compose(path().value)
    this
}
