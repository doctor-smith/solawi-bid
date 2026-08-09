package org.solyton.solawi.bid.module.banking.component.list.sepa

import androidx.compose.runtime.*
import kotlinx.datetime.LocalDate
import org.evoleq.compose.conditional.When
import org.evoleq.compose.date.format
import org.evoleq.compose.layout.Vertical
import org.evoleq.language.Locale
import org.evoleq.math.Source
import org.evoleq.math.emit
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.application.ui.page.user.style.listItemWrapperStyle
import org.solyton.solawi.bid.module.banking.data.SepaMandateId
import org.solyton.solawi.bid.module.banking.data.SepaPaymentId
import org.solyton.solawi.bid.module.banking.data.sepa.PaymentExecutionStatus
import org.solyton.solawi.bid.module.banking.data.sepa.SuccessorKind
import org.solyton.solawi.bid.module.banking.data.sepa.mandate.SepaMandate
import org.solyton.solawi.bid.module.banking.data.sepa.payment.SepaPayment
import org.solyton.solawi.bid.module.banking.data.sepa.payment.SepaPaymentHistories
import org.solyton.solawi.bid.module.banking.data.sepa.payment.SepaPaymentHistory
import org.solyton.solawi.bid.module.banking.data.sepa.payment.SepaPaymentLink
import org.solyton.solawi.bid.module.list.component.*
import org.solyton.solawi.bid.module.list.style.ListStyles
import org.solyton.solawi.bid.module.scrollable.Scrollable
import org.solyton.solawi.bid.module.style.overflow.Overflow
import org.solyton.solawi.bid.module.style.overflow.overflow
import org.solyton.solawi.bid.module.style.overflow.overflowX
import org.solyton.solawi.bid.module.style.page.Title

sealed class VisualPayment(

    open val status: PaymentExecutionStatus,
    open val executionDate: LocalDate,
) {

    interface AdHocPayments {
        val adHocPayments: List<AdHoc>
    }

    interface Retries {
        val retries: List<Retry>
    }

    data class Periodic(
        override val status: PaymentExecutionStatus,
        override val executionDate: LocalDate,
        override val retries: List<Retry> = emptyList(),
        override val adHocPayments: List<AdHoc> = emptyList()

    ) : VisualPayment(status, executionDate), Retries, AdHocPayments

    data class Retry(
        override val status: PaymentExecutionStatus,
        override val executionDate: LocalDate
    ) : VisualPayment(status, executionDate)

    data class AdHoc(
        override val status: PaymentExecutionStatus,
        override val executionDate: LocalDate,
        override val retries: List<Retry>
    ) : VisualPayment(status, executionDate), Retries
}

data class HistoryFilter(
    val debtor: (String) -> Boolean = { true },
) {
    infix fun applyTo(item: PeriodicPaymentsData): Boolean = with(item) {
        debtor(mandate.debtorName)
    }
}

data class HistorySortOrder(
    val debtor: SortOrder = SortOrder.ASC,
) {
    fun toComparator(): Comparator<PeriodicPaymentsData> = Comparator { o1, o2 ->
        var result = 0

        if (result == 0 && debtor != SortOrder.NONE) {
            result = compareValues(o1.mandate.debtorName, o2.mandate.debtorName)
            if (debtor == SortOrder.DESC) result = -result
        }

        result
    }
}

fun List<PeriodicPaymentsData>.sortedBy(order: HistorySortOrder): List<PeriodicPaymentsData> {
    return sortedWith(order.toComparator())
}

