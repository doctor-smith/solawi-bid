package org.solyton.solawi.bid.module.banking.component.list

import androidx.compose.runtime.*
import kotlinx.datetime.LocalDate
import org.evoleq.compose.Markup
import org.evoleq.compose.conditional.When
import org.evoleq.compose.date.format
import org.evoleq.language.Locale
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.application.ui.page.user.style.listItemWrapperStyle
import org.solyton.solawi.bid.module.banking.data.SepaMandateId
import org.solyton.solawi.bid.module.banking.data.SepaPaymentId
import org.solyton.solawi.bid.module.banking.data.sepa.PaymentExecutionStatus
import org.solyton.solawi.bid.module.banking.data.sepa.mandate.SepaMandate
import org.solyton.solawi.bid.module.banking.data.sepa.payment.SepaPayment
import org.solyton.solawi.bid.module.list.component.*
import org.solyton.solawi.bid.module.list.style.ListStyles
import org.solyton.solawi.bid.module.scrollable.Scrollable
import org.solyton.solawi.bid.module.style.overflow.Overflow
import org.solyton.solawi.bid.module.style.overflow.overflow
import org.solyton.solawi.bid.module.style.text.TextOverflow
import org.solyton.solawi.bid.module.style.text.WhiteSpace
import org.solyton.solawi.bid.module.style.text.textOverflow
import org.solyton.solawi.bid.module.style.text.whiteSpace

data class SepaPaymentListItemData(
    // val userProfile: UserProfile,
    val payment: SepaPayment,
    val mandate: SepaMandate
)

data class SepaPaymentsFilter(
    val status: (PaymentExecutionStatus) -> Boolean = { true },
    val debtor: (String) -> Boolean = { true },
    val executionDate: (LocalDate) -> Boolean = { true },
    val amount: (Double) -> Boolean = { true },
    val sequenceType: (String) -> Boolean = { true },
    val failureReason: (String) -> Boolean = { true },
    val custom: (SepaPaymentListItemData) -> Boolean = { true }
) {
    infix fun applyTo(listItem: SepaPaymentListItemData): Boolean = with(listItem) {
        status(payment.status) &&
        debtor(mandate.debtorName) &&
        executionDate(payment.executionDate) &&
        amount(payment.amount) &&
        sequenceType(payment.sequenceType.name) &&
        failureReason(payment.failureReason.orEmpty()) &&
        custom(listItem)
    }
}

data class SepaPaymentsOrder(
    val status: SortOrder = SortOrder.NONE,
    val debtor: SortOrder = SortOrder.ASC,
    val executionDate: SortOrder = SortOrder.DESC,
    val amount: SortOrder = SortOrder.NONE,
    val sequenceType: SortOrder = SortOrder.NONE,
    val failureReason: SortOrder = SortOrder.NONE,
) {
    fun toComparator(): Comparator<SepaPaymentListItemData> = Comparator { o1, o2 ->
        var result = 0

        if (result == 0 && status != SortOrder.NONE) {
            result = compareValues(o1.payment.status.name, o2.payment.status.name)
            if (status == SortOrder.DESC) result = -result
        }

        if (result == 0 && debtor != SortOrder.NONE) {
            result = compareValues(o1.mandate.debtorName, o2.mandate.debtorName)
            if (debtor == SortOrder.DESC) result = -result
        }

        if (result == 0 && executionDate != SortOrder.NONE) {
            result = compareValues(o1.payment.executionDate, o2.payment.executionDate)
            if (executionDate == SortOrder.DESC) result = -result
        }

        if (result == 0 && amount != SortOrder.NONE) {
            result = compareValues(o1.payment.amount, o2.payment.amount)
            if (amount == SortOrder.DESC) result = -result
        }

        if (result == 0 && sequenceType != SortOrder.NONE) {
            result = compareValues(o1.payment.sequenceType.name, o2.payment.sequenceType.name)
            if (sequenceType == SortOrder.DESC) result = -result
        }

        if (result == 0 && failureReason != SortOrder.NONE) {
            result = compareValues(o1.payment.failureReason.orEmpty(), o2.payment.failureReason.orEmpty())
            if (failureReason == SortOrder.DESC) result = -result
        }

        result
    }
}

