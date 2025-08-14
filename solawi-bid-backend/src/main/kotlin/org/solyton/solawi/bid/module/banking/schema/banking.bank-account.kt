package org.solyton.solawi.bid.module.banking.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.auditable.AuditableEntity
import org.solyton.solawi.bid.module.auditable.AuditableUUIDTable
import java.util.*

typealias BankAccountsTable = BankAccounts
typealias BankAccountEntity = BankAccount

object BankAccounts : AuditableUUIDTable("bank_accounts") {
    val iban = varchar("iban", 30)
    val bic = varchar("bic", 20)
    val userId = uuid("user_id")
}

class BankAccount(id: EntityID<UUID>) : UUIDEntity(id), AuditableEntity<UUID> {
    companion object : UUIDEntityClass<BankAccount>(BankAccounts)

    var iban by BankAccounts.iban
    var bic by BankAccounts.bic
    var userId by BankAccounts.userId

    override var createdAt: DateTime by BankAccounts.createdAt
    override var createdBy: UUID by BankAccounts.createdBy
    override var modifiedAt: DateTime? by BankAccounts.modifiedAt
    override var modifiedBy: UUID? by BankAccounts.modifiedBy
}