@Composable
fun SepaPaymentHistoryList(
    sepaPaymentLinks: Source<List<SepaPaymentLink>>,
    sepaPayments: Source<List<SepaPayment>>,
    sepaMandates: Source<List<SepaMandate>>
) {
    val paymentsMap = sepaPayments.emit().associateBy { it.sepaPaymentId }
    val mandatesMap = sepaMandates.emit().associateBy { it.sepaMandateId }
    // All links need to point to payments in the given list (usually
    // provided by the current collection)
    val allowedLinks = sepaPaymentLinks.emit().filter {
        paymentsMap.containsKey(it.successorId)  &&
        paymentsMap.containsKey(it.predecessorId)
    }
    val paymentHistory = SepaPaymentHistories.build( paymentsMap.keys.toList(), allowedLinks )
    val periodicPayments = paymentHistory.toPeriodicPayments(paymentsMap, mandatesMap)

    // stores the filter state
    var filter by remember { mutableStateOf(HistoryFilter()) }
    var filteredPeriodicPayments by remember { mutableStateOf(periodicPayments) }
    var sortOrder by remember { mutableStateOf(HistorySortOrder())}

    LaunchedEffect(filter, periodicPayments, sortOrder){
        filteredPeriodicPayments = periodicPayments.filter {
            filter applyTo it
        }.sortedBy(sortOrder)
    }


    val listStyles = ListStyles()

    ListWrapper {
        TitleWrapper { Title("Payment Histories ${periodicPayments.size}") }
        FilterWrapper {
            Filter(listStyles.modifyFilter {

            }.filter) {
                TextFilter("Filter by Debtor", ignoreCase = true) { text, ignoreCase ->
                    filter = when {
                        text.isBlank() -> filter.copy(debtor = { true })
                        else -> filter.copy(debtor = { debtor -> text.toFilter(ignoreCase).applyTo(debtor) })
                    }
                }
            }
        }
        HeaderWrapper {
            Header {
                HeaderCellWithActions(
                    text = {"Debtor"},
                    styles = HeaderCellStyles().width(20.percent) ,
                    ordering = { SortByDrop{ order: SortOrder -> sortOrder = sortOrder.copy(debtor = order) } }
                )
            }
        }
        Scrollable {
            ListItemsIndexed(filteredPeriodicPayments) { index, paymentsData ->
                ListItemWrapper({ listItemWrapperStyle(index) }) {
                    DataWrapper {
                        val (mandate, payments) = paymentsData
                        TextCell(mandate.debtorName) { width(20.percent) }
                        payments.forEach {
                            PeriodicPayment(it)
                        }
                    }
                }
            }
        }
    }
}

data class PeriodicPaymentStyles(
    val periodic: StyleScope.() -> Unit
)

@Composable
fun PeriodicPayment(
    data: VisualPayment.Periodic,
    allOpen: Boolean = false
) {
    var opened by remember(allOpen) { mutableStateOf(allOpen) }

    Vertical ({
            width(10.percent)
            maxWidth(10.percent)
            overflowX(Overflow.Hidden)
            flexGrow(1.0)
    }){
        PaymentItem(data) { opened = !opened }
        When(opened) {
            data.retries.forEach {
                Div({
                    style {
                        width(100.percent)
                        display(DisplayStyle.Flex)
                        justifyContent(JustifyContent.Center)
                        alignItems(AlignItems.Center)
                    }
                }) {
                    Text("|")
                }
                PaymentItem(it)
            }
        }
    }
}

@Composable
fun PaymentItem(
    data: VisualPayment,
    handleClick: () -> Unit = {}
) {
    val statusColor = colorOf(data.status)
    val currentRetry = when(data ) {
        is VisualPayment.Periodic -> data.retries.lastOrNull()
        else -> null
    }
    val retryStatusColor = currentRetry?.let{ colorOf(it.status )}?: statusColor

    Div({
        title( data.executionDate.format(Locale.Iso))
        style {
            color(statusColor)
            border {
                style(LineStyle.Solid)
                width(1.px)
                borderRadius(5.px)
                color(retryStatusColor)
            }
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Column)
            justifyContent(JustifyContent.Center)
            alignItems(AlignItems.Center)
            overflow(Overflow.Hidden)
            // fontSize(80.percent)
        }
        if(data is VisualPayment.Periodic) {
            onClick { handleClick() }
        }
    }) {
        Text(data.status.name)

    }
}

