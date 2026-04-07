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

    }

    data class NoSuchSepaMandate(val id:String) : SepaException("No such sepa mandate; id = $id")

    data class CannotUpdateSepaMandate(val id: String, val reason: String) : SepaException("Cannot update sepa mandate; id = $id; reason = $reason")

    data class NoSuchSepaCollection(val id:String) : SepaException("No such sepa collection; id = $id")
}
