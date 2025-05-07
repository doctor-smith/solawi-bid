package org.solyton.solawi.bid.module.bid.component.modal

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.evoleq.compose.layout.Property
import org.evoleq.compose.layout.ReadOnlyProperty
import org.evoleq.compose.layout.Vertical
import org.evoleq.compose.modal.Modal
import org.evoleq.compose.modal.ModalData
import org.evoleq.compose.modal.ModalType
import org.evoleq.compose.modal.Modals
import org.evoleq.language.Lang
import org.evoleq.language.get
import org.evoleq.math.Source
import org.evoleq.optics.lens.Lens
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.nextId
import org.evoleq.optics.storage.put
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.dom.ElementScope
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.device.DeviceType
import org.solyton.solawi.bid.application.data.device.mediaType
import org.solyton.solawi.bid.application.data.deviceData
import org.solyton.solawi.bid.module.bid.component.styles.auctionModalStyles
import org.solyton.solawi.bid.module.bid.data.BidRound
import org.w3c.dom.HTMLElement

@Markup
@Suppress("FunctionName")
fun SuccessfulBidInformationModal(
    id: Int,
    texts: Lang.Block,
    modals: Storage<Modals<Int>>,
    storage: Storage<Application>,
    round: Lens<Application, BidRound>,
    device: Source<DeviceType>,
    // cancel: ()->Unit,
    update: ()->Unit
): @Composable ElementScope<HTMLElement>.()->Unit = Modal(
    id,
    modals,
    storage * deviceData * mediaType.get,
    onOk = {
        update()
    },
    onCancel = null,
    texts = texts,
    styles = auctionModalStyles(device),
) {
    val bid = (storage * round).read()
    P{Text(texts["message"])}
    Vertical {
        ReadOnlyProperty(Property(texts["amount"], bid.bidAmount!!))
        ReadOnlyProperty(Property(texts["numberOfShares"], bid.numberOfShares!!))
        ReadOnlyProperty(Property(texts["totalAmountPerMonth"], bid.numberOfShares * bid.bidAmount))
    }
}

@Markup
fun Storage<Modals<Int>>.showSuccessfulBidInformationModal(
    storage: Storage<Application>,
    round: Lens<Application, BidRound>,
    texts: Lang.Block,
    device: Source<DeviceType>,
    update: ()->Unit
) = with(nextId()) {
    put(this to ModalData(
        ModalType.Dialog,
        SuccessfulBidInformationModal(
            this,
            texts,
            this@showSuccessfulBidInformationModal,
            storage,
            round,
            device,
            update = update
        )
    ) )
}