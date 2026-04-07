package org.solyton.solawi.bid.module.banking.exception

sealed class BankAccountsException(override val message: String): Exception(message) {
    data class NoSuchBankAccount(val id: String) : BankAccountsException("No such bank account: id = $id")
    data class InvalidBic(val bic: String): BankAccountsException("Invalid BIC: $bic")

    data class InvalidBicCountryCode(val bic: String): BankAccountsException("Invalid BIC country code: $bic")

    data class BicNotInEU(val bic: String): BankAccountsException("BIC not in EU: $bic")
    data class InvalidIban(val iban: String): BankAccountsException("Invalid IBAN format: $iban")

    data class NoSuchCreditorId(val creditorId: String): BankAccountsException("No such creditor id: $creditorId")

    data class NoSuchCreditorIdentifier(val creditorIdentifierId: String): BankAccountsException("No such creditor identifier, id: $creditorIdentifierId")

    data class CannotCreateMandateReference(val reason: String): BankAccountsException("Cannot create mandate reference: $reason")

    data class NoSuchCreditorBankAccountAccess(val id: String): BankAccountsException("No such creditor bank account accessor: id = $id")

    data class NoSuchDebtorBankAccountAccess(val id: String): BankAccountsException("No such debtor bank account accessor: id = $id")
}
