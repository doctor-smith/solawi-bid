package org.solyton.solawi.bid.module.banking.data.internal

import org.jetbrains.exposed.sql.jodatime.Date
import org.joda.time.DateTime
import org.joda.time.LocalDate
import java.util.*

import org.solyton.solawi.bid.module.banking.schema.MandateStatus
import org.solyton.solawi.bid.module.banking.schema.SepaSequenceType
import java.math.BigDecimal

data class SepaMandateCreationRequest(
    val creditorId: UUID,
    val debtorBankAccountId: UUID,
    val debtorName: String,
    val signedAt: DateTime? = null,
    val validFrom: DateTime? = null,
    val validUntil: DateTime? = null,
    val customMandateReference: String
)



data class SepaMandateResponse(
    val id: UUID,
    val creditorId: UUID,
    val debtorBankAccountId: UUID,
    val debtorName: String,
    val mandateReference: String,
    val signedAt: DateTime,
    val status: MandateStatus,
    val validFrom: DateTime,
    val validUntil: DateTime?,
    val lastUsedAt: DateTime?,
    val isActive: Boolean,
    val createdAt: DateTime,
    val modifiedAt: DateTime?
)


data class Pain008GenerationRequest(
    val creditorId: UUID,
    val creditorAccountId: UUID,
    val executionDate: LocalDate,
    val transactions: List<Pain008Transaction>,
    val createdBy: UUID,
)


data class Pain008Transaction(
    val endToEndId: String,                    // Eindeutige Transaktions-ID
    val amount: BigDecimal,                    // Betrag in EUR
    val debtorName: String,                    // Name des Schuldners
    val debtorIban: String,                    // IBAN des Schuldners
    val debtorBic: String = "",                // BIC des Schuldners (optional)
    val mandateReference: String,              // Mandatsreferenz
    val mandateSignDate: LocalDate,            // Datum der Mandatsunterzeichnung
    val remittanceInfo: String = "",            // Verwendungszweck (optional)
    val sequenceType: SepaSequenceType
)

