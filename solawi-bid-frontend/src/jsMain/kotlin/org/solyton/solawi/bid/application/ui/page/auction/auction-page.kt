package org.solyton.solawi.bid.application.ui.page.auction

import androidx.compose.runtime.*
import org.evoleq.compose.Markup
import org.evoleq.compose.guard.data.isLoading
import org.evoleq.compose.guard.data.onEmpty
import org.evoleq.compose.layout.*
import org.evoleq.compose.routing.openUrlInNewTab
import org.evoleq.device.data.mediaType
import org.evoleq.language.*
import org.evoleq.math.*
import org.evoleq.optics.lens.FirstBy
import org.evoleq.optics.lens.times
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.transform.bid.bidApplicationIso
import org.solyton.solawi.bid.application.service.useI18nTransform
import org.solyton.solawi.bid.application.ui.effect.LaunchComponentLookup
import org.solyton.solawi.bid.module.bid.action.readAuctions
import org.solyton.solawi.bid.module.bid.component.AuctionDetails
import org.solyton.solawi.bid.module.bid.component.AuctionListStyles
import org.solyton.solawi.bid.module.bid.component.BidArrow
import org.solyton.solawi.bid.module.bid.component.CurrentBidRound
import org.solyton.solawi.bid.module.bid.component.button.AuctionsButton
import org.solyton.solawi.bid.module.bid.component.button.CreateNewRoundButton
import org.solyton.solawi.bid.module.bid.component.button.ImportBiddersButton
import org.solyton.solawi.bid.module.bid.component.button.UpdateAuctionButton
import org.solyton.solawi.bid.module.bid.component.effect.LaunchDownloadOfBidRoundResults
import org.solyton.solawi.bid.module.bid.component.effect.TriggerBidRoundEvaluation
import org.solyton.solawi.bid.module.bid.component.effect.TriggerExportOfBidRoundResults
import org.solyton.solawi.bid.module.bid.component.list.HeaderCell
import org.solyton.solawi.bid.module.bid.component.list.TextCell
import org.solyton.solawi.bid.module.bid.component.list.TimeCell
import org.solyton.solawi.bid.module.bid.data.*
import org.solyton.solawi.bid.module.bid.data.api.AddBidders
import org.solyton.solawi.bid.module.bid.data.api.NewBidder
import org.solyton.solawi.bid.module.bid.data.api.RoundState
import org.solyton.solawi.bid.module.bid.data.auction.rounds
import org.solyton.solawi.bid.module.bid.data.bidround.Round
import org.solyton.solawi.bid.module.bid.data.reader.BidComponent
import org.solyton.solawi.bid.module.bid.data.reader.auctionAccepted
import org.solyton.solawi.bid.module.bid.permission.BidRight
import org.solyton.solawi.bid.module.bid.service.isNotGranted
import org.solyton.solawi.bid.module.control.button.DownloadButton
import org.solyton.solawi.bid.module.control.button.EvaluationButton
import org.solyton.solawi.bid.module.control.button.HelpButton
import org.solyton.solawi.bid.module.i18n.data.language
import org.solyton.solawi.bid.module.i18n.data.locale
import org.solyton.solawi.bid.module.i18n.guard.onMissing
import org.solyton.solawi.bid.module.style.forestGreenUltraLite
import org.solyton.solawi.bid.module.style.layout.accent.vertical.verticalAccentStyles
import org.solyton.solawi.bid.module.style.page.verticalPageStyle
import org.solyton.solawi.bid.module.style.wrap.Wrap
import kotlin.js.Date

val auctionPropertiesStyles = PropertiesStyles(
    containerStyle = { width(40.percent) },
    propertyStyles = PropertyStyles(
        keyStyle = { width(50.percent) },
        valueStyle = { width(50.percent) }
    )
)

