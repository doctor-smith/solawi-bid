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
            data,
            //listOf(0,0,0,1,1,1,1,1,1,1,1,1,1,1,2,2,4,3,3,3,3,3,3,4,5,5,5,5,6,6,7,8,9,9,9,9).map{it.toDouble()},
            DistributionConfiguration(50.0, 100.0, 10)
        )
    }

}

val rawData: String = "85\n" +
    "78\n" +
    "55\n" +
    "82\n" +
    "83\n" +
    "82\n" +
    "82\n" +
    "60\n" +
    "82\n" +
    "88\n" +
    "82\n" +
    "82\n" +
    "80\n" +
    "84\n" +
    "66\n" +
    "85\n" +
    "78\n" +
    "70\n" +
    "82\n" +
    "85\n" +
    "83\n" +
    "75\n" +
    "84\n" +
    "70\n" +
    "82\n" +
    "80\n" +
    "85\n" +
    "82\n" +
    "82\n" +
    "85\n" +
    "85\n" +
    "82\n" +
    "82\n" +
    "75\n" +
    "82\n" +
    "82\n" +
    "80\n" +
    "60\n" +
    "85\n" +
    "82\n" +
    "82\n" +
    "90\n" +
    "82\n" +
    "82\n" +
    "80\n" +
    "75\n" +
    "70\n" +
    "80\n" +
    "82\n" +
    "78\n" +
    "60\n" +
    "75\n" +
    "75\n" +
    "82\n" +
    "100\n" +
    "82\n" +
    "82\n" +
    "85\n" +
    "55\n" +
    "78\n" +
    "85\n" +
    "85\n" +
    "85\n" +
    "82\n" +
    "85\n" +
    "82\n" +
    "85\n" +
    "82\n" +
    "82\n" +
    "82\n" +
    "82\n" +
    "90\n" +
    "88\n" +
    "72\n" +
    "83\n" +
    "80\n" +
    "80\n" +
    "60\n" +
    "85\n" +
    "90\n" +
    "82\n" +
    "82\n" +
    "65\n" +
    "80\n" +
    "83\n" +
    "70\n" +
    "82\n" +
    "65\n" +
    "82\n" +
    "82\n" +
    "70\n" +
    "85\n" +
    "55\n" +
    "82\n" +
    "80\n" +
    "78\n" +
    "75\n" +
    "80\n" +
    "82\n" +
    "65\n" +
    "80\n" +
    "82"

val data = rawData.split("\n").map { it.toInt().toDouble() }