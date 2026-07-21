package org.solyton.solawi.bid.module.banking.component.properties

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.evoleq.compose.layout.PropertiesStyles
import org.evoleq.compose.layout.Property
import org.evoleq.compose.layout.ReadOnlyProperties
import org.jetbrains.compose.web.css.flexGrow
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.width
import org.solyton.solawi.bid.module.banking.data.internal.Currency
import org.solyton.solawi.bid.module.banking.data.internal.format
import org.solyton.solawi.bid.module.banking.data.internal.toMoney
import org.solyton.solawi.bid.module.banking.data.sepa.PaymentExecutionStatus
import org.solyton.solawi.bid.module.banking.data.sepa.payment.SepaPayment

@Markup
@Composable
@Suppress("FunctionName")
fun PaymentsProperties(payments: List<SepaPayment>) =
    ReadOnlyProperties(listOf(
        Property("Number", payments.size),
        Property("Total Amount", (payments.sumOf { it.amount }).toMoney(Currency.EUR).format()),
    ),
        PropertiesStyles().modifyContainerStyle {
            flexGrow(1)
        }.modifyPropertyStyles {
            modifyKeyStyle {
                width(50.percent)
            }.modifyValueStyle {
                width(50.percent)
            }
        }
    )


@Markup
@Composable
@Suppress("FunctionName")
fun PaymentsOverviewProperties(payments: List<SepaPayment>) {
    val relevantPayments = payments.filter { it.retrySuccessorId == null }
    val confirmedPayments = relevantPayments.filter {
        it.status in listOf(PaymentExecutionStatus.CONFIRMED, PaymentExecutionStatus.PAYED_MANUALLY, "EXECUTED")
    }
    val failedPayments = relevantPayments.filter { it.status == PaymentExecutionStatus.FAILED }
    val openPayments = relevantPayments.filterNot { it.status in listOf(
        PaymentExecutionStatus.FAILED,
        PaymentExecutionStatus.CONFIRMED,
        PaymentExecutionStatus.PAYED_MANUALLY
    ) }
    ReadOnlyProperties(listOf(
        Property("Total Number", relevantPayments.size),
        Property("Confirmed", confirmedPayments.size),
        Property("Open", openPayments.size),
        Property("Failed", failedPayments.size),
        Property("Total Amount", (relevantPayments.sumOf { it.amount }).toMoney(Currency.EUR).format()),
        Property("Confirmed Amount", (confirmedPayments.sumOf { it.amount }).toMoney(Currency.EUR).format()),
        Property("Open Amount", (openPayments.sumOf { it.amount }).toMoney(Currency.EUR).format()),
        Property("Failed Amount", (failedPayments.sumOf { it.amount }).toMoney(Currency.EUR).format()),
    ),
        PropertiesStyles().modifyContainerStyle {
            flexGrow(1)
        }.modifyPropertyStyles {
            modifyKeyStyle {
                width(50.percent)
            }.modifyValueStyle {
                width(50.percent)
            }
        }
    )
}

