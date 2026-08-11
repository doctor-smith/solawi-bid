package org.solyton.solawi.bid.module.banking.component.list.sepa

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.evoleq.compose.Markup
import org.evoleq.compose.date.format
import org.evoleq.device.data.mediaType
import org.evoleq.language.Locale
import org.evoleq.math.*
import org.evoleq.optics.storage.Read
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.dispatch
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.css.*
import org.solyton.solawi.bid.application.ui.page.user.style.listItemWrapperStyle
import org.solyton.solawi.bid.module.banking.action.sepa.downloadSepaMessage
import org.solyton.solawi.bid.module.banking.action.sepa.mergeSepaMessages
import org.solyton.solawi.bid.module.banking.action.sepa.updateSepaMessagesStatus
import org.solyton.solawi.bid.module.banking.component.form.sepa.MergeSepaMessagesData
import org.solyton.solawi.bid.module.banking.component.modal.sepa.mergeSepaMessagesModalTexts
import org.solyton.solawi.bid.module.banking.component.modal.sepa.showMergeSepaMessagesModal
import org.solyton.solawi.bid.module.banking.data.SepaMessageId
import org.solyton.solawi.bid.module.banking.data.application.BankingApplication
import org.solyton.solawi.bid.module.banking.data.application.deviceData
import org.solyton.solawi.bid.module.banking.data.bankingApplicationActions
import org.solyton.solawi.bid.module.banking.data.bankingApplicationModals
import org.solyton.solawi.bid.module.banking.data.internal.Currency
import org.solyton.solawi.bid.module.banking.data.sepa.collection.SepaCollection
import org.solyton.solawi.bid.module.banking.data.sepa.message.SepaMessage
import org.solyton.solawi.bid.module.banking.data.sepa.message.SepaMessageStatus
import org.solyton.solawi.bid.module.control.button.*
import org.solyton.solawi.bid.module.list.component.*
import org.solyton.solawi.bid.module.list.style.ListStyles
import org.solyton.solawi.bid.module.scrollable.Scrollable


