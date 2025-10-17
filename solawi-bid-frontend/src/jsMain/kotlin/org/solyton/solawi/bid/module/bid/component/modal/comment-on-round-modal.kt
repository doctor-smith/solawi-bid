package org.solyton.solawi.bid.module.bid.component.modal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.evoleq.compose.Markup
import org.evoleq.compose.label.Label
import org.evoleq.compose.modal.Modal
import org.evoleq.compose.modal.ModalData
import org.evoleq.compose.modal.ModalType
import org.evoleq.compose.modal.Modals
import org.evoleq.compose.style.data.device.DeviceType
import org.evoleq.language.Lang
import org.evoleq.math.Source
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.nextId
import org.evoleq.optics.storage.put
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.ElementScope
import org.jetbrains.compose.web.dom.TextInput
import org.solyton.solawi.bid.module.bid.component.styles.auctionModalStyles
import org.solyton.solawi.bid.module.style.form.fieldDesktopStyle
import org.solyton.solawi.bid.module.style.form.formLabelDesktopStyle
import org.solyton.solawi.bid.module.style.form.textInputDesktopStyle
import org.w3c.dom.HTMLElement

@Markup
@Suppress("FunctionName")
fun CommentOnRoundModal(
    id: Int,
    texts: Lang.Block,
    modals: Storage<Modals<Int>>,
    device: Source<DeviceType>,
    setComment: (String) -> Unit,
    readComment: () -> String,
    isOkButtonDisabled: ()->Boolean,
    cancel: ()->Unit,
    update: ()->Unit
): @Composable ElementScope<HTMLElement>.()->Unit = Modal(
    id,
    modals,
    device,
    onOk = {
        update()
    },
    onCancel = {
        cancel()
    },
    isOkButtonDisabled = isOkButtonDisabled,
    texts = texts,
    styles = auctionModalStyles(device),
) {
    Div(attrs = {style { fieldDesktopStyle() }}) {
        // state
        var comment by remember { mutableStateOf(readComment()) }

        Label("Kommentar", id = "comment" , labelStyle = formLabelDesktopStyle)
        TextInput(comment) {
            id("comment")
            style { textInputDesktopStyle() }
            onInput {
                comment = it.value
                setComment(comment)
            }
        }
    }
}



@Markup
fun Storage<Modals<Int>>.showCommentOnRoundModal(
    texts: Lang.Block,
    device: Source<DeviceType>,
    setComment: (String)->Unit,
    readComment: () -> String,
    isOkButtonDisabled: ()->Boolean,
    cancel: ()->Unit,
    update: ()->Unit
) = with(nextId()) {
    put(this to ModalData(
        ModalType.Dialog,
        CommentOnRoundModal(
            this,
            texts,
            this@showCommentOnRoundModal,
            device,
            setComment,
            readComment,
            isOkButtonDisabled,
            cancel,
            update
        )
    ) )
}
