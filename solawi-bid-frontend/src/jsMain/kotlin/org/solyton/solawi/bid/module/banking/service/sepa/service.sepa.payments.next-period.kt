package org.solyton.solawi.bid.module.banking.service.sepa

import org.solyton.solawi.bid.module.banking.data.sepa.PaymentExecutionStatus
import org.solyton.solawi.bid.module.banking.data.sepa.SepaSequenceType
import org.solyton.solawi.bid.module.banking.data.sepa.payment.SepaPayment
import org.solyton.solawi.bid.module.banking.data.sepa.payment.SepaPaymentHistories

/**
 * Determines if the current SEPA payment is a candidate for processing in the next payment period.
 *
 * A payment is considered a candidate if:
 * - Its `sequenceType` is not in the forbidden sequence types list.
 * - There is no successor payment designated for the next period (`nextPeriodSuccessorId` is null).
 * - If the payment is a retry, its immediate predecessor has not failed (or there is no predecessor).
 *
 * @param history A collection of payment history objects that includes predecessors and successors of SEPA payments.
 * @param forbiddenSeqTypes A list of SEPA sequence types that are not allowed for processing in the next period.
 * @return `true` if the payment is a candidate for processing in the next period, `false` otherwise.
 */
fun SepaPayment.isCandidateForNextPeriodPayment(history:  SepaPaymentHistories, forbiddenSeqTypes: List<SepaSequenceType>): Boolean  =
    // Payments might be created if
    // the sequenceType is allowed
    sequenceType !in forbiddenSeqTypes &&
    // there is no regular next period payment and
    nextPeriodSuccessorId == null &&
    // if the payment is retried, we can create a new one, if it has no failing predecessors
    when(status){
        PaymentExecutionStatus.DROPPED -> false
        PaymentExecutionStatus.FAILED,
        PaymentExecutionStatus.PAYED_MANUALLY,
        PaymentExecutionStatus.CONFIRMED ->
            // Check if the payment has a direct failing predecessor
            history[sepaPaymentId]?.didNotFail()?:false &&
            // Check if the payment has other failing predecessors
            history.predecessorOf(sepaPaymentId)?.didNotFail() ?: true
        else -> true
    }
