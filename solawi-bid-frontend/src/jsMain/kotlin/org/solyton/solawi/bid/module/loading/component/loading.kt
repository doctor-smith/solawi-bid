package org.solyton.solawi.bid.module.loading.component

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div

@Markup
@Composable
@Suppress("FunctionName")
fun Loading() = Div ({
    classes("loading-page")
})


@Markup
@Composable
@Suppress("FunctionName")
fun LoadingContent() =
    Div({
        style {
            display(DisplayStyle.Flex)
            justifyContent(JustifyContent.Center)
            alignItems(AlignItems.Center)
            height(100.percent)
            width(100.percent)
        }
    }) {
        Div({
            classes("loading-content")
            style {
                flexGrow(0.0)
            }
        })
    }
