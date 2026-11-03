package org.solyton.solawi.bid.module.bid.component.button

import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import org.evoleq.compose.Markup
import org.evoleq.device.data.mediaType
import org.evoleq.language.Lang
import org.evoleq.language.component
import org.evoleq.language.text
import org.evoleq.language.tooltip
import org.evoleq.math.Reader
import org.evoleq.math.Source
import org.evoleq.math.emit
import org.evoleq.math.times
import org.evoleq.optics.lens.Lens
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.css.Color
import org.solyton.solawi.bid.module.bid.action.configureAuction
import org.solyton.solawi.bid.module.bid.component.form.showUpdateAuctionModal
import org.solyton.solawi.bid.module.bid.data.*
import org.solyton.solawi.bid.module.bid.data.api.AuctionDetails
import org.solyton.solawi.bid.module.bid.data.api.ConfigureAuction
import org.solyton.solawi.bid.module.bid.data.auction.Auction
import org.solyton.solawi.bid.module.bid.data.auction.rounds
import org.solyton.solawi.bid.module.bid.data.biduser.organizations
import org.solyton.solawi.bid.module.bid.data.reader.auctionAccepted
import org.solyton.solawi.bid.module.bid.data.reader.existRounds
import org.solyton.solawi.bid.module.bid.permission.BidRight
import org.solyton.solawi.bid.module.bid.service.isNotGranted
import org.solyton.solawi.bid.module.control.button.GearButton
import org.solyton.solawi.bid.module.control.button.GearButtonWithText
import org.solyton.solawi.bid.module.error.component.showErrorModal
import org.solyton.solawi.bid.module.error.lang.errorModalTexts
import org.solyton.solawi.bid.module.i18n.data.language
import org.solyton.solawi.bid.module.style.data.Side

@Markup
@Composable
@Suppress("FunctionName")
fun UpdateAuctionButton(
    storage: Storage<BidApplication>,
    auction: Lens<BidApplication, Auction>,
    applicationId: Source<String>,
    texts: Reader<Unit, Lang.Block>,
    dataId: String,
    showText: Boolean = false
) {
    val scope = rememberCoroutineScope()

    var auctionState by remember { mutableStateOf<Auction>((storage * auction).read() ) }
    val auctionStorage = Storage<Auction>(
        read = {auctionState},
        write = { auctionState = it }
    )

    // Auction can only be configured, if no rounds have been created
    val isDisabled = (auctionStorage * rounds * existRounds).emit() ||
        (auctionStorage * auctionAccepted).emit()||
        (storage * user.get).emit().isNotGranted(BidRight.Auction.update)



    val action: () -> Unit = {
        (storage * modals).showUpdateAuctionModal(
            auction =  auctionStorage,
            organizations = storage * user * organizations.get,
            organizationApplicationContextRelations = storage * applicationOrganizationRelations.get,
            applicationId = applicationId,
            texts = ((storage * i18N * language).read() as Lang.Block).component("solyton.auction.updateDialog"),
            device = storage * deviceData * mediaType.get,
            cancel = {}
        ) {
            val a = auctionStorage.read()
            val data = ConfigureAuction(
                a.auctionId,
                a.name,
                a.contextId,
                a.date,
                AuctionDetails.SolawiTuebingen(
                    a.auctionDetails.minimalBid ?: 0.0,
                    a.auctionDetails.benchmark ?: 0.0,
                    a.auctionDetails.targetAmount ?: 0.0,
                    a.auctionDetails.solidarityContribution ?: 0.0
                )
            )

            scope.launch {
                val action = configureAuction(data)
                val actions = (storage * actions).read()
                try {
                    actions.dispatch( action )
                } catch(exception: Exception) {
                    (storage * modals).showErrorModal(
                        errorModalTexts(exception.message?:exception.cause?.message?:"Cannot Emit action '${action.name}'"),
                        storage * deviceData * mediaType.get

                    )
                }
            }
        }
    }
    when(showText) {
        true -> GearButtonWithText(
            Color.black,
            Color.transparent,
            texts * text,
            texts * tooltip,
            Side.Right,
            storage * deviceData * mediaType.get,
            isDisabled,
            dataId = dataId
        ) {
            action()
        }
        false -> GearButton(
            Color.black,
            Color.transparent,
            texts * text,
            storage * deviceData * mediaType.get,
            isDisabled,
            dataId = dataId
        ) {
            action()
        }
    }
}
