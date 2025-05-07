package org.solyton.solawi.bid.module.bid.component.modal

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.evoleq.compose.modal.*
import org.evoleq.language.Lang
import org.evoleq.math.Source
import org.evoleq.optics.lens.Lens
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.nextId
import org.evoleq.optics.storage.put
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.vh
import org.jetbrains.compose.web.dom.ElementScope
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.device.DeviceType
import org.solyton.solawi.bid.application.data.device.mediaType
import org.solyton.solawi.bid.application.data.deviceData
import org.solyton.solawi.bid.module.bid.component.BidRoundEvaluation
import org.solyton.solawi.bid.module.bid.component.styles.auctionModalStyles
import org.solyton.solawi.bid.module.bid.data.Round
import org.w3c.dom.HTMLElement

@Markup
@Suppress("FunctionName")
fun BidRoundEvaluationModal(
    id: Int,
    texts: Lang.Block,
    modals: Storage<Modals<Int>>,
    storage: Storage<Application>,
    round: Lens<Application, Round>,
    device: Source<DeviceType>,
    cancel: (()->Unit)?,
    update: ()->Unit
): @Composable ElementScope<HTMLElement>.()->Unit = Modal(
    id,
    modals,
    storage * deviceData * mediaType.get,
    onOk = {
        update()
    },
    onCancel = cancel,
    texts = texts,
    styles = auctionModalStyles(device),

) {
    // todo:i18n
    Text("Evaluation of Bid Round")
    BidRoundEvaluation(
        storage = storage,
        round = round
    )
}

@Markup
fun Storage<Modals<Int>>.showBidRoundEvaluationModal(
    storage: Storage<Application>,
    round: Lens<Application, Round>,
    texts: Lang.Block,
    device: Source<DeviceType>,
    cancel: (()->Unit)?,
    update: ()->Unit
) = with(nextId()) {
    put(this to ModalData(
        ModalType.Dialog,
        BidRoundEvaluationModal(
            this,
            texts,
            this@showBidRoundEvaluationModal,
            storage,
            round,
            device,
            cancel,
            update
        )
    ) )
}
