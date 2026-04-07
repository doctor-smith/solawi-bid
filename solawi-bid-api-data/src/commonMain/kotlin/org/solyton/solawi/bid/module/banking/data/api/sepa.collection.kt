package org.solyton.solawi.bid.module.banking.data.api

import kotlinx.serialization.Serializable
import org.evoleq.ktorx.client.Parameters
import org.evoleq.ktorx.client.QueryParams
import org.solyton.solawi.bid.module.banking.data.*

typealias ApiSepaCollection = SepaCollection
typealias ApiSepaCollections = SepaCollections
typealias ApiSepaSequenceType = SepaSequenceType

@Serializable
data class SepaCollections(
    val all: List<SepaCollection>
)

@Serializable
data class SepaCollection(
    val sepaCollectionId: SepaCollectionId,
    val creditorIdentifierId: CreditorIdentifierId,
    val creditorBankAccountId: BankAccountId,
    val mandateReferencePrefix: MandateReferencePrefix,
    val remittanceInformation: RemittanceInformation,
    val sepaSequenceType: SepaSequenceType,
    val localInstrument: LocalInstrument?,
    val chargeBearer: ChargeBearer = ChargeBearer("SLEV"),
    val requestedCollectionDay: Int? = null,
    val leadTimeDays: Int = 2,
    val purposeCode: PurposeCode? = null,
    val isActive: Boolean = true,
    val sepaMandates: SepaMandates? = null,
    val sepaPayments: SepaPayments? = null,
    val referenceIds: List<SepaCollectionReferenceId> = emptyList()
)

@Serializable
data class CreateSepaCollection(
    val creditorIdentifierId: CreditorIdentifierId,
    val creditorBankAccountId: BankAccountId,
    val mandateReferencePrefix: MandateReferencePrefix,
    val remittanceInformation: RemittanceInformation,
    val sepaSequenceType: SepaSequenceType,
    val localInstrument: LocalInstrument?,
    val chargeBearer: ChargeBearer = ChargeBearer("SLEV"),
    val requestedCollectionDay: Int? = null,
    val leadTimeDays: Int = 2,
    val purposeCode: PurposeCode? = null,
    val retryOnFailure: Boolean = false,
    val maxRetries: Int = 0,
    val isActive: Boolean = true,
    // not impl in the first step
    val sepaMandates: SepaMandates? = null,
    // not impl in the first step
    val sepaPayments: SepaPayments? = null,
    val referenceIds: List<SepaCollectionReferenceId> = emptyList()
)

@Serializable
data class UpdateSepaCollection(
    val sepaCollectionId: SepaCollectionId,
    val creditorIdentifierId: CreditorIdentifierId,
    val creditorAccountId: BankAccountId,
    val mandateReferencePrefix: MandateReferencePrefix,
    val remittanceInformation: RemittanceInformation,
    val sepaSequenceType: SepaSequenceType,
    val localInstrument: LocalInstrument?,
    val chargeBearer: ChargeBearer = ChargeBearer("SLEV"),
    val requestedCollectionDay: Int? = null,
    val leadTimeDays: Int = 2,
    val purposeCode: PurposeCode? = null,
    val isActive: Boolean = true,
    val referenceIds: List<SepaCollectionReferenceId> = emptyList()
)

@Serializable
data class ReadSepaCollectionsByLegalEntity(
    /**
     * takes param
     * "legal_entity"
     */
    override val queryParams: QueryParams
) : Parameters()


@Serializable
enum class SepaSequenceType {
    /**
     * FRST (First): First direct debit of a new mandate.
     * Used for the initial collection after the mandate signature.
     */
    FRST,

    /**
     * RCUR (Recurring): All subsequent direct debits after the first.
     * Typical for regular, recurring payments (e.g., monthly subscription).
     */
    RCUR,

    /**
     * OOFF (One-Off): One-time direct debit that is not recurring.
     * Used for special payments outside a recurring plan.
     */
    OOFF,

    /**
     * FNAL (Final): The final direct debit in a mandate cycle.
     * Used when a mandate ends or the last payment of a series is collected.
     */

    FNAL
}
