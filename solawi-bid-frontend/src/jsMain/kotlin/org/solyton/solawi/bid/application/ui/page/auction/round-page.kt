package org.solyton.solawi.bid.application.ui.page.auction

import androidx.compose.runtime.*
import org.evoleq.compose.Markup
import org.evoleq.compose.layout.Vertical
import org.evoleq.optics.lens.FirstBy
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.auctions
import org.solyton.solawi.bid.application.data.environment
import org.solyton.solawi.bid.module.style.page.verticalPageStyle
import org.solyton.solawi.bid.module.bid.data.link
import org.solyton.solawi.bid.module.bid.data.rounds
import org.solyton.solawi.bid.module.qrcode.QRCodeSvg

// todo improve data transfer to page
@Markup
@Composable
@Suppress("FunctionName")
fun RoundPage(storage: Storage<Application>, auctionId: String, roundId: String) = Div{
    // Data
    val auction = storage * auctions * FirstBy { it.auctionId == auctionId }
    val round = auction * rounds * FirstBy { it.roundId == roundId }
    val link = round * link
    val frontendBaseUrl = with((storage * environment).read()){
        "$frontendUrl:$frontendPort"
    }
    val fullLink = "$frontendBaseUrl/bid/send/${link.read()}"

    //H1 { Text("Round Page") }
    Vertical(verticalPageStyle){
        Vertical({
            justifyContent(JustifyContent.Center)
        }) {
            QRCodeSvg(
                size = 80.vh,
                id = roundId,
                data = fullLink,
                download = false
            )
        }
    }

    /*
    Div(attrs = {
                style {
                    width(80.percent)
                }
            }){
                Text("left")
            }



    Div() {
        var preEvaluationLoaded  by remember { mutableStateOf(false) }
        // todo:improve find another way?
        if(!preEvaluationLoaded) {
            LaunchedEffect(Unit) {
                launch {
                    (storage * actions).read().emit(
                        preEvaluateBidRound(
                            auctionId,
                            auctions * FirstBy { it.auctionId == auctionId } * rounds * FirstBy { it.roundId == roundId }
                        )
                    )
                    preEvaluationLoaded = true
                }
            }
        }


        val preEvaluation = (round * preEvaluation).read()
        val details = preEvaluation.auctionDetailsPreEval

        Text(preEvaluation.toString())
    }
    */
}
