package org.solyton.solawi.bid.module.banking.repository

import org.jetbrains.exposed.sql.Transaction
import org.solyton.solawi.bid.module.banking.exception.BankAccountsException
import org.solyton.solawi.bid.module.banking.schema.CreditorIdentifierEntity
import java.util.*

fun Transaction.validatedCreditorIdentifier(id: UUID): CreditorIdentifierEntity =
    CreditorIdentifierEntity.findById(id)?: throw BankAccountsException.NoSuchCreditorIdentifier(id.toString())
