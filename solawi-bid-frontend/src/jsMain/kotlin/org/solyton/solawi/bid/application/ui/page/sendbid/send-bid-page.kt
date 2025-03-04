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
import org.jetbrains.compose.web.css.flexGrow
import org.jetbrains.compose.web.dom.Div
import org.solyton.solawi.bid.application.data.*
import org.solyton.solawi.bid.application.data.device.mediaType
import org.solyton.solawi.bid.application.ui.effect.LaunchSetDeviceData
import org.solyton.solawi.bid.application.ui.style.form.formPageDesktopStyle
import org.solyton.solawi.bid.application.ui.style.wrap.Wrap
import org.solyton.solawi.bid.module.bid.action.sendBidAction
import org.solyton.solawi.bid.module.bid.component.form.SendBidForm
import org.solyton.solawi.bid.module.bid.component.modal.showSuccessfulBidInformationModal
import org.solyton.solawi.bid.module.bid.data.Bid
import org.solyton.solawi.bid.module.bid.data.api.ApiBid
import org.solyton.solawi.bid.module.bid.data.showSuccessMessage
import org.solyton.solawi.bid.module.control.button.StdButton
import org.solyton.solawi.bid.module.i18n.data.language

@Markup
@Composable
@Suppress("FunctionName")
fun SendBidPage(storage: Storage<Application>, link: String) = Div(attrs = {style { formPageDesktopStyle() }}) {
    val round = bidRounds * FirstOrNull { it.round.link == link }
    val roundLens = bidRounds * FirstBy { it.round.link == link }
    val showSuccessMessageModal = storage * round  map {
        when{
            it == null -> false
            else -> it.showSuccessMessage
        }
    }
     if(showSuccessMessageModal.read()) LaunchedEffect(Unit) {
         val modals = (storage * modals)
         val texts = ((storage * i18N * language).read() as Lang.Block).component("solyton.auction.round.successfulBidInformationModal")

         modals.showSuccessfulBidInformationModal(
            storage = storage,
            round = roundLens,
            texts = texts,
            device = (storage * deviceData * mediaType.get),
            cancel = { (storage * roundLens * showSuccessMessage ).write(false) },
            update = { (storage * roundLens * showSuccessMessage ).write(false) }
        )
    }
    LaunchSetDeviceData(storage * deviceData)
    Vertical() {
        SendBidForm((storage * deviceData * mediaType).read()) {
            CoroutineScope(Job()).launch {
                (storage * actions).read().emit(
                    sendBidAction((it to link).toApiType())
                )
            }
        }
        Div({ style { flexGrow(1) } }) {}
        Wrap {
            StdButton({ "QR Code" }, storage * deviceData * mediaType.get) {
                navigate("/bid/qr-code/$link")
            }
        }
    }
}

fun Pair<Bid, String>.toApiType(): ApiBid = ApiBid(first.username, second, first.amount)