fun colorOf(data: PaymentExecutionStatus): CSSColorValue = when(data) {
    PaymentExecutionStatus.FAILED -> Color.crimson
    PaymentExecutionStatus.CONFIRMED,
    PaymentExecutionStatus.PAYED_MANUALLY -> Color.seagreen
    PaymentExecutionStatus.CREATED,
    PaymentExecutionStatus.MESSAGE_CREATED,
    PaymentExecutionStatus.SENT,
    PaymentExecutionStatus.PENDING -> Color.orange
    PaymentExecutionStatus.MESSAGE_SETTLED -> Color.orangered
    PaymentExecutionStatus.DROPPED -> Color.gray
}

data class PeriodicPaymentsData(
    val mandate: SepaMandate,
    val payments: List<VisualPayment.Periodic>
)

fun SepaPaymentHistories.toPeriodicPayments(
    paymentsMap: Map<SepaPaymentId, SepaPayment>,
    mandatesMap: Map<SepaMandateId, SepaMandate>
): List<PeriodicPaymentsData> = all.map { it.toPeriodicPayments(paymentsMap, mandatesMap) }

fun SepaPaymentHistory.toPeriodicPayments(
    paymentsMap: Map<SepaPaymentId, SepaPayment>,
    mandatesMap: Map<SepaMandateId, SepaMandate>
): PeriodicPaymentsData {
    val payments = mutableListOf<VisualPayment.Periodic>()

    val initial = paymentsMap[id]!!
    val mandate = mandatesMap[initial.sepaMandateId]!!

    toPeriodicPayments(paymentsMap, mandatesMap, payments)
    return PeriodicPaymentsData(mandate,payments)
}

fun SepaPaymentHistory.toPeriodicPayments(
    paymentsMap: Map<SepaPaymentId, SepaPayment>,
    mandatesMap: Map<SepaMandateId, SepaMandate>,
    collectedPayments: MutableList<VisualPayment.Periodic>
) {
    val initial = paymentsMap[id]!!

    // Prepare payment:
    // Eventually add Retries and AdHoc Payments
    val payment = when(this) {
        is SepaPaymentHistory.Initial -> VisualPayment.Periodic(
            initial.status,
            initial.executionDate,
            retries = links.filter{ it.kind == SuccessorKind.RETRY }.flatMap{ it.toRetryPayments(paymentsMap, mandatesMap) }
        )
        is SepaPaymentHistory.Successor.Node -> VisualPayment.Periodic(
            initial.status,
            initial.executionDate,
            retries = links.filter{ it.kind == SuccessorKind.RETRY }.flatMap{ it.toRetryPayments(paymentsMap, mandatesMap) }
        )
        is SepaPaymentHistory.Successor.Leaf -> VisualPayment.Periodic(initial.status, initial.executionDate)
    }
    collectedPayments.add(
        payment
    )
    when(this) {
        is SepaPaymentHistory.Links ->
            links.filter{
                it.kind == SuccessorKind.NEXT_PERIOD
            }.forEach {
                it.toPeriodicPayments(paymentsMap, mandatesMap, collectedPayments)
            }
        else -> return
    }
}

fun SepaPaymentHistory.toRetryPayments(
    paymentsMap: Map<SepaPaymentId, SepaPayment>,
    mandatesMap: Map<SepaMandateId, SepaMandate>,
    collectedRetries: MutableList<VisualPayment.Retry> = mutableListOf()
) : List<VisualPayment.Retry> {
    val initial = paymentsMap[id]!!

    collectedRetries += VisualPayment.Retry(
        initial.status,
        initial.executionDate
    )
    when(this) {
        is SepaPaymentHistory.Links -> links.filter{it.kind == SuccessorKind.RETRY}.forEach {
            collectedRetries.addAll(it.toRetryPayments(paymentsMap, mandatesMap))
        }
        else -> Unit
    }

   return  collectedRetries
}
