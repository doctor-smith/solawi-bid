package org.solyton.solawi.bid.module.banking.util

import org.solyton.solawi.bid.module.banking.schema.PaymentExecutionStatus
import org.solyton.solawi.bid.module.banking.schema.SepaMessageStatus


@Suppress("CyclomaticComplexMethod")
operator fun SepaMessageStatus.compareTo(other: SepaMessageStatus): Int = when(this) {
    SepaMessageStatus.CREATED -> when(other) {
        SepaMessageStatus.CREATED -> 0
        else -> -1
    }
    SepaMessageStatus.SENT -> when(other) {
        SepaMessageStatus.CREATED -> 1
        SepaMessageStatus.SENT -> 0
        else -> -1
    }
    SepaMessageStatus.PENDING -> when(other) {
        SepaMessageStatus.CREATED -> 1
        SepaMessageStatus.SENT -> 1
        SepaMessageStatus.PENDING -> 0
        else -> -1
    }

    SepaMessageStatus.CONFIRMED , SepaMessageStatus.FAILED-> when(other) {
        SepaMessageStatus.CREATED -> 1
        SepaMessageStatus.SENT -> 1
        SepaMessageStatus.PENDING -> 1
        SepaMessageStatus.CONFIRMED -> 0
        SepaMessageStatus.FAILED -> 0
        SepaMessageStatus.SETTLED -> -1
    }

    SepaMessageStatus.SETTLED -> when(other) {
        SepaMessageStatus.SETTLED -> 0
        else -> 1
    }
}

fun List<PaymentExecutionStatus>.toSepaMessageStatus(): SepaMessageStatus? = when {
    any { it == PaymentExecutionStatus.MESSAGE_SETTLED } -> SepaMessageStatus.SETTLED
    any { it == PaymentExecutionStatus.CONFIRMED } -> SepaMessageStatus.SETTLED
    any { it == PaymentExecutionStatus.FAILED } -> SepaMessageStatus.SETTLED
    any { it == PaymentExecutionStatus.PAYED_MANUALLY } -> SepaMessageStatus.SETTLED
    any { it == PaymentExecutionStatus.DROPPED } -> SepaMessageStatus.SETTLED
    any { it == PaymentExecutionStatus.PENDING } -> SepaMessageStatus.CONFIRMED
    any { it == PaymentExecutionStatus.SENT } -> SepaMessageStatus.SENT
    any { it == PaymentExecutionStatus.MESSAGE_CREATED } -> SepaMessageStatus.CREATED
    else -> null
}
