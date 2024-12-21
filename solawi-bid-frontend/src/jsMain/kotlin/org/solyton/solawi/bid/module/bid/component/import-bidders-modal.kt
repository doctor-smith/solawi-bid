package org.solyton.solawi.bid.module.bid.component

import androidx.compose.runtime.*
import org.evoleq.compose.Markup
import org.evoleq.compose.dnd.Dropzone
import org.evoleq.compose.dnd.readFileContent
import org.evoleq.compose.modal.Modal
import org.evoleq.compose.modal.ModalData
import org.evoleq.compose.modal.ModalType
import org.evoleq.compose.modal.Modals
import org.evoleq.csv.parseCsv
import org.evoleq.language.Lang
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.nextId
import org.evoleq.optics.storage.put
import org.jetbrains.compose.web.dom.ElementScope
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.module.bid.data.Auction
import org.solyton.solawi.bid.module.bid.data.api.NewBidder
import org.w3c.dom.HTMLElement

@Markup
@Suppress("FunctionName")
fun ImportBiddersModal(
    id: Int,
    texts: Lang.Block,
    modals: Storage<Modals<Int>>,
    auction: Storage<Auction>,
    setBidders: (List<NewBidder>)->Unit,
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



    H2 { Text("Drop files here") }

    Dropzone { files ->
        files.filter { it.name.endsWith("csv") }.map {
            readFileContent(it) { content ->
                setBidders(parseCsv(content).map {
                    NewBidder(it["Email"]!!,0, it["Anteile"]!!.toInt())
                })

            }
        }
    }

}

@Markup
fun Storage<Modals<Int>>.showImportBiddersModal(
    auction: Storage<Auction>,
    texts: Lang.Block,
    setBidders: (List<NewBidder>)->Unit,
    cancel: ()->Unit,
    update: ()->Unit
) = with(nextId()) {
    put(this to ModalData(
        ModalType.Dialog,
        ImportBiddersModal(
            this,
            texts,
            this@showImportBiddersModal,
            auction,
            setBidders,
            cancel,
            update
        )
    )
    )
}