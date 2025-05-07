package org.solyton.solawi.bid.module.loading.component

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.jetbrains.compose.web.dom.Div

@Markup
@Composable
@Suppress("FunctionName")

fun Loading() = Div ({
    classes("loading-page")
})