package org.solyton.solawi.bid.module.banking.data.api

import kotlinx.serialization.Serializable
import org.evoleq.ktorx.client.Parameters
import org.evoleq.ktorx.client.QueryParams
import org.solyton.solawi.bid.module.banking.data.BIC
import org.solyton.solawi.bid.module.banking.data.BankAccountId
import org.solyton.solawi.bid.module.banking.data.IBAN
import org.solyton.solawi.bid.module.values.AccessorId
import org.solyton.solawi.bid.module.values.UserId
import org.solyton.solawi.bid.module.values.Username

typealias ApiBankAccount = BankAccount
typealias ApiBankAccounts = BankAccounts
typealias ApiAccountType = AccountType

@Serializable
enum class AccountType {
    CREDITOR, DEBTOR
}

@Serializable
data class BankAccounts(
    val all: List<BankAccount>
)

@Serializable
data class BankAccount(
    val id: BankAccountId,
    val userId: UserId,
    val bic: BIC,
    val iban: IBAN
)

@Serializable
data class ReadBankAccounts(
    /**
     * takes param
     * "legal_entity: UUID"
     */
    override val queryParams: QueryParams
) : Parameters()

@Serializable
data class ReadBankAccount(
    val id: BankAccountId
)

@Serializable
data class CreateBankAccount(
    val userId: UserId,
    val bic: BIC,
    val iban: IBAN,
    val accountHolder: String? = null,
    val isActive: Boolean = true,
    val accessType: AccountType = AccountType.DEBTOR,
)

@Serializable
data class UpdateBankAccount(
    val id: BankAccountId,
    val userId: UserId,
    val bic: BIC,
    val iban: IBAN
)

@Serializable
data class ImportBankAccounts(
    val override: Boolean = false,
    val accessorId: AccessorId,
    val bankAccounts: List<ImportBankAccount>
)

@Serializable
data class DeleteBankAccount(
    val id: BankAccountId
)

@Serializable
data class ImportBankAccount(
    val username: Username,
    val bic: BIC,
    val iban: IBAN
)
