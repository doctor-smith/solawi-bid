package org.solyton.solawi.bid.application.ui.page.auction

import androidx.compose.runtime.*
import org.evoleq.compose.Markup
import org.evoleq.compose.layout.*
import org.evoleq.device.data.mediaType
import org.evoleq.language.Locale
import org.evoleq.language.component
import org.evoleq.language.subComp
import org.evoleq.language.title
import org.evoleq.math.Reader
import org.evoleq.math.emit
import org.evoleq.math.map
import org.evoleq.math.times
import org.evoleq.optics.lens.FirstBy
import org.evoleq.optics.lens.times
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.isEmpty
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.transform.bid.bidApplicationIso
import org.solyton.solawi.bid.module.bid.action.readAuctions
import org.solyton.solawi.bid.module.bid.component.AuctionDetails
import org.solyton.solawi.bid.module.bid.component.BidRoundListItem
import org.solyton.solawi.bid.module.bid.component.button.CreateNewRoundButton
import org.solyton.solawi.bid.module.bid.component.button.ImportBiddersButton
import org.solyton.solawi.bid.module.bid.component.button.UpdateAuctionButton
import org.solyton.solawi.bid.module.bid.component.effect.LaunchDownloadOfBidRoundResults
import org.solyton.solawi.bid.module.bid.component.effect.TriggerBidRoundEvaluation
import org.solyton.solawi.bid.module.bid.component.effect.TriggerExportOfBidRoundResults
import org.solyton.solawi.bid.module.bid.data.*
import org.solyton.solawi.bid.module.bid.data.api.AddBidders
import org.solyton.solawi.bid.module.bid.data.api.NewBidder
import org.solyton.solawi.bid.module.bid.data.api.RoundState
import org.solyton.solawi.bid.module.bid.data.auction.rounds
import org.solyton.solawi.bid.module.bid.data.bidround.Round
import org.solyton.solawi.bid.module.bid.data.reader.BidComponent
import org.solyton.solawi.bid.module.bid.permission.BidRight
import org.solyton.solawi.bid.module.bid.service.isNotGranted
import org.solyton.solawi.bid.module.control.button.EvaluationButton
import org.solyton.solawi.bid.module.control.button.FileExportButton
import org.solyton.solawi.bid.module.i18n.data.language
import org.solyton.solawi.bid.module.i18n.data.locale
import org.solyton.solawi.bid.module.style.layout.accent.vertical.verticalAccentStyles
import org.solyton.solawi.bid.module.style.page.verticalPageStyle
import org.solyton.solawi.bid.module.style.wrap.Wrap

val auctionPropertiesStyles = PropertiesStyles(
    containerStyle = { width(40.percent) },
    propertyStyles = PropertyStyles(
        keyStyle = { width(50.percent) },
        valueStyle = { width(50.percent) }
    )
)

