package org.solyton.solawi.bid.application.ui.page.test

import androidx.compose.runtime.*
import org.evoleq.compose.Markup
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.vh
import org.jetbrains.compose.web.css.vw
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.TextInput
import org.solyton.solawi.bid.application.ui.style.form.textInputDesktopStyle
import org.solyton.solawi.bid.application.ui.style.wrap.Wrap
import org.solyton.solawi.bid.module.qrcode.QRCodeSvg
import org.solyton.solawi.bid.module.statistics.component.Distribution
import org.solyton.solawi.bid.module.statistics.data.DistributionConfiguration

@Markup
@Composable
@Suppress("FunctionName")
fun TestPage() {
    Text("Test Page")

    var text by remember { mutableStateOf<String>("Hello, QR Code!") }
    Wrap {
        Div {

            TextInput(text) {
                style { textInputDesktopStyle() }
                id("qr-string")
                onInput {
                    text = it.value.trim()
                }
            }


            QRCodeSvg("test-page", text, download = true)


        }
    }

    Wrap(style = {
        width(50.vw)
        height(20.vh)
    }){
        Distribution(
            listOf(0,0,0,1,1,1,1,1,1,1,1,1,1,1,2,2,4,3,3,3,3,3,3,4,5,5,5,5,6,6,7,8,9,9,9,9).map{it.toDouble()},
            DistributionConfiguration(0.0, 11.0, 10)
        )
    }

}