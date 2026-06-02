package org.solyton.solawi.bid.module.banking.component.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import kotlinx.datetime.LocalDate
import org.evoleq.compose.Markup
import org.evoleq.compose.conditional.When
import org.evoleq.compose.date.format
import org.evoleq.compose.layout.Horizontal
import org.evoleq.language.Locale
import org.evoleq.math.Source
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.justifyContent
import org.jetbrains.compose.web.css.justifyItems

import org.jetbrains.compose.web.css.paddingLeft
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.TextInput
import org.jetbrains.letsPlot.Geom
import org.solyton.solawi.bid.application.ui.page.user.style.listItemWrapperStyle
import org.solyton.solawi.bid.module.banking.data.SepaMandateId
import org.solyton.solawi.bid.module.banking.data.SepaPaymentId
import org.solyton.solawi.bid.module.banking.data.sepa.PaymentExecutionStatus
import org.solyton.solawi.bid.module.banking.data.sepa.mandate.SepaMandate
import org.solyton.solawi.bid.module.banking.data.sepa.payment.SepaPayment
import org.solyton.solawi.bid.module.list.component.ActionsWrapper
import org.solyton.solawi.bid.module.list.component.CheckBoxCell
import org.solyton.solawi.bid.module.list.component.DataWrapper
import org.solyton.solawi.bid.module.list.component.Filter
import org.solyton.solawi.bid.module.list.component.FilterParser
import org.solyton.solawi.bid.module.list.component.FilterWrapper
import org.solyton.solawi.bid.module.list.component.Header
import org.solyton.solawi.bid.module.list.component.HeaderCell
import org.solyton.solawi.bid.module.list.component.HeaderWrapper
import org.solyton.solawi.bid.module.list.component.ListItemWrapper
import org.solyton.solawi.bid.module.list.component.ListItemsIndexed
import org.solyton.solawi.bid.module.list.component.ListWrapper
import org.solyton.solawi.bid.module.list.component.OverallActions
import org.solyton.solawi.bid.module.list.component.OverallActionsWrapper
import org.solyton.solawi.bid.module.list.component.TextCell
import org.solyton.solawi.bid.module.list.component.TextFilter
import org.solyton.solawi.bid.module.list.component.Title
import org.solyton.solawi.bid.module.list.component.TitleWrapper
import org.solyton.solawi.bid.module.list.component.applyTo
import org.solyton.solawi.bid.module.list.component.rememberMutableStateMapOf
import org.solyton.solawi.bid.module.list.component.toFilter
import org.solyton.solawi.bid.module.list.style.ListStyles
import org.solyton.solawi.bid.module.scrollable.Scrollable

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

data class SepaPaymentListItemKey(
    val paymentId: SepaPaymentId,
    val mandateId: SepaMandateId
)

data class OverAllActionData(
    val itemsMap: Map<SepaPaymentListItemKey,SepaPaymentListItemData> = emptyMap(),
    val visibleItems: List<SepaPaymentListItemKey> = emptyList(),
    val checkedPayments: Map<SepaPaymentId, Boolean> = emptyMap()
)

@Markup
@Composable
@Suppress("FunctionName")
fun ListOfPayments(
    title: String? = null,
    mandates: List<SepaMandate>,
    payments: List<SepaPayment>,
    styles: ListStyles = ListStyles(),
    overallActions: @Composable (data: OverAllActionData) -> Unit = {},
    actions: @Composable () -> Unit = {}
){
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

    LaunchedEffect(filter, listData) {
        filteredList = listData.filter {
            filter applyTo it
        }
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
                HeaderCell("Status") { width(20.percent) }
                HeaderCell("Debtor"){ width(25.percent) }
                HeaderCell("Exec. Date"){ width(15.percent) }
                HeaderCell("Amount") { width(10.percent) }
                HeaderCell("Seq Type") {width(10.percent)}
                HeaderCell("Failure Reason") {width(30.percent)}
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
                            TextCell(listItem.payment.status.name) { width(20.percent) }
                            TextCell(listItem.mandate.debtorName) { width(25.percent) }
                            TextCell(listItem.payment.executionDate.format(Locale.Iso)) { width(15.percent) }
                            TextCell("${listItem.payment.amount}") { width(10.percent) }
                            TextCell(listItem.payment.sequenceType.name) { width(10.percent) }
                            TextCell(listItem.payment.failureReason ?: "--") { width(30.percent) }
                        }
                        ActionsWrapper(styles.actionsWrapper) {
                            actions()
                        }
                    }
                }
            }
        }
    }
}
