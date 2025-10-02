package org.solyton.solawi.bid.application.ui.page.auction

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.launch
import org.evoleq.compose.Markup
import org.evoleq.compose.layout.Vertical
import org.evoleq.math.Source
import org.evoleq.math.emit
import org.evoleq.optics.lens.FirstBy
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.justifyContent
import org.jetbrains.compose.web.css.vh
import org.jetbrains.compose.web.dom.Div
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.auctions
import org.solyton.solawi.bid.application.data.environment
import org.solyton.solawi.bid.module.bid.action.readAuctions
import org.solyton.solawi.bid.module.bid.data.auction.rounds
import org.solyton.solawi.bid.module.bid.data.bidround.link
import org.solyton.solawi.bid.module.qrcode.QRCodeSvg
import org.solyton.solawi.bid.module.style.page.verticalPageStyle

// todo improve data transfer to page
@Markup
@Composable
@Suppress("FunctionName")
fun RoundPage(storage: Storage<Application>, auctionId: String, roundId: String) = Div{
    // Effect
    // Load missing data if necessary
    val dataIsMissing = onMissing(
        storage * auctions.get,
        { it.auctionId == auctionId}
    ){
        LaunchedEffect(Unit) {
            launch {
                readAuctions()
            }
        }
    }
    if(dataIsMissing) return@Div

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

@Composable fun <T> onMissing(
    storage: Source<List<T>>,
    predicate: (T)-> Boolean,
    effect: @Composable ()->Unit
): Boolean {
    val missing = storage.emit().none { predicate(it) }
    return if(missing){ effect(); true } else { false }
}
