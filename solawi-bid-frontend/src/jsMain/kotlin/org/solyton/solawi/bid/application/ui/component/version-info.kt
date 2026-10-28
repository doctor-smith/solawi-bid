package org.solyton.solawi.bid.application.ui.component

import androidx.compose.runtime.*
import org.evoleq.compose.attribute.dataId
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.application.data.version.VersionInfo
import org.solyton.solawi.bid.application.data.version.getVersionInfo

@Composable
fun VersionInfo() {
    var versionInfo by remember { mutableStateOf<VersionInfo?>(null) }

    LaunchedEffect(Unit) {
        versionInfo = runCatching {
            getVersionInfo()
        }.getOrNull()
    }

    Div({
        dataId("app.version-info")
        style {
            position(Position.Fixed)
            bottom(12.px)
            right(16.px)
            fontSize(12.px)
            color(Color("#888888"))
        }
    }) {
        versionInfo?.let {
            Text("v${it.version} · ${it.commit.take(7)}")
        }
    }
}
