package org.solyton.solawi.bid.module.banking.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.jodatime.datetime
import org.solyton.solawi.bid.module.auditable.AuditableEntity
import org.solyton.solawi.bid.module.auditable.AuditableUUIDTable
import java.util.*

typealias DebtorBankAccessTable = DebtorBankAccess

object DebtorBankAccess : AuditableUUIDTable("debtor_bank_access") {
    val bankAccountId = reference("bank_account_id", BankAccounts) // links to DEBTOR account
    val accessType = enumerationByName("access_type", 20, DebtorBankAccessType::class) // PSD2, HBCI
    val accessToken = text("access_token") // encrypted, read-only access
    val tokenExpiry = datetime("token_expiry").nullable()

    init {
        uniqueIndex("uq_debtor_bankaccount_accesstype", bankAccountId, accessType)
    }
}

class DebtorBankAccessEntity(id: EntityID<UUID>) : UUIDEntity(id), AuditableEntity<UUID> {
    companion object : UUIDEntityClass<DebtorBankAccessEntity>(DebtorBankAccess)

    var bankAccount by BankAccount referencedOn DebtorBankAccess.bankAccountId
    var accessType by DebtorBankAccess.accessType
    var accessToken by DebtorBankAccess.accessToken
    var tokenExpiry by DebtorBankAccess.tokenExpiry

    override var createdAt: org.joda.time.DateTime by DebtorBankAccess.createdAt
    override var createdBy: UUID by DebtorBankAccess.createdBy
    override var modifiedAt: org.joda.time.DateTime? by DebtorBankAccess.modifiedAt
    override var modifiedBy: UUID? by DebtorBankAccess.modifiedBy
}

enum class DebtorBankAccessType {
    PSD2,
    HBCI
}
