package org.solyton.solawi.bid.module.banking.data.api

import kotlinx.serialization.Serializable

typealias ApiBankAccount = BankAccount

@Serializable
data class BankAccount(
    val id: String,
    val userId: String,
    val bic: String,
    val iban: String
)

@Serializable
data object ReadBankAccounts

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
    val bic: String,
    val iban: String
)

@Serializable
data class DeleteBankAccount(
    val id: String
)
