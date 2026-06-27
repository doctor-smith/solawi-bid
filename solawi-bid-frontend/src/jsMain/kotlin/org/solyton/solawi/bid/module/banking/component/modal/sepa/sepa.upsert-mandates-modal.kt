package org.solyton.solawi.bid.module.banking.component.modal.sepa

import androidx.compose.runtime.*
import org.evoleq.compose.Markup
import org.evoleq.compose.modal.Modal
import org.evoleq.compose.modal.ModalData
import org.evoleq.compose.modal.ModalType
import org.evoleq.compose.modal.Modals
import org.evoleq.compose.style.data.device.DeviceType
import org.evoleq.device.data.mediaType
import org.evoleq.language.Lang
import org.evoleq.language.texts
import org.evoleq.math.Source
import org.evoleq.math.emit
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.nextId
import org.evoleq.optics.storage.put
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.ElementScope
import org.solyton.solawi.bid.application.ui.page.user.style.listItemWrapperStyle
import org.solyton.solawi.bid.module.banking.component.form.updateSepaMandateFormTexts
import org.solyton.solawi.bid.module.banking.data.application.BankingApplication
import org.solyton.solawi.bid.module.banking.data.application.deviceData
import org.solyton.solawi.bid.module.banking.data.sepa.collection.SepaCollection
import org.solyton.solawi.bid.module.banking.data.sepa.mandate.SepaMandate
import org.solyton.solawi.bid.module.control.button.EditButton
import org.solyton.solawi.bid.module.list.component.*
import org.solyton.solawi.bid.module.scrollable.Scrollable
import org.solyton.solawi.bid.module.scrollable.ScrollableStyles
import org.solyton.solawi.bid.module.style.modal.commonModalStyles
import org.solyton.solawi.bid.module.style.wrap.Wrap
import org.w3c.dom.HTMLElement


@Markup
@Suppress("FunctionName")
fun UpsertSepaMandatesModal(
    id: Int,
    texts: Source<Lang.Block>,
    modals: Storage<Modals<Int>>,
    storage: Storage<BankingApplication>,
    device: Source<DeviceType>,
    sepaCollections: List<SepaCollection>,
    sepaMandates: List<SepaMandate>,
    setSepaMandates: (List<SepaMandate>) -> Unit,
    update: ()->Unit
): @Composable ElementScope<HTMLElement>.()->Unit = Modal(
    type = ModalType.Dialog,
    id = id,
    modals = modals,
    device = storage * deviceData * mediaType.get,
    onOk = {
        update()
    },
    onCancel = {},
    texts = texts.emit(),
    styles = commonModalStyles(device),
) {
    Wrap {

        ListWrapper {
            HeaderWrapper {
                Header{
                    HeaderCell("Mandate Reference") {width(25.percent)}
                    HeaderCell("Status") {width(10.percent)}
                    HeaderCell("Signed At") {width(10.percent)}
                    HeaderCell("Amendment Of") {width(25.percent)}
                    HeaderCell("Collections - Key / Ref Prefix") {width(30.percent)}
                }
            }
            Scrollable(ScrollableStyles
                .modifyContainerStyle { height(40.vh) }
                .modifyContentStyle { width(100.percent) }
            ) {
                ListItemsIndexed(sepaMandates) { index, item ->
                    var itemState by remember { mutableStateOf(item) }
                    val amendmentOf = itemState.amendmentOf?.let{
                        sepaMandates.find { it.sepaMandateId == it.amendmentOf }?.mandateReference?.value
                    }?: "--"

                    val collections = sepaCollections.filter { it.sepaMandates.map { mandate -> mandate.sepaMandateId }.any{ id -> id == item.sepaMandateId } }
                    ListItemWrapper(styles = { listItemWrapperStyle(index) }) {
                        DataWrapper {
                            TextCell(itemState.mandateReference.value) {width(25.percent)}
                            TextCell(itemState.status.name) {width(10.percent)}
                            TextCell(itemState.signedAt.date.toString()) {width(10.percent)}
                            TextCell(amendmentOf) {width(25.percent)}
                            TextCell(collections.joinToString(", ") { "${it.collectionKey.value} / ${it.mandateReferencePrefix.value}" }) {width(30.percent)}
                        }
                        ActionsWrapper {
                            EditButton(
                                color = Color.black,
                                bgColor = Color.white,
                                deviceType = device,
                            ) {
                                modals.showUpsertSepaMandateModal(
                                    id,
                                    storage,
                                    texts,
                                    device,
                                    itemState,
                                    {item ->
                                        itemState = item
                                        /*
                                        setSepaMandates(sepaMandates.map {
                                            if (item.sepaMandateId == it.sepaMandateId) item else it
                                        })

                                         */
                                    },
                                    {
                                        setSepaMandates(sepaMandates.map {
                                            if (item.sepaMandateId == it.sepaMandateId) item else it
                                        })
                                    }
                                )
                            }
                        }
                    }
                }
            }

        }
    }
}

@Markup
fun Storage<Modals<Int>>.showUpsertSepaMandatesModal(
    storage: Storage<BankingApplication>,
    texts: Source<Lang.Block>,
    device: Source<DeviceType>,
    sepaCollections: List<SepaCollection>,
    sepaMandates: List<SepaMandate>,
    setSepaMandates: (List<SepaMandate>) -> Unit,
    update: () -> Unit
) = with(nextId()) {
    put(this to ModalData(this,
        ModalType.Dialog,
        UpsertSepaMandatesModal(
            this,
            texts,
            this@showUpsertSepaMandatesModal,
            storage,
            device,
            sepaCollections,
            sepaMandates,
            setSepaMandates,
            update = update
        )
    ) )
}

val upsertSepaMandatesModalTexts = Source {
    "dialog" texts {
        "title" colon "Create or Update SEPA mandates"
        "okButton" block {
            "title" colon "Ok"
        }
        "cancelButton" block {
            "title" colon "Cancel"
        }
        add(updateSepaMandateFormTexts.emit())
    }
}
