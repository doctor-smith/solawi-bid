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
/*
 * Copyright (c) 2021. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

import org.jetbrains.letsPlot.frontend.JsFrontendUtil
import org.jetbrains.letsPlot.geom.geomDensity
import org.jetbrains.letsPlot.letsPlot
import kotlinx.browser.document
import kotlinx.browser.window
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.sqrt
import kotlin.random.Random

fun createContent() {
    val contentDiv = document.getElementById("content")

    val n = 200
    val data = mapOf<String, Any>(
        "x" to List(n) { nextGaussian() }
    )

    val p = letsPlot(data) + geomDensity(
        color = "dark-green",
        fill = "yellow",
        alpha = .3,
        size = 2.0
    ) { x = "x" }

    val plotDiv = JsFrontendUtil.createPlotDiv(p)
    contentDiv?.appendChild(plotDiv)
}

/**
 * The Box-Muller transform converts two independent uniform variates on (0, 1)
 * into two standard Gaussian variates (mean 0, variance 1).
 * https://en.wikipedia.org/wiki/Box%E2%80%93Muller_transform
 */
fun nextGaussian(): Double {
    var u = 0.0
    var v = 0.0
    while (u < 1.0e-7) u = Random.nextDouble()
    while (v < 1.0e-7) v = Random.nextDouble()
    return sqrt(-2.0 * ln(u)) * cos(2.0 * PI * v)
}

@Markup
@Composable
@Suppress("FunctionName")
fun TestPage() {
    Text("Test Page")
    var text by remember { mutableStateOf<String>("Hello, QR Code!") }
    Div(attrs = {id("content")})
    LaunchedEffect(Unit) {
        createContent()
    }
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