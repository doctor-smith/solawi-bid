package org.solyton.solawi.bid.module.banking.action

import org.evoleq.compose.Markup
import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.evoleq.optics.storage.suffixed
import org.solyton.solawi.bid.module.banking.data.api.ApiLegalEntity
import org.solyton.solawi.bid.module.banking.data.api.CreateLegalEntity
import org.solyton.solawi.bid.module.banking.data.application.BankingApplication
import org.solyton.solawi.bid.module.banking.data.application.legalEntity
import org.solyton.solawi.bid.module.banking.data.legalentity.LegalEntityType
import org.solyton.solawi.bid.module.banking.data.toApiType
import org.solyton.solawi.bid.module.banking.data.toDomainType
import org.solyton.solawi.bid.module.user.data.address.Address
import org.solyton.solawi.bid.module.user.data.transform.toApiType
import org.solyton.solawi.bid.module.values.LegalEntityId


const val CREATE_LEGAL_ENTITY = "CREATE_LEGAL_ENTITY"

@Markup
fun createLegalEntity(
    partyId: String,
    name: String,
    legalFrom: String,
    legalEntityType: LegalEntityType,
    address: Address,
    nameSuffix: String = ""
): Action<BankingApplication, CreateLegalEntity, ApiLegalEntity> = Action(
    name = CREATE_LEGAL_ENTITY.suffixed(nameSuffix),
    reader = { _ -> CreateLegalEntity(
        partyId = LegalEntityId(partyId),
        name = name,
        legalForm = legalFrom,
        legalEntityType = legalEntityType.toApiType(),
        address = address.toApiType()
    ) },
    endPoint = CreateLegalEntity::class,
    writer = legalEntity.set contraMap { fY: ApiLegalEntity -> fY.toDomainType()}
)
