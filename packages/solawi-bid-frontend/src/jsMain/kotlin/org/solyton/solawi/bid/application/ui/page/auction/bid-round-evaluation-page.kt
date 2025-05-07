package org.solyton.solawi.bid.application.ui.page.auction

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.evoleq.optics.storage.Storage
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.application.data.Application

@Markup
@Composable
@Suppress("FunctionName")
fun BidRoundEvaluationPage(storage: Storage<Application>, bidRoundId: String) = Div{
    H1 { Text("BidRoundEvaluationPage") }


}