package org.solyton.solawi.bid.module.statistics.component

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.evoleq.compose.layout.Horizontal
import org.evoleq.compose.layout.Vertical
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.solyton.solawi.bid.module.statistics.data.Box
import org.solyton.solawi.bid.module.statistics.data.DistributionConfiguration
import org.solyton.solawi.bid.module.statistics.service.distribute

@Markup
@Composable
@Suppress("FunctionName")
fun Distribution(data: List<Double>, config: DistributionConfiguration) {
    val distribution = data.distribute(config)

    Horizontal({
        height(100.percent)
        width(100.percent)
        justifyContent(JustifyContent.SpaceEvenly)
    }) {
        distribution.percentiles.forEach { perc ->
            Percentile(perc.value, distribution.maxHeight)
        }
    }
}

@Markup
@Composable
@Suppress("FunctionName")
fun Percentile(box: Box, maxHeight: Double) {
    val top = 100.0 * (maxHeight - box.height) / maxHeight
    val bottom = 100.0 * box.height / maxHeight
    val width = (100.0 / box.resolution.toDouble()).percent

    Vertical({
        width(width)
        paddingLeft(1.px)
        paddingRight(1.px)

    }/*{flexGrow(1)}*/) {
        Div(attrs = {
            style {
                if(top == 100.0) {minHeight(1.px)}
                flex(top.percent)
                width(100.percent)
                backgroundColor(Color.transparent)
        }}){}
        Div(attrs = {style {
            flex(bottom.percent)
            width(100.percent)
            minHeight(bottom.percent)
            backgroundColor(Color.skyblue)
        }})
    }
}