data class SepaMessagesFilter(
    val status: (SepaMessageStatus) -> Boolean = { true },
    val executionDate: (LocalDate) -> Boolean = { true }
) {
    infix fun applyTo(message: SepaMessage) =
        status(message.status) &&
        executionDate(message.executionDate)
}
data class SepaMessagesOrder(
    val status: SortOrder = SortOrder.NONE,
    val executionDate: SortOrder = SortOrder.DESC,
    val number: SortOrder = SortOrder.NONE,
    val amount: SortOrder = SortOrder.NONE,
    val remittanceInformation: SortOrder = SortOrder.NONE
) {
    fun toComparator(): Comparator<SepaMessage> = Comparator { o1, o2 ->
        var result = 0

        if (result == 0 && status != SortOrder.NONE) {
            result = compareValues(o1.status.name, o2.status.name)
            if (status == SortOrder.DESC) result = -result
        }
        if (result == 0 && executionDate != SortOrder.NONE) {
            result = compareValues(o1.executionDate, o2.executionDate)
            if (executionDate == SortOrder.DESC) result = -result
        }
        if(result == 0 && number != SortOrder.NONE) {
            result = compareValues(o1.numberOfPayments, o2.numberOfPayments)
            if (number == SortOrder.DESC) result = -result
        }
        if(result == 0 && amount != SortOrder.NONE) {
            result = compareValues(o1.totalAmount, o2.totalAmount)
            if (amount == SortOrder.DESC) result = -result
        }
        if(result == 0 && remittanceInformation != SortOrder.NONE) {
            result = compareValues(o1.remittanceInformation.value, o2.remittanceInformation.value)
            if (remittanceInformation == SortOrder.DESC) result = -result
        }
        result
    }
}
fun List<SepaMessage>.sortedBy(order: SepaMessagesOrder): List<SepaMessage> {
    return sortedWith(order.toComparator())
}
@Markup
@Composable
fun SepaMessageList(
    storage: Storage<BankingApplication>,
    collection: Source<SepaCollection>,
    messages: Source<List<SepaMessage>>,
    styles: ListStyles = ListStyles(),
    modalId: Int,
) {
    val scope = rememberCoroutineScope()
    val messageIdentifiers = collection.map { collection ->
        collection.sepaPayments.mapNotNull { payment -> payment.messageIdentifier }
    }
    val messagesOfCollection = messages map {
        list -> list
            .filter { message -> message.messageIdentifier in messageIdentifiers }
            .sortedByDescending { it.executionDate }
    }

    var filteredMessages by remember { mutableStateOf(messagesOfCollection.emit()) }

    var sortOrder by remember { mutableStateOf(SepaMessagesOrder()) }

    val checkedMap  = rememberMutableStateMapOf<SepaMessageId, Boolean>()

    var messageFilter by remember { mutableStateOf(SepaMessagesFilter()) }

    LaunchedEffect(
        messageFilter,
        sortOrder,
        messagesOfCollection.emit()
    ) {
        filteredMessages = messagesOfCollection.emit().filter{
            messageFilter applyTo it
        }.sortedBy(sortOrder)
    }

    val overallActionsData by remember { derivedStateOf {
        OverallActionsData(
            (messagesOfCollection map {
                messages -> messages.associateBy({it.sepaMessageId}){it}
            }).emit(),
            filteredMessages.map{it.sepaMessageId},
            checkedMap.toMap()
        )
    } }

    val listStyles = styles.modifyOverallActionsWrapper {
        paddingLeft(20.px)
        justifyContent(JustifyContent.FlexEnd)
    }.modifyOverallActions {
        flexGrow(0.0)
        alignSelf(AlignSelf.End)
    }

    ListWrapper(listStyles.listWrapper) {
        OverallActionsWrapper(listStyles.overallActionsWrapper) {
            OverallActions(listStyles.overallActions) {
                OverAllActionButtons(
                    scope, storage, overallActionsData, modalId
                )
            }
        }
        FilterWrapper(listStyles.modifyFilterWrapper { width(80.percent) }.filterWrapper) {
            Filter(listStyles.modifyFilter {
                width(20.percent)
                paddingLeft(20.px)
            }.filter){
                TextFilter("Filter by Status", ignoreCase = true) { text, ignoreCase ->
                    messageFilter = when {
                        text.isBlank() -> messageFilter.copy(status = { true })
                        else -> messageFilter.copy(status = { status -> text.toFilter(ignoreCase
                        ).applyTo(status.name) })
                    }
                }
            }
            Filter(listStyles.modifyFilter { width(15.percent) }.filter) {
                TextFilter("Filter by Execution Date", ignoreCase = true) { text, ignoreCase ->
                    messageFilter = when {
                        text.isBlank() -> messageFilter.copy(executionDate = { true })
                        else -> messageFilter.copy(executionDate = { date -> text.toFilter(ignoreCase).applyTo(date.format(Locale.Iso)) })
                    }
                }
            }
        }
        HeaderWrapper(listStyles.headerWrapper){
            Header(listStyles.header) {
                HeaderCell("Msg-Identifier") {
                    width(20.percent)
                }
                HeaderCellWithActions(
                    text = {"Exec. Date"},
                    styles = HeaderCellStyles().width(10.percent) ,
                    ordering = { SortByDrop{ order: SortOrder -> sortOrder = sortOrder.copy(executionDate = order) } }
                )
                HeaderCellWithActions(
                    text = {"Status"},
                    styles = HeaderCellStyles().width(10.percent) ,
                    ordering = { SortByDrop{ order: SortOrder -> sortOrder = sortOrder.copy(status = order) } }
                )
                HeaderCellWithActions(
                    text = {"Number"},
                    styles = HeaderCellStyles().width(10.percent) ,
                    ordering = { SortByDrop{ order: SortOrder -> sortOrder = sortOrder.copy(number = order) } }
                )
                HeaderCellWithActions(
                    text = {"Amount"},
                    styles = HeaderCellStyles().width(10.percent) ,
                    ordering = { SortByDrop{ order: SortOrder -> sortOrder = sortOrder.copy(amount = order) } }
                )
                HeaderCellWithActions(
                    text = {"Remittance Info"},
                    styles = HeaderCellStyles().width(40.percent) ,
                    ordering = { SortByDrop{ order: SortOrder -> sortOrder = sortOrder.copy(remittanceInformation = order) } }
                )
            }
        }
        Scrollable {
            ListItemsIndexed(filteredMessages) { index, item ->
                val isChecked = checkedMap[item.sepaMessageId] == true
                key(item.sepaMessageId, ) {
                    ListItemWrapper({
                        listItemWrapperStyle(index)
                    }) {
                        DataWrapper(listStyles.dataWrapper) {
                            CheckBoxCell({ isChecked }, { width(2.percent) }) {
                                if (isChecked) {
                                    checkedMap.remove(item.sepaMessageId)
                                } else {
                                    checkedMap[item.sepaMessageId] = true
                                }
                            }
                            TextCell(item.messageIdentifier.value) {
                                width(18.percent)
                            }
                            TextCell(item.executionDate.format(Locale.Iso)) {
                                width(10.percent)
                            }
                            TextCell(item.status.name) { width(10.percent) }
                            NumberCell(item.numberOfPayments) { width(10.percent) }
                            PriceCell((item.totalAmount ?: 0.0).round(2), Currency.EUR) { width(10.percent) }
                            TextCell(item.remittanceInformation.value) {
                                width(40.percent)
                            }
                        }
                        ActionsWrapper(listStyles.actionsWrapper) {
                            val deviceType = Read(storage * deviceData * mediaType)
                            when(item.status) {
                                SepaMessageStatus.CREATED -> DownloadButton(
                                    color = Color.black,
                                    bgColor = Color.white,
                                    texts = { "Download PAIN Message" },
                                    deviceType = deviceType,
                                ) {
                                    scope.launch {
                                        storage * bankingApplicationActions dispatch downloadSepaMessage(item.sepaMessageId)
                                    }
                                }
                                else -> Unit
                            }
                            NextStatusButtons(scope, storage, item)
                        }
                    }
                }
            }
        }
    }
}

