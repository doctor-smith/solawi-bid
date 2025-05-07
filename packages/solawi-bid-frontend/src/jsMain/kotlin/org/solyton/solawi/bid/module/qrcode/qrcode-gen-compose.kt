package org.solyton.solawi.bid.module.qrcode

import androidx.compose.runtime.*
import kotlinx.browser.document
import kotlinx.coroutines.launch
import org.evoleq.compose.Markup
import org.jetbrains.compose.web.css.CSSNumeric
import org.jetbrains.compose.web.css.CSSNumericValue
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLDivElement

@JsModule("qrcode")
@JsNonModule
external object QRCode {
    fun toString(text: String, options: dynamic, callback: (error: String?, svg: String?) -> Unit)
}

@Markup
@Composable
@Suppress("FunctionName")
fun QRCodeSvg(id: String, data: String, size: Double = 256.0, download: Boolean = false) =
    QRCodeSvg(id, data, size.px, download)


@Markup
@Composable
@Suppress("FunctionName")
fun QRCodeSvg(id: String, data: String, size: CSSNumeric, download: Boolean = false) {
    var qrSvg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(data) {
        launch {
            QRCode.toString(data, js("{}")) { _, svg ->
                qrSvg = svg
            }
        }
    }

    Div(attrs = {
        id("qr-container-$id")
        style {
            width(size)
        }
    }) {
        qrSvg?.let { svgContent ->
            DisposableEffect(svgContent) {
                val container = document.getElementById("qr-container-$id") as? HTMLDivElement
                container?.innerHTML = svgContent // Inject the SVG into the div
                onDispose { container?.innerHTML = "" }
            }
        }
    }
    if(download) {
        DownloadSvgButton(qrSvg)
    }
}



@Markup
@Composable
@Suppress("FunctionName")
fun DownloadSvgButton(svgString: String?) {
    if(svgString != null) {
        Button(attrs = {
            onClick {
                downloadSvg( svgString)
            }
        }) {
            Text("Download QR Code")
        }
    }
}

fun downloadSvg(svgString: String) {
    val blob = js("new Blob([svgString], {type: 'image/svg+xml'})")
    val url = js("URL.createObjectURL(blob)")

    // Create an anchor tag dynamically to trigger the download
    val link = js("document.createElement('a')")
    link.href = url as String
    link.download = "image.svg"

    // Trigger the download by clicking the link programmatically
    js("link.click()")

    // Optionally, revoke the URL after use
    js("URL.revokeObjectURL(url)")
}
