package org.solyton.solawi.bid.module.banking.data.sepa


enum class SepaSequenceType {
    /**
     * FRST (First): First direct debit of a new mandate.
     * Used for the initial collection after the mandate signature.
     */
    FRST,

    /**
     * RCUR (Recurring): All subsequent direct debits after the first.
     * Typical for regular, recurring payments (e.g., monthly subscription).
     */
    RCUR,

    /**
     * OOFF (One-Off): One-time direct debit that is not recurring.
     * Used for special payments outside a recurring plan.
     */
    OOFF,

    /**
     * FNAL (Final): The final direct debit in a mandate cycle.
     * Used when a mandate ends or the last payment of a series is collected.
     */

    FNAL,

    /**
     *
     */
    UNCLEAR
}

enum class MandateStatus {
    ACTIVE,        // valid and usable
    REVOKED,       // revoked by the customer (must not be used anymore)
    EXPIRED,       // expired (not used for 36 months)
    SUSPENDED      // temporarily disabled (can be reactivated)
}

enum class PaymentExecutionStatus {
    /**
     * CREATED: Payment has been created in the system but not yet sent to the bank.
     */
    CREATED,
    /**
     * MESSAGE_CREATED: Sepa Message has been created and PAIN.008 has been delivered to the client
     */
    MESSAGE_CREATED,
    /**
     * SENT: Payment has been submitted to the bank for processing.
     */
    SENT,

    /**
     * CONFIRMED: Bank confirmed that the payment has been successfully executed.
     */
    CONFIRMED,

    /**
     * FAILED: Bank rejected or returned the payment (e.g., insufficient funds, invalid IBAN).
     * The reason can be found in Payment.failureReason or BankStatus.
     */
    FAILED,

    /**
     * PENDING: Payment is under review by the bank or awaiting settlement.
     */
    PENDING
}

