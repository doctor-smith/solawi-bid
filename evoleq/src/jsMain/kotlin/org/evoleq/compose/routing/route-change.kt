package org.evoleq.compose.routing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect


@Composable
@Suppress("FunctionName")
fun RouteChangeEffect(onRouteChanged: (String) -> Unit) {
    val path = currentPath()

    LaunchedEffect(path) {
        console.log("Route changed to $path")
        onRouteChanged(path)
    }
}
