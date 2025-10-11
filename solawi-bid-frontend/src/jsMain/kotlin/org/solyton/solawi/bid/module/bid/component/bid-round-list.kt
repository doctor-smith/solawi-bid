package org.solyton.solawi.bid.module.bid.component

import androidx.compose.runtime.Composable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.evoleq.compose.Markup
import org.evoleq.compose.layout.Horizontal
import org.evoleq.compose.layout.Vertical
import org.evoleq.compose.routing.navigate
import org.evoleq.compose.routing.openUrlInNewTab
import org.evoleq.device.data.mediaType
import org.evoleq.language.Lang
import org.evoleq.language.subComp
import org.evoleq.math.Reader
import org.evoleq.math.emit
import org.evoleq.math.times
import org.evoleq.optics.lens.FirstBy
import org.evoleq.optics.lens.Lens
import org.evoleq.optics.lens.times
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.module.bid.action.changeRoundState
import org.solyton.solawi.bid.module.bid.component.button.ChangeRoundStateButton
import org.solyton.solawi.bid.module.bid.component.button.ExportBidRoundResultsButton
import org.solyton.solawi.bid.module.bid.component.button.ExportBidRoundResultsButton_Dep
import org.solyton.solawi.bid.module.bid.component.button.QRLinkToRoundPageButton
import org.solyton.solawi.bid.module.bid.component.effect.LaunchBidRoundEvaluation
import org.solyton.solawi.bid.module.bid.component.effect.LaunchDownloadOfBidRoundResults
import org.solyton.solawi.bid.module.bid.component.effect.LaunchExportOfBidRoundResults
import org.solyton.solawi.bid.module.bid.component.effect.LaunchPresentationOfBidRoundEvaluationInModal
import org.solyton.solawi.bid.module.bid.data.*
import org.solyton.solawi.bid.module.bid.data.api.RoundState
import org.solyton.solawi.bid.module.bid.data.api.nextState
import org.solyton.solawi.bid.module.bid.data.auction.Auction
import org.solyton.solawi.bid.module.bid.data.auction.rounds
import org.solyton.solawi.bid.module.bid.data.bidround.Round
import org.solyton.solawi.bid.module.bid.data.reader.roundAccepted
import org.solyton.solawi.bid.module.control.button.IconButton
import org.solyton.solawi.bid.module.control.button.StdButton
import org.solyton.solawi.bid.module.error.component.showErrorModal
import org.solyton.solawi.bid.module.error.lang.errorModalTexts
import org.solyton.solawi.bid.module.bid.data.reader.rounds as roundsKey

@Deprecated("Will be replaced")
@Markup
@Composable
@Suppress("FunctionName")
fun BidRoundList(
    storage: Storage<BidApplication>,
    auction: Lens<BidApplication, Auction>,
    roundTexts: Reader<Unit, Lang.Block>
) {
    H2 {
        Text((roundTexts * subComp("bidRoundList") * roundsKey).emit())
    }

    val frontendBaseUrl = with((storage * environment).read()){
        "$frontendUrl:$frontendPort"
    }
    (storage * auction * rounds).read().reversed().forEach { round ->
        BidRoundListItem(
            storage = storage,
            auction = auction,
            round = round,
            frontendBaseUrl= frontendBaseUrl,
            texts = roundTexts
        )
    }
}

