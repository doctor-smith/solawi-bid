package org.solyton.solawi.bid.module.bid.component

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.evoleq.compose.layout.Horizontal
import org.evoleq.compose.layout.Vertical
import org.evoleq.compose.routing.navigate
import org.evoleq.language.Lang
import org.evoleq.language.subComp
import org.evoleq.math.Reader
import org.evoleq.math.emit
import org.evoleq.math.times
import org.evoleq.optics.lens.Lens
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.device.mediaType
import org.solyton.solawi.bid.application.data.deviceData
import org.solyton.solawi.bid.application.data.environment
import org.solyton.solawi.bid.module.bid.component.button.ChangeRoundStateButton
import org.solyton.solawi.bid.module.bid.component.button.EvaluateBidRoundButton
import org.solyton.solawi.bid.module.bid.component.button.ExportBidRoundResultsButton
import org.solyton.solawi.bid.module.bid.component.button.QRLinkToRoundPageButton
import org.solyton.solawi.bid.module.bid.component.effect.LaunchBidRoundEvaluation
import org.solyton.solawi.bid.module.bid.component.effect.LaunchDownloadOfBidRoundResults
import org.solyton.solawi.bid.module.bid.component.effect.LaunchExportOfBidRoundResults
import org.solyton.solawi.bid.module.bid.component.effect.LaunchPresentationOfBidRoundEvaluationInModal
import org.solyton.solawi.bid.module.bid.data.Auction
import org.solyton.solawi.bid.module.bid.data.Round
import org.solyton.solawi.bid.module.bid.data.api.RoundState
import org.solyton.solawi.bid.module.bid.data.reader.roundAccepted
import org.solyton.solawi.bid.module.bid.data.rounds
import org.solyton.solawi.bid.module.control.button.StdButton
import org.solyton.solawi.bid.module.bid.data.reader.rounds as roundsKey

@Markup
@Composable
@Suppress("FunctionName")
fun BidRoundList(
    storage: Storage<Application>,
    auction: Lens<Application, Auction>,
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

@Markup
@Composable
@Suppress("FunctionName")
fun BidRoundListItem(
    storage: Storage<Application>,
    auction: Lens<Application, Auction>,
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
            frontendBaseUrl= frontendBaseUrl,
            texts = texts
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
            ExportBidRoundResultsButton(
                storage = storage,
                auction = auction,
                round = round,
                texts = (texts * subComp("bidRoundList") * subComp("item") * subComp("buttons") * subComp("exportResults"))
            )
            EvaluateBidRoundButton(
                storage = storage,
                auction = auction,
                round = round,
                //texts = (texts * subComp("bidRoundList") * subComp("item") * subComp("buttons") * subComp("exportResults"))
            )
            // todo:i18n
            StdButton({ "Bieten" }, storage * deviceData * mediaType.get) {
                navigate("/bid/send/$${round.link}")
            }
        }
    }
}
