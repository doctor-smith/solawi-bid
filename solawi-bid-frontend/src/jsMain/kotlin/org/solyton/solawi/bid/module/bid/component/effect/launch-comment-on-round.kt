package org.solyton.solawi.bid.module.bid.component.effect


import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.evoleq.compose.Markup
import org.evoleq.device.data.mediaType
import org.evoleq.language.Lang
import org.evoleq.language.component
import org.evoleq.optics.lens.Lens
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.dispatch
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.module.bid.action.commentOnRound
import org.solyton.solawi.bid.module.bid.component.modal.showCommentOnRoundModal
import org.solyton.solawi.bid.module.bid.data.BidApplication
import org.solyton.solawi.bid.module.bid.data.actions
import org.solyton.solawi.bid.module.bid.data.bidround.Round
import org.solyton.solawi.bid.module.bid.data.deviceData
import org.solyton.solawi.bid.module.bid.data.i18N
import org.solyton.solawi.bid.module.bid.data.modals
import org.solyton.solawi.bid.module.i18n.data.language

@Markup
@Suppress("FunctionName")
fun TriggerCommentOnRoundDialog(
    storage: Storage<BidApplication>,
    round: Lens<BidApplication, Round>,
) = CoroutineScope(Job()).launch{
    showCommentOnRoundDialog(
        storage,
        round
    )
}

fun showCommentOnRoundDialog(
    storage: Storage<BidApplication>,
    round: Lens<BidApplication, Round>
) {
    var comment = ""

    (storage * modals).showCommentOnRoundModal(
        texts = ((storage * i18N * language).read() as Lang.Block).component("solyton.auction.round.commentOnRoundModal"),
        device = (storage * deviceData * mediaType.get),
        setComment = {new: String -> comment = new},
        readComment = {comment},
        isOkButtonDisabled = {false},
        cancel = {}
    ) {
        CoroutineScope(Job()).launch {
            // todo:error-handling
            (storage * actions).dispatch(commentOnRound(comment, round))
        }
    }
}
