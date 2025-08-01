package org.solyton.solawi.bid.application.ui.page.sendbid

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.evoleq.compose.Markup
import org.evoleq.compose.layout.Vertical
import org.evoleq.compose.routing.navigate
import org.evoleq.language.Lang
import org.evoleq.language.component
import org.evoleq.math.FirstOrNull
import org.evoleq.math.map
import org.evoleq.math.read
import org.evoleq.optics.lens.FirstBy
import org.evoleq.optics.lens.times
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.evoleq.device.data.mediaType
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.transform.bid.bidApplicationIso
import org.solyton.solawi.bid.application.ui.effect.LaunchSetDeviceData
import org.solyton.solawi.bid.module.style.form.formPageDesktopStyle
import org.solyton.solawi.bid.module.style.page.verticalPageStyle
import org.solyton.solawi.bid.module.style.wrap.Wrap
import org.solyton.solawi.bid.module.bid.action.sendBidAction
import org.solyton.solawi.bid.module.bid.component.form.SendBidForm
import org.solyton.solawi.bid.module.bid.component.modal.showSuccessfulBidInformationModal
import org.solyton.solawi.bid.module.bid.data.actions
import org.solyton.solawi.bid.module.bid.data.bidround.Bid
import org.solyton.solawi.bid.module.bid.data.api.ApiBid
import org.solyton.solawi.bid.module.bid.data.bidRounds
import org.solyton.solawi.bid.module.bid.data.bidround.showSuccessMessage
import org.solyton.solawi.bid.module.bid.data.deviceData
import org.solyton.solawi.bid.module.bid.data.i18N
import org.solyton.solawi.bid.module.bid.data.modals
import org.solyton.solawi.bid.module.control.button.StdButton
import org.solyton.solawi.bid.module.i18n.data.language

@Markup
@Composable
@Suppress("FunctionName")
fun SendBidPage(storage: Storage<Application>, link: String) = Div/*(attrs = {style { formPageDesktopStyle() }})*/ {
    val round = bidRounds * FirstOrNull { it.round.link == link }
    val roundLens = bidRounds * FirstBy { it.round.link == link }
    val showSuccessMessageModal = storage * bidApplicationIso * round  map {
        when{
            it == null -> false
            else -> it.showSuccessMessage
        }
    }
     if(showSuccessMessageModal.read()) LaunchedEffect(Unit) {
         val modals = (storage * bidApplicationIso * modals)
         val texts = ((storage * bidApplicationIso * i18N * language).read() as Lang.Block).component("solyton.auction.round.successfulBidInformationModal")

         modals.showSuccessfulBidInformationModal(
            storage = storage * bidApplicationIso,
            round = roundLens,
            texts = texts,
            device = (storage * bidApplicationIso * deviceData * mediaType.get),
            update = { (storage * bidApplicationIso * roundLens * showSuccessMessage ).write(false) }
        )
    }
    LaunchSetDeviceData(storage * bidApplicationIso * deviceData)
    Vertical(style = {
        verticalPageStyle()
        formPageDesktopStyle()
        height(100.vh)
        paddingBottom(10.px)
    }) {
        SendBidForm((storage * bidApplicationIso * deviceData * mediaType).read()) {
            CoroutineScope(Job()).launch {
                (storage * bidApplicationIso * actions).read().dispatch(
                    sendBidAction((it to link).toApiType())
                )
            }
        }
        Div({ style { flexGrow(1) } }) {}
        Wrap {
            StdButton({ "QR Code" }, storage * bidApplicationIso * deviceData * mediaType.get) {
                navigate("/bid/qr-code/$link")
            }
        }
    }
}

fun Pair<Bid, String>.toApiType(): ApiBid = ApiBid(first.username, second, first.amount)
