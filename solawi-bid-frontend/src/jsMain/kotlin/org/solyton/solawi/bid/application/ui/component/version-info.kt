package org.solyton.solawi.bid.application.ui.component

import androidx.compose.runtime.*
import org.evoleq.compose.attribute.dataId
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.application.data.env.*
import org.solyton.solawi.bid.application.data.version.VersionInfo
import org.solyton.solawi.bid.application.data.version.getVersionInfo

@Composable
fun VersionInfo() {
    var versionInfo by remember { mutableStateOf<VersionInfo?>(null) }
    var env by remember { mutableStateOf<Environment?>(null) }

    // TODO get version info of backend and display it here in a nice fashion
    /*
    var backendInfo by remember { mutableStateOf<VersionInfo?>(null) }

    LaunchedEffect(Unit) {
        backendInfo = runCatching {
            getVersionInfo()
        }.getOrNull()
    }

     */
    LaunchedEffect(Unit) {
        versionInfo = runCatching {
            getVersionInfo()
        }.getOrNull()
    }
    LaunchedEffect(Unit) {
        env = runCatching { getEnv() }.getOrNull()
    }

    Div({
        dataId("app.version-info")
        style {
            position(Position.Fixed)
            bottom(12.px)
            right(16.px)
            marginRight(3.percent)
            fontSize(12.px)
            color(Color("#888888"))
        }
    }) {
        val version = versionInfo?.version
        val commit = versionInfo?.commit?.take(7)
        val fullEnv = {e: Environment? -> when{
            e?.isDev() == true -> "DEV"
            e?.isStaging() == true -> "STAGING"
            else -> ""
        }}
        val info = when {
            env.isNonProdEnv() -> "${fullEnv(env)} · $version · $commit"
            else -> "$version · $commit"
        }
        Text(info)
    }
}