@Deprecated("Will be replaced")
@Markup
@Composable
@Suppress("FunctionName")
fun BidRoundListItem(
    storage: Storage<BidApplication>,
    auction: Lens<BidApplication, Auction>,
    round: Round,
    frontendBaseUrl: String,
    texts: Reader<Unit, Lang.Block>
) {
    // Effects
    when(RoundState.fromString(round.state) ) {
        is RoundState.Stopped -> LaunchExportOfBidRoundResults(
            storage = storage,
            auction = auction,
            round = round
        )
        is RoundState.Evaluated -> LaunchBidRoundEvaluation(
            storage = storage,
            auction = auction,
            round = round
        )
        is RoundState.Closed -> LaunchPresentationOfBidRoundEvaluationInModal(
            storage = storage,
            auction = auction,
            round = round
        )
        is RoundState.Opened,
        is RoundState.Started,

        is RoundState.Frozen  -> Unit
    }

    // Markup
    Horizontal(styles = {
        width(100.percent)
        justifyContent(JustifyContent.SpaceBetween)
    }) {
        // Effect
        LaunchDownloadOfBidRoundResults(
            storage = storage,
            auction = auction,
            round = round,
            texts = texts
        )
        // Buttons
        QRLinkToRoundPageButton(
            storage = storage,
            auction = auction,
            round = round,
            frontendBaseUrl= frontendBaseUrl
        )
        Horizontal {
            ChangeRoundStateButton(
                storage = storage,
                auction = auction,
                round = round,
                texts = texts
            )
            Div({style { width(20.px); minHeight(1.px) }})
            BidProcess(
                texts,
                device = storage * deviceData * mediaType.get,
                round,
                (storage * auction * roundAccepted(round.roundId)).emit()
            )
        }
        Vertical {
            ExportBidRoundResultsButton_Dep(
                storage = storage,
                auction = auction,
                round = round,
                texts = (texts * subComp("bidRoundList") * subComp("item") * subComp("buttons") * subComp("exportResults"))
            )
            // todo:i18n
            StdButton({ "Bieten" }, storage * deviceData * mediaType.get) {
                navigate("/bid/send/${round.link}")
            }
        }
    }
}

@Markup
@Composable
@Suppress("FunctionName")
fun CurrentBidRound(
    storage: Storage<BidApplication>,
    auction: Lens<BidApplication, Auction>,
    round: Round,
    frontendBaseUrl: String,
    texts: Reader<Unit, Lang.Block>
) {
    // Effects
    when(RoundState.fromString(round.state) ) {
        is RoundState.Stopped -> LaunchExportOfBidRoundResults(
            storage = storage,
            auction = auction,
            round = round
        )
        is RoundState.Evaluated -> LaunchBidRoundEvaluation(
            storage = storage,
            auction = auction,
            round = round
        )
        is RoundState.Closed -> LaunchPresentationOfBidRoundEvaluationInModal(
            storage = storage,
            auction = auction,
            round = round
        )
        is RoundState.Opened,
        is RoundState.Started,

        is RoundState.Frozen  -> Unit
    }

    // Markup
    Vertical( {
        width(100.percent)
        justifyContent(JustifyContent.Center)
        gap(20.px)
        marginTop(30.px)
        marginBottom(10.px)
    }) {
        // Effect
        LaunchDownloadOfBidRoundResults(
            storage = storage,
            auction = auction,
            round = round,
            texts = texts
        )

        // todo:i18n
        H2{Text( "Runde ${round.roundNumber}" )}

        Horizontal({justifyContent(JustifyContent.Center); width(100.percent)}) {
            BidProcess(
                texts,
                device = storage * deviceData * mediaType.get,
                round,
                (storage * auction * roundAccepted(round.roundId)).emit()
            ) {
                CoroutineScope(Job()).launch {
                    val actions = (storage * actions).read()
                    try {
                        actions.dispatch(
                            changeRoundState(
                                RoundState.fromString(round.state).nextState(),
                                auction * rounds * FirstBy { it.roundId == round.roundId })
                        )
                    } catch (exception: Exception) {
                        (storage * modals).showErrorModal(
                            texts = errorModalTexts(
                                exception.message ?: exception.cause?.message ?: "Cannot Emit action 'ChangeRoundState'"
                            ),
                            device = storage * deviceData * mediaType.get,
                        )
                    }
                }
            }
        }
        Horizontal {
            QRLinkToRoundPageButton(
                storage = storage,
                auction = auction,
                round = round,
                frontendBaseUrl= frontendBaseUrl
            )
            ExportBidRoundResultsButton(
                storage = storage,
                auction = auction,
                round = round,
                texts = (texts * subComp("bidRoundList") * subComp("item") * subComp("buttons") * subComp("exportResults")),
                device = storage * deviceData * mediaType.get,
                isDisabled = RoundState.fromString(round.state) !in setOf(
                    RoundState.Started,
                    RoundState.Closed,
                    RoundState.Frozen
                )
            )

            // todo:i18n
            IconButton(
                Color.black,
                Color.transparent,
                arrayOf("fa-solid", "fa-chalkboard-user"),
                {"Im Auftrag anderer bieten"},
                storage * deviceData * mediaType.get,
                isDisabled = round.state != "${RoundState.Started}"
            ) {
                openUrlInNewTab("/bid/send/${round.link}")
            }
        }
    }
}