@Markup
@Composable
@Suppress("FunctionName", "CognitiveComplexMethod")
fun AuctionPage(storage: Storage<Application>, auctionId: String) = Div({style { overflowY("auto" /*todo:dev - scroll only if needed*/)}}) {
    // Effects
    if(isLoading(
        onEmpty(
            storage * bidApplicationIso * auctions.get
        ) {
            LaunchedEffect(Unit) {
                (storage * bidApplicationIso * actions).read().dispatch(readAuctions())
            }
        },
        onMissing(
                BidComponent.AuctionPage,
                storage * bidApplicationIso * i18N.get,
        ) {
            LaunchComponentLookup(
                BidComponent.AuctionPage,
                storage  * Reader { app: Application -> app.environment.useI18nTransform() },
                storage * bidApplicationIso * i18N
            )
        }
    )) return@Div

    // Internal State
    var newBidders by remember { mutableStateOf<List<NewBidder>>(listOf()) }
    var addBidders by remember { mutableStateOf<AddBidders>(AddBidders()) }

    // Data
    val auction = auctions * FirstBy { it.auctionId == auctionId }
    val auctionAccepted = storage * bidApplicationIso * auction * auctionAccepted
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
    Vertical({verticalPageStyle(); }) {
        // Auction Details
        VerticalAccent(verticalAccentStyles(storage * bidApplicationIso * deviceData * mediaType.get)) {
            Wrap({marginLeft(20.px)}) {
                Horizontal(styles = { justifyContent(JustifyContent.SpaceBetween); width(100.percent) }) {
                    // Title
                    H1 { Text(with((storage * bidApplicationIso * auction).read()) { name }) }

                    // Action Buttons
                    Horizontal({
                        alignItems(AlignItems.Center);
                        // todo:dev - use default value
                        gap(5.px)}
                    ) {
                        HelpButton(
                            color = Color.black,
                            bgColor = Color.transparent,
                            texts = buttons * subComp("help") * tooltip ,
                            deviceType = storage * bidApplicationIso * deviceData * mediaType.get,
                            isDisabled = false,
                            dataId = "auction-page.button.help"
                        ) {
                            openUrlInNewTab("/manual/how-to-carry-ou-an-auction")
                        }
                        AuctionsButton(
                            url = "/app/auctions",
                            color = Color.black,
                            bgColor = Color.transparent,
                            // todo:i18n
                            texts= {"Auctions"},
                            deviceType = storage * bidApplicationIso * deviceData * mediaType.get,
                            isDisabled = false,
                            dataId = "auction-page.button.nav-to-auctions"
                        )
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
                }
            }
            Wrap({marginLeft(20.px)}) {
                H2 { Text((details * title).emit()) }
            }
            Wrap({marginLeft(20.px); backgroundColor(forestGreenUltraLite)}) {
                Horizontal {
                    AuctionDetails(
                        storage * bidApplicationIso * auction,
                        storage * bidApplicationIso * i18N * locale.get map { l -> Locale.from(l) },
                        details,
                        auctionPropertiesStyles
                    )
                }
            }
        }

        // Process / ~ Explanation
        Wrap({width(100.percent)}) {
            val r = (storage * bidApplicationIso * runningRound).emit()
            val frontendBaseUrl = with((storage * bidApplicationIso * environment).read()) {
                "$frontendUrl:$frontendPort"
            }
            if (r != null && not(auctionAccepted).emit()) {
                CurrentBidRound(
                    storage = storage * bidApplicationIso,
                    auction = auction,
                    r,
                    frontendBaseUrl,
                    (storage * bidApplicationIso * i18N * language * component(BidComponent.Round))
                )
            }
            if(r == null && not(auctionAccepted).emit()) {
                Horizontal({
                    width(100.percent)
                    marginTop(50.px)
                    // alignItems(AlignItems.Center)
                    justifyContent(JustifyContent.Center)
                    gap(20.px)
                }) {
                    UpdateAuctionButton(
                        storage = storage * bidApplicationIso,
                        auction = auction,
                        texts = buttons * subComp("updateAuction"),
                        dataId = "auction-page.button.configure-auction.explanation",
                        showText = true,
                    )

                    BidArrow(
                        Color.black,
                        Color.black,
                        Color.transparent,
                        {"Next"}

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
                        dataId = "auction-page.button.import-bidders.explanation",
                        showText = true
                    )
                    BidArrow(
                        Color.black,
                        Color.black,
                        Color.transparent,
                        {"Next"}

                    )
                    CreateNewRoundButton(
                        storage = storage * bidApplicationIso,
                        auction = auction,
                        texts = buttons * subComp("createRound"),
                        dataId = "auction-page.button.create-round.explanation",
                        showText = true
                    )
                }
            }
        }

        // List of passed rounds
        if((storage * bidApplicationIso * frozenRounds).emit().isNotEmpty()) {
            val listStyles = AuctionListStyles()
                .modifyHeaderWrapper{
                    justifyContent(JustifyContent.SpaceBetween)
                    alignItems(AlignItems.Center)
                    width(100.percent)
                }
                .modifyListItemWrapper{
                    justifyContent(JustifyContent.SpaceBetween)
                    alignItems(AlignItems.Center)
                    backgroundColor(Color.ghostwhite)
                    width(100.percent)
                    border {
                        style(LineStyle.Solid)
                        color(Color.ghostwhite)
                    }
                    borderWidth(1.px, 1.px, 1.px, 1.px)
                }
            Wrap {
                Vertical(listStyles.wrapper) {
                    // Title
                    // todo:i18n
                    Wrap(listStyles.titleWrapper){H2({/*style { marginLeft(20.px) }*/ }) { Text("Abgeschlossene Runden") }}

                    // Header
                    Horizontal(styles = listStyles.headerWrapper ) {
                        Horizontal(listStyles.header/*{ justifyContent(JustifyContent.FlexStart); width(80.percent) }*/) {
                            // Space for  Check or Xmark
                            // -> HCell
                            // todo:i18n
                            HeaderCell("Runde Nr")

                            // todo:i18n
                            HeaderCell("Start")

                            // todo:i18n
                            HeaderCell("Stop")

                            // todo:i18n
                            HeaderCell("Kommentar"){width(50.percent)}
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

                        Horizontal(styles = listStyles.listItemWrapper ) {
                            Horizontal(listStyles.dataWrapper) {
                                // Check or Xmark ???
                                // todo:i18n runde
                                TextCell("Runde ${round.roundNumber}")
                                // todo:i18n date
                                TimeCell(Date())
                                // todo:i18n date
                                TimeCell(Date(Date.now() + 15 * 60_000))
                                // todo:i18n date
                                TextCell("Kommentar zu Runde ${round.roundNumber}") { width(50.percent) }
                            }
                            Horizontal(listStyles.actionsWrapper) {
                                val isExportDisabled = (storage * bidApplicationIso * user.get).emit()
                                    .isNotGranted(BidRight.BidRound.manage)
                                // todo:i18n
                                DownloadButton(
                                    Color.black,
                                    Color.transparent,
                                    { "Ergebnisse Exportieren" },
                                    storage * bidApplicationIso * deviceData * mediaType.get,
                                    isExportDisabled
                                ) {
                                    TriggerExportOfBidRoundResults(
                                        storage = storage * bidApplicationIso,
                                        auction = auction,
                                        round = round
                                    )
                                }

                                val isEvaluationDisabled = (storage * bidApplicationIso * user.get).emit()
                                    .isNotGranted(BidRight.BidRound.manage)
                                // todo:i18n
                                EvaluationButton(
                                    Color.black,
                                    Color.transparent,
                                    { "Evaluation einsehen" },
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
}
