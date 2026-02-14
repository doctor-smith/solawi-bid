package org.solyton.solawi.bid.module.banking.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import java.util.*

typealias BankAccountAccessorsTable = BankAccountAccessors
typealias BankAccountAccessorEntity = BankAccountAccessor

object BankAccountAccessors : UUIDTable("bank_account_accessors") {
    val bankAccountId = reference("bank_account_id", BankAccountsTable)
    val accessorId = uuid("accessor_id")

    init {
        uniqueIndex(bankAccountId, accessorId)
    }
}

class BankAccountAccessor(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<BankAccountAccessor>(BankAccountAccessors)

    var bankAccount by BankAccount referencedOn BankAccountAccessors.bankAccountId
    var accessorId by BankAccountAccessors.accessorId
}
