package org.solyton.solawi.bid.application.ui.page.auction

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.launch
import org.evoleq.compose.Markup
import org.evoleq.compose.guard.data.onMissing
import org.evoleq.compose.layout.Vertical
import org.evoleq.math.Reader
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
import org.solyton.solawi.bid.application.data.transform.bid.bidApplicationIso
import org.solyton.solawi.bid.application.service.useI18nTransform
import org.solyton.solawi.bid.application.ui.effect.LaunchComponentLookup
import org.solyton.solawi.bid.module.bid.action.readAuctions
import org.solyton.solawi.bid.module.bid.data.auction.rounds
import org.solyton.solawi.bid.module.bid.data.bidround.link
import org.solyton.solawi.bid.module.bid.data.i18N
import org.solyton.solawi.bid.module.bid.data.reader.BidComponent
import org.solyton.solawi.bid.module.i18n.data.componentLoaded
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
        { it.auctionId == auctionId }
    ) {
        LaunchedEffect(Unit) {
            launch {
                readAuctions()
            }
        }
    }
    if(dataIsMissing) return@Div
    LaunchComponentLookup(
        BidComponent.Round,
        storage  * Reader { app: Application -> app.environment.useI18nTransform() },
        storage * bidApplicationIso * i18N
    )
    val loaded = (storage * bidApplicationIso * i18N * componentLoaded(BidComponent.Round)).emit()
    if(!loaded) return@Div

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

