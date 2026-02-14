package org.solyton.solawi.bid.module.banking.service

import org.jetbrains.exposed.sql.Transaction
import org.solyton.solawi.bid.module.banking.exception.BankAccountsException
import org.solyton.solawi.bid.module.banking.schema.BankAccountEntity
import java.util.*

fun Transaction.validatedBankAccount(bankAccountId: UUID) =
    BankAccountEntity.findById(bankAccountId) ?: throw BankAccountsException.NoSuchBankAccount(bankAccountId.toString())