data class OverallActionsData(
    val items: Map<SepaMessageId, SepaMessage>,
    val visible : List<SepaMessageId>,
    val checked: Map<SepaMessageId, Boolean>
) {
    fun selectedAndVisibleMessages(): List<SepaMessage> {
        return visible.filter { checked[it] == true }.mapNotNull { items[it] }
    }
}

@Markup
@Composable
@Suppress("UnusedParameter")
fun OverAllActionButtons(
    scope: CoroutineScope,
    storage: Storage<BankingApplication>,
    data: OverallActionsData,
    modalId: Int
) {
    val deviceType = Read(storage * deviceData * mediaType)

    var modalDataState by remember(data) {  mutableStateOf(
        MergeSepaMessagesData(
            sepaMessages = data.selectedAndVisibleMessages(),
            executionDate = null,
            remittanceInformation = null
        )
    ) }

    CodeMergeButton(
        color = Color.black,
        bgColor = Color.white,
        texts = { "Merge selected and visible messages - only possible if all selected message have status CREATED" },
        deviceType = deviceType,
        isDisabled = data.selectedAndVisibleMessages().any { it.status != SepaMessageStatus.CREATED }
    ) {
        // Open dialog to set common execution date
        (storage * bankingApplicationModals).showMergeSepaMessagesModal(
            parentModalId = modalId,
            storage = storage,
            texts = mergeSepaMessagesModalTexts,
            device = deviceType,
            isOkButtonDisabled = {
                modalDataState.executionDate == null ||
                modalDataState.remittanceInformation == null
            },
            data = modalDataState,
            setData = {
                modalDataState = it
            }
        ) {
            scope.launch {
                val executionDate = requireNotNull(modalDataState.executionDate)
                val remittanceInformation = requireNotNull(modalDataState.remittanceInformation)
                val sepaMessageIds = modalDataState.sepaMessages.map { it.sepaMessageId }
                storage * bankingApplicationActions dispatch mergeSepaMessages(
                    sepaMessageIds,
                    executionDate,
                    remittanceInformation
                )
            }
        }
    }
}

@Markup
@Composable
fun NextStatusButtons(
    scope: CoroutineScope,
    storage: Storage<BankingApplication>,
    sepaMessage: SepaMessage,
) {


    fun dispatch(status: SepaMessageStatus) = scope.launch {
        storage * bankingApplicationActions dispatch updateSepaMessagesStatus(
            sepaMessage.sepaMessageId,
            status
        )
    }

    val deviceType = Read(storage * deviceData * mediaType)

    return when (sepaMessage.status) {
        SepaMessageStatus.CREATED -> EnvelopeCircleCheckButton(
            color = Color.black,
            bgColor = Color.white,
            texts = { "Set status to Sent" },
            deviceType = deviceType,
        ) {
            dispatch(SepaMessageStatus.SENT)
        }

        SepaMessageStatus.SENT -> {
            BanButton(
                color = Color.black,
                bgColor = Color.white,
                texts = { "Set status to Failed" },
                deviceType = deviceType,
            ) {
                dispatch(SepaMessageStatus.FAILED)
            }
            ClockButton(
                color = Color.black,
                bgColor = Color.white,
                texts = { "Set status to Pending" },
                deviceType = deviceType,
            ) {
                dispatch(SepaMessageStatus.PENDING)
            }

        }
        SepaMessageStatus.PENDING -> {
            BanButton(
                color = Color.black,
                bgColor = Color.white,
                texts = { "Set status to Failed" },
                deviceType = deviceType,
            ) {
                dispatch(SepaMessageStatus.FAILED)
            }
            CheckButton(
                color = Color.black,
                bgColor = Color.white,
                texts = { "Set status to Confirmed" },
                deviceType = deviceType,
            ) {
                dispatch(SepaMessageStatus.CONFIRMED)
            }
        }
        SepaMessageStatus.CONFIRMED -> SackDollarButton(
            color = Color.black,
            bgColor = Color.white,
            texts = { "Set status to settled" },
            deviceType = deviceType,
        ) {
            dispatch(SepaMessageStatus.SETTLED)
        }
        SepaMessageStatus.FAILED -> Unit
        SepaMessageStatus.SETTLED -> Unit
        SepaMessageStatus.MERGED -> Unit
    }

}
