package org.solyton.solawi.bid.module.bid.component.modal

import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import org.evoleq.compose.Markup
import org.evoleq.compose.dnd.Dropzone
import org.evoleq.compose.dnd.readFileContent
import org.evoleq.compose.modal.Modal
import org.evoleq.compose.modal.ModalData
import org.evoleq.compose.modal.ModalType
import org.evoleq.compose.modal.Modals
import org.evoleq.compose.style.data.device.DeviceType
import org.evoleq.csv.parseCsv
import org.evoleq.language.Lang
import org.evoleq.math.Source
import org.evoleq.optics.storage.Action
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.nextId
import org.evoleq.optics.storage.put
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.name
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.ElementScope
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.letsPlot.commons.values.Colors
import org.solyton.solawi.bid.module.banking.data.FiscalYearId
import org.solyton.solawi.bid.module.bid.action.importBiddersFromOrganization
import org.solyton.solawi.bid.module.bid.component.styles.auctionModalStyles
import org.solyton.solawi.bid.module.bid.data.BidApplication
import org.solyton.solawi.bid.module.bid.data.api.AddBidders
import org.solyton.solawi.bid.module.bid.data.api.ApiAuction
import org.solyton.solawi.bid.module.bid.data.api.BidderData
import org.solyton.solawi.bid.module.bid.data.api.ImportBiddersFromOrganization
import org.solyton.solawi.bid.module.bid.data.api.NewBidder
import org.solyton.solawi.bid.module.bid.data.auction.auctionId
import org.solyton.solawi.bid.module.bid.data.values.AuctionId
import org.solyton.solawi.bid.module.control.button.UploadButton
import org.solyton.solawi.bid.module.values.ProviderId
import org.w3c.dom.HTMLElement

enum class ImportBiddersSource {
    CSV,
    ORGANIZATION_MEMBERS
}

@Markup
@Suppress("FunctionName")
fun ImportBiddersModal(
    id: Int,
    texts: Lang.Block,
    modals: Storage<Modals<Int>>,
    device: Source<DeviceType>,
    dispatch: suspend (Action<BidApplication, ImportBiddersFromOrganization, ApiAuction>)->Unit,
    organizationId: ProviderId,
    auctionId: AuctionId,
    // fiscalYearId: FiscalYearId,
    setBidders: (List<NewBidder>)->Unit,
    addBidders: (AddBidders)->Unit,
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
    var isProcessingFileContent by remember { mutableStateOf(false) }
    var importBiddersSource by remember { mutableStateOf(ImportBiddersSource.CSV) }

    Div {
        ImportBiddersSource.entries.forEach { source ->
            Input(type = InputType.Radio) {
                name("import-bidders-source")
                value(source.name)
                checked(importBiddersSource == source)
                onChange { importBiddersSource = source }
            }
            Span { Text(source.name) }
        }
    }
    if (importBiddersSource == ImportBiddersSource.CSV) {
        Dropzone(
            onProcessingStarted = { isProcessingFileContent = true },
            onProcessingStopped = { isProcessingFileContent = true },
        ) { files ->
            files.filter { it.name.endsWith("csv") }.map {
                readFileContent(it) { content ->

                    val parsed = parseCsv(content)

                    setBidders(parsed.map {
                        NewBidder(it["Email"]!!, 0, it["Anteile"]!!.toInt())
                    })
                    if (!parsed.hasData(
                            "Vorname",
                            "Nachname",
                            "Email",
                            "Anteile",
                            "Eier-Anteile",
                            "Emails",
                            "Data"
                        )
                    ) return@readFileContent
                    // todo:i18n
                    addBidders(AddBidders(parsed.map {
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
    } else {
        val scope = rememberCoroutineScope()
        UploadButton(
            color = Color.black,
            bgColor = Color.white,
            {"Import bidders from organization"},
            device,
            false,
        ) {
            scope.launch {
                dispatch(importBiddersFromOrganization(
                    ImportBiddersFromOrganization(
                        organizationId,
                        auctionId = auctionId
                    )
                ))
            }
        }
    }
}


fun List<Map<String, String>>.hasData(vararg keys: String): Boolean = all {
    keys.all { key ->
        it.containsKey(key)
    }
}

@Markup
fun Storage<Modals<Int>>.showImportBiddersModal(
    texts: Lang.Block,
    device: Source<DeviceType>,
    dispatch: suspend (Action<BidApplication, ImportBiddersFromOrganization, ApiAuction>)->Unit,
    organizationId: ProviderId,
    auctionId: AuctionId,
    setBidders: (List<NewBidder>)->Unit,
    addBidders: (AddBidders)->Unit,
    isOkButtonDisabled: ()->Boolean,
    cancel: ()->Unit,
    update: ()->Unit
) = with(nextId()) {
    put(this to ModalData(
        ModalType.Dialog,
        ImportBiddersModal(
            this,
            texts,
            this@showImportBiddersModal,
            device,
            dispatch,
            organizationId,
            auctionId,
            setBidders,
            addBidders,
            isOkButtonDisabled,
            cancel,
            update
        )
    ) )
}
