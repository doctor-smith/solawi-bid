package org.solyton.solawi.bid.module.bid.component.modal

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
import org.evoleq.math.Source
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.nextId
import org.evoleq.optics.storage.put
import org.jetbrains.compose.web.dom.ElementScope
import org.solyton.solawi.bid.application.data.device.DeviceType
import org.solyton.solawi.bid.module.bid.component.styles.auctionModalStyles
import org.solyton.solawi.bid.module.bid.data.Auction
import org.solyton.solawi.bid.module.bid.data.api.AddBidders
import org.solyton.solawi.bid.module.bid.data.api.BidderData
import org.solyton.solawi.bid.module.bid.data.api.NewBidder
import org.w3c.dom.HTMLElement

@Markup
@Suppress("FunctionName")
fun ImportBiddersModal(
    id: Int,
    texts: Lang.Block,
    modals: Storage<Modals<Int>>,
    auction: Storage<Auction>,
    device: Source<DeviceType>,
    setBidders: (List<NewBidder>)->Unit,
    addBidders: (AddBidders)->Unit,
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
        texts = texts,
        styles = auctionModalStyles(device),
    ) {
        var isProcessingFileContent by remember{ mutableStateOf(false) }

        Dropzone(
            onProcessingStarted = {isProcessingFileContent = true},
            onProcessingStopped = {isProcessingFileContent = true},
        ) { files ->
            files.filter { it.name.endsWith("csv") }.map {
                readFileContent(it) { content ->

                    val parsed = parseCsv(content)

                    setBidders(parsed.map {
                        NewBidder(it["Email"]!!,0, it["Anteile"]!!.toInt())
                    })
                    addBidders(AddBidders(parsed.map{
                        BidderData(
                            it["Vorname"]!!,
                            it["Nachname"]!!,
                            it["Email"]!!,
                            it["Anteile"]!!.toInt(),
                            it["Eier-Anteile"]!!.toInt(),
                            it["Emails"]!!.split(",").map { it.trim() },
                            it["Data"]!!.split(",").map { it.trim() },
                        )
                    }))
                }
            }
        }

    }



@Markup
fun Storage<Modals<Int>>.showImportBiddersModal(
    auction: Storage<Auction>,
    texts: Lang.Block,
    device: Source<DeviceType>,
    setBidders: (List<NewBidder>)->Unit,
    addBidders: (AddBidders)->Unit,
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
            device,
            setBidders,
            addBidders,
            cancel,
            update
        )
    )
    )
}