fun List<SepaPaymentListItemData>.sortedBy(order: SepaPaymentsOrder): List<SepaPaymentListItemData> {
    return sortedWith(order.toComparator())
}

data class SepaPaymentListItemKey(
    val paymentId: SepaPaymentId,
    val mandateId: SepaMandateId
)

data class OverAllActionData(
    val itemsMap: Map<SepaPaymentListItemKey,SepaPaymentListItemData> = emptyMap(),
    val visibleItems: List<SepaPaymentListItemKey> = emptyList(),
    val checkedPayments: Map<SepaPaymentId, Boolean> = emptyMap()
)

data class ActionsData(
    val data: SepaPaymentListItemData
)

@Markup
@Composable
@Suppress("FunctionName")
fun ListOfPayments(
    title: String? = null,
    mandates: List<SepaMandate>,
    payments: List<SepaPayment>,
    styles: ListStyles = ListStyles(),
    displayIfEmpty: Boolean = true,
    overallActions: @Composable (data: OverAllActionData) -> Unit = {},
    actions: @Composable (data: ActionsData) -> Unit = {}
) = When(displayIfEmpty || payments.isNotEmpty()) {
    // val sortedPayments = payments.sortedByDescending { it.executionDate }
    val mandatesMap = payments.associateBy({it.sepaPaymentId}) { payment ->
        mandates.firstOrNull { it.sepaMandateId == payment.sepaMandateId }
    }

    // Combine mandates and payments in a single list / map
    val listData = payments.map { payment -> SepaPaymentListItemData( payment, mandatesMap[payment.sepaPaymentId]!!) }
    val listDataMap = listData.associateBy { SepaPaymentListItemKey(it.payment.sepaPaymentId, it.payment.sepaMandateId) }

    // stores the filtered list of payments, based on the filter state
    var filteredList by remember { mutableStateOf(listData) }
    // stores the filter state
    var filter by remember { mutableStateOf(SepaPaymentsFilter()) }
    // stores the sort order state
    var sortOrder by remember { mutableStateOf(SepaPaymentsOrder()) }
    // stores checked state of each item in the list of payments, regardless of the filter state
    val checkedMap = rememberMutableStateMapOf<SepaPaymentId, Boolean>()
    // data to be passed to the overall actions component
    val overallActionsData by remember { derivedStateOf {
        OverAllActionData(
            itemsMap = listDataMap,
            visibleItems = filteredList.map {
                SepaPaymentListItemKey(
                    it.payment.sepaPaymentId,
                    it.payment.sepaMandateId
                )
            },
            checkedPayments = checkedMap.toMap()
        )
    } }

    LaunchedEffect(filter, listData, sortOrder) {
        filteredList = listData.filter {
            filter applyTo it
        }.sortedBy(sortOrder)
    }

    // var showFilter by remember { mutableStateOf(false) }

    ListWrapper(styles.listWrapper) {
        When(title != null) {
            TitleWrapper(styles.titleWrapper) {
                Title(styles.title) { H3 { Text(requireNotNull(title)) } }
            }
        }
        OverallActionsWrapper(styles.overallActionsWrapper) {
            OverallActions(styles.overallActions) {
                key(overallActionsData) {
                    overallActions(overallActionsData)
                }
            }
        }

        FilterWrapper(styles.modifyFilterWrapper { width(80.percent) }.filterWrapper) {

            Filter(styles.modifyFilter {
                width(20.percent)
                paddingLeft(20.px)
            }.filter) {
                TextFilter("Filter by Status") {
                    filter = when {
                        it.isBlank() -> filter.copy(status = { true })
                        else -> filter.copy(status = { status -> it.toFilter().applyTo(status.name) })
                    }
                }
            }
            Filter(styles.modifyFilter { width(25.percent) }.filter) {
                TextFilter("Filter by Debtor") {
                    filter = when {
                        it.isBlank() -> filter.copy(debtor = { true })
                        else -> filter.copy(debtor = { debtor -> it.toFilter().applyTo(debtor) })
                    }
                }
            }
        }

        HeaderWrapper(styles.headerWrapper) {
            Header(styles.header) {
                val allChecked = filteredList.isNotEmpty() && filteredList.all { checkedMap[it.payment.sepaPaymentId] == true }
                key(allChecked) {
                    CheckBoxCell({ allChecked }, { width(2.percent) }) {
                        val newCheckedState = !allChecked
                        if (newCheckedState) {
                            filteredList.forEach {
                                checkedMap[it.payment.sepaPaymentId] = true
                            }
                        } else {
                            filteredList.forEach {
                                checkedMap.remove(it.payment.sepaPaymentId)
                            }
                        }
                    }
                }

                HeaderCellWithActions(
                    text = {"Status"},
                    styles = HeaderCellStyles().width(15.percent) ,
                    ordering = { SortByDrop{ order: SortOrder -> sortOrder = sortOrder.copy(status = order) } }
                )
                HeaderCellWithActions(
                    text = {"Debtor"},
                    styles = HeaderCellStyles().width(25.percent) ,
                    ordering = { SortByDrop{ order: SortOrder -> sortOrder = sortOrder.copy(debtor = order) } }
                )
                HeaderCellWithActions(
                    text = {"Exec. Date"},
                    styles = HeaderCellStyles().width(15.percent) ,
                    ordering = { SortByDrop{ order: SortOrder -> sortOrder = sortOrder.copy(executionDate = order) } }
                )
                HeaderCellWithActions(
                    text = {"Amount"},
                    styles = HeaderCellStyles().width(10.percent) ,
                    ordering = { SortByDrop{ order: SortOrder -> sortOrder = sortOrder.copy(amount = order) } }
                )
                HeaderCellWithActions(
                    text = {"S-Type"},
                    styles = HeaderCellStyles().width(10.percent) ,
                    ordering = { SortByDrop{ order: SortOrder -> sortOrder = sortOrder.copy(sequenceType = order) } }
                )
                // HeaderCell("Status"){ width(15.percent) }
                // HeaderCell("Debtor"){ width(25.percent) }
                // HeaderCell("Exec. Date"){ width(15.percent) }
                // HeaderCell("Amount") { width(10.percent) }
                // HeaderCell("Seq Type") {width(10.percent)}
                HeaderCell("Failure Reason") {width(20.percent)}
            }
        }
        Scrollable {
            ListItemsIndexed(filteredList) { index, listItem ->
                val isChecked = checkedMap[listItem.payment.sepaPaymentId] == true
                key(listItem.payment.sepaPaymentId, isChecked) {
                    ListItemWrapper({
                        listItemWrapperStyle(index)
                    }) {
                        DataWrapper(styles.dataWrapper) {
                            CheckBoxCell({ isChecked }, { width(2.percent) }) {
                                if (isChecked) {
                                    checkedMap.remove(listItem.payment.sepaPaymentId)
                                } else {
                                    checkedMap[listItem.payment.sepaPaymentId] = true
                                }
                            }
                            TextCell(listItem.payment.status.name) { width(15.percent) }
                            TextCell(listItem.mandate.debtorName) { width(25.percent) }
                            TextCell(listItem.payment.executionDate.format(Locale.Iso)) { width(15.percent) }
                            TextCell("${listItem.payment.amount}") { width(10.percent) }
                            TextCell(listItem.payment.sequenceType.name) { width(10.percent) }
                            TextCell(
                                text = listItem.payment.failureReason ?: "--",
                                tooltip = listItem.payment.failureReason
                            ) {
                                minWidth(0.px)
                                width(20.percent)

                                flexGrow(0)
                                flexShrink(0)
                                whiteSpace(WhiteSpace.NoWrap)
                                overflow(Overflow.Hidden)
                                textOverflow(TextOverflow.Ellipsis)
                            }
                        }
                        ActionsWrapper(styles.actionsWrapper) {
                            actions(ActionsData(listItem))
                        }
                    }

                }
            }
        }
    }
}
