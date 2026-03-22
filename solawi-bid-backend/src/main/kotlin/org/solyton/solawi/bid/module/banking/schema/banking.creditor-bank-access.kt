package org.solyton.solawi.bid.module.banking.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.jodatime.datetime
import org.solyton.solawi.bid.module.auditable.AuditableEntity
import org.solyton.solawi.bid.module.auditable.AuditableUUIDTable
import java.util.*

typealias CreditorBankAccessTable = CreditorBankAccess

object CreditorBankAccess : AuditableUUIDTable("creditor_bank_access") {
    val bankAccountId = reference("bank_account_id", BankAccounts)
    val accessType = enumerationByName("access_type", 20, CreditorBankAccessType::class) // PSD2, HBCI, EBICS
    val accessToken = text("access_token") // encrypted
    val tokenExpiry = datetime("token_expiry").nullable()

    init {
        uniqueIndex("uq_creditor_bankaccount_accesstype", bankAccountId, accessType)
    }
}

class CreditorBankAccessEntity(id: EntityID<UUID>) : UUIDEntity(id), AuditableEntity<UUID> {
    companion object : UUIDEntityClass<CreditorBankAccessEntity>(CreditorBankAccess)

    var bankAccount by BankAccount referencedOn CreditorBankAccess.bankAccountId
    var accessType by CreditorBankAccess.accessType
    var accessToken by CreditorBankAccess.accessToken
    var tokenExpiry by CreditorBankAccess.tokenExpiry

    override var createdAt: org.joda.time.DateTime by CreditorBankAccess.createdAt
    override var createdBy: UUID by CreditorBankAccess.createdBy
    override var modifiedAt: org.joda.time.DateTime? by CreditorBankAccess.modifiedAt
    override var modifiedBy: UUID? by CreditorBankAccess.modifiedBy
}

enum class CreditorBankAccessType {
    PSD2,
    HBCI,
    EBICS
}
