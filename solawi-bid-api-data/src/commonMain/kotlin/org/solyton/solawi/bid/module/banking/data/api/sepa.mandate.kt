package org.solyton.solawi.bid.module.banking.data.api

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import org.evoleq.ktorx.client.Parameters
import org.evoleq.ktorx.client.QueryParams
import org.solyton.solawi.bid.module.banking.data.*

typealias ApiSepaMandate = SepaMandate
typealias ApiSepaMandates = SepaMandates
typealias ApiMandateStatus = MandateStatus

@Serializable
data class SepaMandates(
    val all: List<SepaMandate>
)

@Serializable
data class SepaMandate(
    val sepaMandateId: SepaMandateId,
    val debtorBankAccountId: BankAccountId,
    val debtorName: String,
    val mandateReference: MandateReference,
    val signedAt: LocalDateTime,
    val validFrom: LocalDateTime,
    val validUntil: LocalDateTime?,
    val lastUsedAt: LocalDateTime?,
    val status: MandateStatus = MandateStatus.ACTIVE,
    val isActive: Boolean = true,
    val amendmentOf: SepaMandateId? = null,
    val collectionId: SepaCollectionId? = null,
)

@Serializable
data class CreateSepaMandate(
    val creditorId: CreditorId,
    val debtorBankAccountId: BankAccountId,
    val debtorName: String,
    val mandateReference: MandateReference,
    val mandateReferencePrefix: MandateReferencePrefix?,
    val signedAt: LocalDateTime,
    val validFrom: LocalDateTime,
    val validUntil: LocalDateTime?,
    val status: MandateStatus = MandateStatus.ACTIVE,
    val isActive: Boolean = true,
    val amendmentOf: SepaMandateId? = null,
    val collectionId: SepaCollectionId? = null,
)

@Serializable
data class ReadSepaMandatesByCreditorsLegalEntity(
    /**
     * takes param
     * "legal_entity"
     */
    override val queryParams: QueryParams
) : Parameters()

@Serializable
data class UpdateSepaMandate(
    val sepaMandateId: SepaMandateId,
    val debtorBankAccountId: BankAccountId,
    val creditorId: CreditorId,
    val debtorName: String,
    val mandateReference: MandateReference,
    val signedAt: LocalDateTime,
    val validFrom: LocalDateTime,
    val validUntil: LocalDateTime?,
    val lastUsedAt: LocalDateTime?,
    val status: MandateStatus = MandateStatus.ACTIVE,
    val isActive: Boolean = true,
    val amendmentOf: SepaMandateId? = null,
    val collectionId: SepaCollectionId? = null,
)

@Serializable
data class AddSepaMandateToCollection(
    val sepaMandateId: SepaMandateId,
    val sepaCollectionId: SepaCollectionId
)

@Serializable
data class RemoveSepaMandateFromCollection(
    val sepaMandateId: SepaMandateId,
    val sepaCollectionId: SepaCollectionId
)

@Serializable
enum class MandateStatus {
    ACTIVE,        // valid and usable
    REVOKED,       // revoked by the customer (must not be used anymore)
    EXPIRED,       // expired (not used for 36 months)
    SUSPENDED      // temporarily disabled (can be reactivated)
}
