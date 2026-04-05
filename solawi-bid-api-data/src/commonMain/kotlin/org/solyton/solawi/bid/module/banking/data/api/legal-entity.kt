package org.solyton.solawi.bid.module.banking.data.api

import kotlinx.serialization.Serializable
import org.evoleq.ktorx.client.Parameters
import org.evoleq.ktorx.client.QueryParams
import org.solyton.solawi.bid.module.authentication.data.api.Identifier
import org.solyton.solawi.bid.module.user.data.api.userprofile.Address
import org.solyton.solawi.bid.module.values.LegalEntityId
import org.solyton.solawi.bid.module.values.ProviderId

typealias ApiLegalEntity = LegalEntity
typealias ApiLegalEntityType = LegalEntityType


@Serializable
data class LegalEntities(val all: List<LegalEntity>)

@Serializable
data class LegalEntity(
    val legalEntityId: LegalEntityId,
    val partyId: LegalEntityId,
    val name: String,
    val legalForm: String?,
    val legalEntityType: LegalEntityType,
    val address: Address,
)

@Serializable
enum class LegalEntityType {
    HUMAN,
    ORGANIZATION
}


@Serializable
data class CreateLegalEntity(
    val partyId: LegalEntityId,
    val name: String,
    val legalForm: String?,
    val legalEntityType: LegalEntityType,
    val address: Address,
)

@Serializable
data class ReadLegalEntity(
   /**
    * takes param
    * "party: UUID"
    */
    override val queryParams: QueryParams
) : Parameters()

@Serializable
data class ReadLegalEntitiesOfProvider(val providerId: ProviderId)

@Serializable
data class UpdateLegalEntity(
    val legalEntityId: LegalEntityId,
    val partyId: LegalEntityId,
    val name: String,
    val legalForm: String,
    val legalEntityType: LegalEntityType,
    val address: Address,
)

@Serializable
data class DeleteLegalEntity(val legalEntityId: LegalEntityId)
