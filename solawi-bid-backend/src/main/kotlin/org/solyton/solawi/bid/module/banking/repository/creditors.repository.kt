package org.solyton.solawi.bid.module.banking.repository

import org.jetbrains.exposed.sql.Transaction
import org.solyton.solawi.bid.module.banking.data.CreditorId
import org.solyton.solawi.bid.module.banking.exception.BankAccountsException
import org.solyton.solawi.bid.module.banking.schema.CreditorIdentifierEntity
import org.solyton.solawi.bid.module.banking.schema.CreditorIdentifiersTable
import java.util.*

fun Transaction.validatedCreditorIdentifier(id: UUID): CreditorIdentifierEntity =
    CreditorIdentifierEntity.findById(id)?: throw BankAccountsException.NoSuchCreditorIdentifier(id.toString())

fun Transaction.validatedCreditorIdentifier(id: CreditorId): CreditorIdentifierEntity =
    CreditorIdentifierEntity.find{
        CreditorIdentifiersTable.creditorId eq id.value
    }.firstOrNull()?: throw BankAccountsException.NoSuchCreditor(id.toString())
