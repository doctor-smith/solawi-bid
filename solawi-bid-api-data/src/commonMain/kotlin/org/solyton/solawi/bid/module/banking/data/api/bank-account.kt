package org.solyton.solawi.bid.module.banking.data.api

import kotlinx.serialization.Serializable
import org.evoleq.ktorx.client.Parameters
import org.evoleq.ktorx.client.QueryParams

typealias ApiBankAccount = BankAccount
typealias ApiBankAccounts = BankAccounts

@Serializable
data class BankAccounts(
    val all: List<BankAccount>
)

@Serializable
data class BankAccount(
    val id: String,
    val userId: String,
    val bic: String,
    val iban: String
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
    val id: String
)

@Serializable
data class CreateBankAccount(
    val userId: String,
    val bic: String,
    val iban: String
)

@Serializable
data class UpdateBankAccount(
    val id: String,
    val userId: String,
    val bic: String,
    val iban: String
)

@Serializable
data class DeleteBankAccount(
    val id: String
)
