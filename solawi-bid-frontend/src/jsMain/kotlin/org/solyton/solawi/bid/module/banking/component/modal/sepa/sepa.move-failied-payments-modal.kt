package org.solyton.solawi.bid.module.banking.component.modal.sepa

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
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
import org.jetbrains.compose.web.css.marginLeft
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.ElementScope
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.application.ui.page.application.style.listItemWrapperStyle
import org.solyton.solawi.bid.module.banking.component.list.OverAllActionData
import org.solyton.solawi.bid.module.banking.component.list.SepaPaymentListItemKey
import org.solyton.solawi.bid.module.list.component.*
import org.solyton.solawi.bid.module.scrollable.Scrollable
import org.solyton.solawi.bid.module.style.modal.commonModalStyles
import org.solyton.solawi.bid.module.style.wrap.Wrap
import org.w3c.dom.HTMLElement


@Markup
@Suppress("FunctionName")
fun MoveFailedPaymentsModal(
    id: Int,
    parentModalId: Int,
    texts: Lang.Block,
    modals: Storage<Modals<Int>>,
    device: Source<DeviceType>,
    data: OverAllActionData,
    setData: (OverAllActionData) -> Unit,
    update: ()->Unit
): @Composable ElementScope<HTMLElement>.()->Unit = Modal(
    type = ModalType.Child<Int>(parentModalId),
    id = id,
    modals = modals,
    device = device,
    onOk = {
        update()
    },
    onCancel = {},
    texts = texts,
    styles = commonModalStyles(device).modifyContainerStyle {
        width(80.percent)
        marginLeft(5.percent)
    },
) {
    val selectedPayments = data.itemsMap.filter {
        it.key in data.visibleItems &&
                data.checkedPayments[it.key.paymentId] == true
    }.values.toList()

    Wrap {
        ListWrapper() {
            TitleWrapper{ Title { H2 { Text("List of failed payments") }}}

            HeaderWrapper {
                Header {
                    HeaderCell("Debtor"){ width(20.percent)}
                    HeaderCell("Amount"){ width(10.percent)}
                    HeaderCell("Failure Reason"){ width(60.percent)}
                }
            }
            Scrollable {
                ListItemsIndexed(selectedPayments) { index, item ->
                    ListItemWrapper({listItemWrapperStyle(index)}) {
                        DataWrapper {
                            TextCell(item.mandate.debtorName) { width(20.percent) }
                            TextCell("${item.payment.amount}") { width(10.percent) }
                            EditableTextCell(item.payment.failureReason ?: "", style = { width(60.percent) }) {
                                val key = SepaPaymentListItemKey(item.payment.sepaPaymentId, item.payment.sepaMandateId)
                                val newMap = data.itemsMap.toMutableMap()
                                newMap[key] = item.copy(payment = item.payment.copy(failureReason = it.ifBlank { null }))
                                setData(data.copy(
                                    itemsMap = newMap,
                                ))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Markup
fun Storage<Modals<Int>>.showMoveFailedPaymentsModal(
    parentModalId: Int,
    texts: Lang.Block,
    device: Source<DeviceType>,
    data: OverAllActionData,
    setData: (OverAllActionData) -> Unit,
    update: ()->Unit
) = with(nextId()) {
    put(this to ModalData(this,
        ModalType.Child<Int>(parentModalId),
        MoveFailedPaymentsModal(
            id = this,
            parentModalId = parentModalId,
            texts = texts,
            modals = this@showMoveFailedPaymentsModal,
            device = device,
            data = data,
            setData = setData,
            update = update
        )
    ) )
}
