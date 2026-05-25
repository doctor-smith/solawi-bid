package org.solyton.solawi.bid.module.banking.component.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.datetime.LocalDate
import org.evoleq.compose.Markup
import org.evoleq.compose.conditional.When
import org.evoleq.compose.date.format
import org.evoleq.compose.layout.Horizontal
import org.evoleq.language.Locale
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
import org.solyton.solawi.bid.module.banking.data.sepa.PaymentExecutionStatus
import org.solyton.solawi.bid.module.banking.data.sepa.mandate.SepaMandate
import org.solyton.solawi.bid.module.banking.data.sepa.payment.SepaPayment
import org.solyton.solawi.bid.module.list.component.ActionsWrapper
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
import org.solyton.solawi.bid.module.list.component.toFilter
import org.solyton.solawi.bid.module.list.style.ListStyles
import org.solyton.solawi.bid.module.navbar.component.SimpleUpDown
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

@Markup
@Composable
@Suppress("FunctionName")
fun ListOfPayments(
    title: String? = null,
    mandates: List<SepaMandate>,
    payments: List<SepaPayment>,
    styles: ListStyles = ListStyles(),
    overallActions: @Composable () -> Unit = {},
    actions: @Composable () -> Unit = {}
){
    // val sortedPayments = payments.sortedByDescending { it.executionDate }
    val mandatesMap = payments.associateBy({it.sepaPaymentId}) { payment ->
        mandates.firstOrNull { it.sepaMandateId == payment.sepaMandateId }
    }

    val listData = payments.map { payment -> SepaPaymentListItemData( payment, mandatesMap[payment.sepaPaymentId]!!) }

    var filteredList by remember { mutableStateOf(listData) }
    var filter by remember { mutableStateOf(SepaPaymentsFilter()) }
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
                overallActions()
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
                ListItemWrapper({
                    listItemWrapperStyle(index)
                }) {
                    DataWrapper(styles.dataWrapper) {
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