@Markup
@Composable
@Suppress("FunctionName")
fun AuctionPage(storage: Storage<Application>, auctionId: String) = Div {
    // Effects
    LaunchedEffect(Unit) {
        (storage * bidApplicationIso * actions).read().dispatch(readAuctions())
    }
    if((storage * bidApplicationIso * auctions).isEmpty()) {
        return@Div
    }

    // Internal State
    var newBidders by remember { mutableStateOf<List<NewBidder>>(listOf()) }
    var addBidders by remember { mutableStateOf<AddBidders>(AddBidders()) }

    // Data
    val auction = auctions * FirstBy { it.auctionId == auctionId }

    val runningRound = auction * rounds * Reader {
        list: List<Round> -> list.firstOrNull{
            round -> round.state != RoundState.Frozen.toString()
        }
    }
    val frozenRounds = auction * rounds * Reader {
        list: List<Round> -> list
            .filter{ it.state == RoundState.Frozen.toString() }
            .sortedByDescending { it.roundNumber }
    }

    // Texts
    val texts = (storage * bidApplicationIso * i18N * language * component(BidComponent.AuctionPage))
    val details = texts * subComp("details")
    val buttons = texts * subComp("buttons")

    // Markup
    Vertical(verticalPageStyle) {
        Wrap { Horizontal(styles = { justifyContent(JustifyContent.SpaceBetween); width(100.percent) }) {
            H1 { Text(with((storage * bidApplicationIso * auction).read()) { name }) }
            Horizontal({
                alignItems(AlignItems.Center);
                // todo:dev - use default value
                gap(5.px)}
            ) {
                UpdateAuctionButton(
                    storage = storage * bidApplicationIso,
                    auction = auction,
                    texts = buttons * subComp("updateAuction"),
                    dataId = "auction-page.button.configure-auction"
                )
                ImportBiddersButton(
                    storage = storage * bidApplicationIso,
                    newBidders = Storage<List<NewBidder>>(
                        read = { newBidders },
                        write = { newBidders = it }
                    ),
                    addBidders = Storage<AddBidders>(
                        read = { addBidders },
                        write = { addBidders = it }
                    ),
                    auction = auction,
                    texts = buttons * subComp("importBidders"),
                    dataId = "auction-page.button.import-bidders"
                )
                CreateNewRoundButton(
                    storage = storage * bidApplicationIso,
                    auction = auction,
                    texts = buttons * subComp("createRound"),
                    dataId = "auction-page.button.create-round"
                )
            }
        } }
        //LineSeparator()
        Wrap { H2 { Text((details * title).emit()) } }
        Wrap { Horizontal {
            AuctionDetails(
                storage * bidApplicationIso * auction,
                storage * bidApplicationIso * i18N * locale.get map {l -> Locale.from(l)},
                details,
                auctionPropertiesStyles
            )
        } }

        // LineSeparator()
        Wrap {
            val r = (storage * bidApplicationIso * runningRound).emit()
            val frontendBaseUrl = with((storage * bidApplicationIso * environment).read()) {
                "$frontendUrl:$frontendPort"
            }
            if (r != null) {
                BidRoundListItem(
                    storage = storage * bidApplicationIso,
                    auction = auction,
                    r,
                    frontendBaseUrl,
                    (storage * bidApplicationIso * i18N * language * component(BidComponent.Round))
                )
            }
        }

        VerticalAccent(verticalAccentStyles(storage * bidApplicationIso * deviceData * mediaType.get)) {
            Wrap{
                // todo:i18n
                H2({style { marginLeft(20.px) }}) { Text("Abgeschlossene Runden") }
                // Header
                Horizontal(styles = {
                    justifyContent(JustifyContent.SpaceBetween)
                    alignItems(AlignItems.Center)
                    width(100.percent)
                }) {
                    Horizontal({justifyContent(JustifyContent.FlexStart); width(80.percent)}) {
                        // Space for  Check or Xmark
                        // -> HCell
                        // todo:i18n
                        Div({style { marginLeft(20.px); width(10.percent); fontWeight("bold") }}){Text("Runde Nr")}

                        // todo:i18n
                        Div({style { marginLeft(20.px); width(10.percent); fontWeight("bold") }}){Text("Start")}

                        // todo:i18n
                        Div({style { marginLeft(20.px); width(10.percent); fontWeight("bold") }}){Text("Stop")}

                        // todo:i18n
                        Div({style { width(50.percent); fontWeight("bold") }}){ Text("Kommentar")}
                    }
                }
                // Rows
                (storage * bidApplicationIso * frozenRounds).emit().forEach { round ->
                    // Effect
                    LaunchDownloadOfBidRoundResults(
                        storage = storage * bidApplicationIso,
                        auction = auction,
                        round = round,
                        texts = texts
                    )

                    Horizontal(styles = {
                        justifyContent(JustifyContent.SpaceBetween)
                        alignItems(AlignItems.Center)
                        width(100.percent)
                    }) {
                        Horizontal({justifyContent(JustifyContent.FlexStart); width(80.percent)}) {
                            // Check or Xmark
                            // -> Cell
                            Div({style { marginLeft(20.px); width(10.percent) }}){Text(round.roundNumber.toString())}

                            // todo:i18n date
                            Div({style { marginLeft(20.px); width(10.percent) }}){Text("8:30")}

                            // todo:i18n date
                            Div({style { marginLeft(20.px); width(10.percent) }}){Text("8:45")}

                            Div({style { width(50.percent) }}){ Text("Kommentar zu Runde ${round.roundNumber}")}
                        }

                        Horizontal({justifyContent(JustifyContent.FlexEnd)}) {
                            val isExportDisabled = (storage * bidApplicationIso * user.get).emit().isNotGranted(BidRight.BidRound.manage)
                            // todo:i18n
                            FileExportButton(
                                Color.black,
                                Color.transparent,
                                {"Ergebnisse Exportieren"},
                                storage * bidApplicationIso * deviceData * mediaType.get,
                                isExportDisabled
                            ) {
                                TriggerExportOfBidRoundResults(
                                    storage = storage * bidApplicationIso,
                                    auction = auction,
                                    round = round
                                )
                            }

                            val isEvaluationDisabled = (storage * bidApplicationIso * user.get).emit().isNotGranted(BidRight.BidRound.manage)
                            // todo:i18n
                            EvaluationButton(
                                Color.black,
                                Color.transparent,
                                {"Evaluation einsehen"},
                                storage * bidApplicationIso * deviceData * mediaType.get,
                                isEvaluationDisabled
                            ) {
                                TriggerBidRoundEvaluation(
                                    storage = storage * bidApplicationIso,
                                    auction = auction,
                                    round = round
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
