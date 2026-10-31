package org.solyton.solawi.bid.application.ui.component

import androidx.compose.runtime.*
import kotlinx.browser.window
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.evoleq.compose.attribute.dataId
import org.evoleq.math.Source
import org.evoleq.math.emit
import org.evoleq.math.map
import org.evoleq.math.times
import org.evoleq.optics.storage.Read
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.application.action.readBackendVersionInfo
import org.solyton.solawi.bid.application.data.*
import org.solyton.solawi.bid.application.data.env.isDev
import org.solyton.solawi.bid.application.data.env.isNonProdEnv
import org.solyton.solawi.bid.application.data.env.isStaging
import org.solyton.solawi.bid.application.data.version.VersionInfo
import org.solyton.solawi.bid.application.data.version.commit
import org.solyton.solawi.bid.application.data.version.full.backend
import org.solyton.solawi.bid.application.data.version.full.frontend
import org.solyton.solawi.bid.application.data.version.getVersionInfo
import org.solyton.solawi.bid.application.data.version.version
import org.solyton.solawi.bid.module.style.overflow.Overflow
import org.solyton.solawi.bid.module.style.overflow.overflow
import org.solyton.solawi.bid.module.user.data.user.username
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun VersionInfo(storage: Storage<Application>) {
    val scope = rememberCoroutineScope()

    val frontendInfo = Read(storage * versionInfo * frontend)
    val backendInfo = Read(storage * versionInfo * backend)
    val environment = Read(storage * environment)
    val username = Read(storage * userData * username)

    LaunchedEffect(Unit) {
        scope.launch {
            (storage * actions ).read().emit( readBackendVersionInfo())
        }
    }
    LaunchedEffect(Unit) {

        val frontendVersionInfo = getVersionInfo()
        println("fe-version-info: $frontendVersionInfo")
        (storage * versionInfo * frontend).write(frontendVersionInfo)

    }

    var opened by remember { mutableStateOf(false) }

    val env = Source {
        when {
            environment.emit().isDev() -> "DEV"
            environment.emit().isStaging() -> "STAGING"
            else -> "PROD"
        }
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
            property("z-index", "1000")
        }
    }) {

        // Version label
        Div({
            onClick { opened = !opened }
            style {
                property("cursor", "pointer")
                property("user-select", "none")
            }
        }) {
            val info = Source{
                if (environment.emit().isNonProdEnv()) {
                    "${env.emit()} · ${frontendInfo.emit().version} · ${frontendInfo.emit().commit.take(7)}"
                } else {
                    "${frontendInfo.emit().version} · ${frontendInfo.emit().commit.take(7)}"
                }
            }

            Text(info.emit())
        }

        // Overlay
        if (opened) {
            Div({
                style {
                    position(Position.Absolute)
                    bottom(28.px)
                    right(0.px)

                    width(260.px)
                    padding(16.px)

                    backgroundColor(Color("rgba(255, 255, 255, 0.92)"))
                    property("backdrop-filter", "blur(8px)")
                    property("-webkit-backdrop-filter", "blur(8px)")

                    color(Color("#666666"))

                    borderRadius(8.px)
                    property("border", "1px solid rgba(0, 0, 0, 0.08)")
                    property("box-shadow", "0 6px 24px rgba(0, 0, 0, 0.08)")
                    property("z-index", "1001")
                }
            }) {
                var copied by remember { mutableStateOf(false) }

                LaunchedEffect(copied) {
                    if (copied) {
                        delay(2000.milliseconds)
                        copied = false
                    }
                }
                val clipboardText = Source {
                        """
                        Environment: ${env.emit()}
                        Frontend:
                          Version: ${frontendInfo.emit<VersionInfo>().version}
                          Commit: ${frontendInfo.emit<VersionInfo>().commit}
                        Backend:
                          Version: ${backendInfo.emit<VersionInfo>().version}
                          Commit: ${backendInfo.emit<VersionInfo>().commit}
                    """.trimIndent()
                }

                // Header
                Div({
                    style {
                        display(DisplayStyle.Flex)
                        justifyContent(JustifyContent.SpaceBetween)
                        alignItems(AlignItems.Center)
                        marginBottom(14.px)
                    }
                }) {
                    Span({
                        style {
                            fontSize(13.px)
                            fontWeight("600")
                            color(Color("#374151"))
                            property("line-height", "20px")
                        }
                    }) {
                        Text("Version Info")
                    }

                    Div({
                        style {
                            display(DisplayStyle.Flex)
                            alignItems(AlignItems.Center)
                            property("gap", "12px")
                        }
                    }) {
                        Span({
                            attr("title", if (copied) "Copied!" else "Copy version info")

                            onClick {
                                window.navigator.clipboard.writeText(clipboardText.emit())
                                    .then {
                                        copied = true
                                    }
                            }

                            style {
                                property("cursor", "pointer")
                                property("user-select", "none")
                                color(Color(if (copied) "#4CAF50" else "#888888"))
                                fontSize(14.px)
                                property("transition", "color 0.2s ease")
                            }
                        }) {
                            Text(if (copied) "✓" else "⧉")
                        }

                        Span({
                            onClick { opened = false }
                            style {
                                property("cursor", "pointer")
                                color(Color("#777777"))
                                fontSize(16.px)
                            }
                        }) {
                            Text("×")
                        }
                    }
                }

                // Environment
                InfoRow("Environment", env)
                InfoRow("Username", username)

                Div({
                    style {
                        property("border-top", "1px solid #EEEEEE")
                        marginTop(12.px)
                        marginBottom(12.px)
                    }
                })

                // Frontend
                Span({
                    style {
                        fontSize(11.px)
                        color(Color("#777777"))
                    }
                }) {
                    Text("FRONTEND")
                }

                InfoRow("Version", frontendInfo * version.get)
                InfoRow("Commit", (frontendInfo * commit.get) map { x: String -> x.take(7)})

                Div({
                    style {
                        property("border-top", "1px solid #EEEEEE")
                        marginTop(12.px)
                        marginBottom(12.px)
                    }
                })

                // Backend
                Span({
                    style {
                        fontSize(11.px)
                        color(Color("#777777"))
                    }
                }) {
                    Text("BACKEND")
                }

                InfoRow("Version", backendInfo * version.get)
                InfoRow("Commit", (backendInfo * commit.get) map {it.take(7)})
            }
        }
    }
}


@Composable
fun InfoRow(label: String, value: Source<String>) =
    InfoRow(label, value.emit())

@Composable
private fun InfoRow(label: String, value: String) {
    Div({
        style {
            display(DisplayStyle.Flex)
            justifyContent(JustifyContent.SpaceBetween)
            gap(20.px)
            alignItems(AlignItems.Center)
            marginTop(8.px)
            fontSize(12.px)
        }
    }) {
        Span({
            style {
                color(Color("#888888"))
            }
        }) {
            Text(label)
        }

        var isOverflowing by remember { mutableStateOf(false) }

        Span({
            ref { element ->
                isOverflowing = element.scrollWidth > element.clientWidth
                onDispose {}
            }

            if (isOverflowing) {
                title(value)
            }

            style {
                color(Color("#666666"))
                property("font-family", "monospace")
                property("min-width", "0")
                property("text-overflow", "ellipsis")
                property("white-space", "nowrap")
                overflow(Overflow.Hidden)
            }
        }) {
            Text(value)
        }
    }
}
