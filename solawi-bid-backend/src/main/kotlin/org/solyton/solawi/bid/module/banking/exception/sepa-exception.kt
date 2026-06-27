package org.solyton.solawi.bid.module.banking.exception

sealed class SepaException(override val message: String): Exception(message) {

    data class MissingXmlSchema(val schema: String): SepaException("Missing xml schema: $schema")
    sealed class Transaction(override val message: String) : SepaException(message) {
        data class InvalidCreditorId(val creditorId: String): Transaction("Invalid creditor id: $creditorId")
        data class InvalidAmount(val amount: String): Transaction("Amount must be a positive number: $amount")
        @Suppress("UnusedPrivateMember")
        data object MissingMandateReference: Transaction("Missing mandate reference") {
            private fun readResolve(): Any = MissingMandateReference
        }

        @Suppress("UnusedPrivateMember")
        data object MissingEndToEndId: Transaction("Missing end to end id") {
            private fun readResolve(): Any = MissingEndToEndId
        }
        @Suppress("UnusedPrivateMember")
        data object MissingDebtorName: Transaction("Missing debtor name") {
            private fun readResolve(): Any = MissingDebtorName
        }
    }

    data class NoSuchSepaMandate(val id:String) : SepaException("No such sepa mandate; id = $id")

    data class CannotUpdateSepaMandate(val id: String, val reason: String) : SepaException("Cannot update sepa mandate; id = $id; reason = $reason")

    data class NoSuchSepaCollection(val id:String) : SepaException("No such sepa collection; id = $id")

    sealed class Payment(override val message: String) : SepaException(message) {
        data class CannotCreate(override val message: String): Payment(message)

        data class NoSuchPayment(val id: String): Payment("No such payment; id = $id")

        data class ChangesNotAllowed(val reason: String): Payment("Changes not allowed: $reason")

        data class StateTransitionForbidden(val from: String, val to: String): Payment(
            "Forbidden Payment State Transition: $from -> $to"
        )

        data object ChangeRequiresDateOfReport : Payment("Change requires date of report") {
            @Suppress("UnusedPrivateMember")
            private fun readResolve(): Any = ChangeRequiresDateOfReport
        }

        data class NoSuchTemplate(val id: String): Payment("No such payment template; id = $id")
    }
    sealed class Message(override val message: String) : SepaException(message) {
        data class NoSuchMessage(val id: String): Message("No such message; id = $id")
        data class Locked(val id: String): Message("Message is locked; id = $id")
    }
}
