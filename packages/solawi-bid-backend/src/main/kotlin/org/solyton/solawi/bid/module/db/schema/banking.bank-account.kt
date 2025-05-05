package org.solyton.solawi.bid.module.db.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import java.util.UUID

typealias BankAccountsTable = BankAccounts
typealias BankAccountEntity = BankAccount

object BankAccounts : UUIDTable("bank_accounts") {
    val iban = varchar("iban", 30)
    val bic = varchar("bic", 20)
}

class BankAccount(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<BankAccount>(BankAccounts)

    var iban by BankAccounts.iban
    var bic by BankAccounts.bic
}