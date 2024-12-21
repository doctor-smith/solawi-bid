package org.solyton.solawi.bid.module.bid.component

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.evoleq.compose.date.format
import org.evoleq.compose.date.parse
import org.evoleq.compose.label.Label
import org.evoleq.compose.modal.Modal
import org.evoleq.compose.modal.ModalData
import org.evoleq.compose.modal.ModalType
import org.evoleq.compose.modal.Modals
import org.evoleq.language.Lang
import org.evoleq.language.Locale
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.nextId
import org.evoleq.optics.storage.put
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.dom.DateInput
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.ElementScope
import org.jetbrains.compose.web.dom.TextInput
import org.solyton.solawi.bid.application.ui.style.form.*
import org.solyton.solawi.bid.module.bid.data.Auction
import org.solyton.solawi.bid.module.bid.data.date
import org.solyton.solawi.bid.module.bid.data.name
import org.w3c.dom.HTMLElement

const val DEFAULT_AUCTION_ID = "DEFAULT_AUCTION_ID"

@Markup
@Suppress("FunctionName")
fun AuctionModal(
    id: Int,
    texts: Lang.Block,
    modals: Storage<Modals<Int>>,
    auction: Storage<Auction>,
    cancel: ()->Unit,
    update: ()->Unit
): @Composable ElementScope<HTMLElement>.()->Unit = Modal(
    id,
    modals,
    onOk = {
        update()
    },
    onCancel = {
        cancel()
    },
    texts = texts
) {

    Div(attrs = {style { formStyle() }}) {

        Div(attrs = {style { fieldStyle() }}) {
            Label("Name", id = "name" , labelStyle = formLabelStyle)
            TextInput((auction * name).read()) {
                id("name")
                style { textInputStyle() }
                onInput { (auction * name).write(it.value) }
            }
        }
        Div(attrs = {style { fieldStyle() }}) {
            Label("Datum", id = "date" , labelStyle = formLabelStyle)
            DateInput((auction * date).read().format(Locale.Iso)) {
                id("date")
                style { dateInputStyle() }
                onInput {
                    (auction * date).write(it.value.parse(Locale.Iso))
                }
            }
        }
    }
}

@Markup
fun Storage<Modals<Int>>.showAuctionModal(
    auction: Storage<Auction>,
    texts: Lang.Block,
    cancel: ()->Unit,
    update: ()->Unit
) = with(nextId()) {
    put(this to ModalData(
        ModalType.Dialog,
        AuctionModal(
            this,
            texts,
            this@showAuctionModal,
            auction,
            cancel,
            update
        )
    ))
}
