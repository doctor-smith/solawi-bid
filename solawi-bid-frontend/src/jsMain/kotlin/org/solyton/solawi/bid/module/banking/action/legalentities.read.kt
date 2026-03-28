package org.solyton.solawi.bid.module.banking.action

import org.evoleq.compose.Markup
import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.evoleq.optics.storage.suffixed
import org.solyton.solawi.bid.module.banking.data.api.ApiLegalEntity
import org.solyton.solawi.bid.module.banking.data.api.ReadLegalEntity
import org.solyton.solawi.bid.module.banking.data.application.BankingApplication
import org.solyton.solawi.bid.module.banking.data.application.legalEntity
import org.solyton.solawi.bid.module.banking.data.toDomainType


const val READ_PERSONAL_LEGAL_ENTITY = "ReadPersonalLegalEntity"

/**
 * Reads the personal legal entity associated with the specified party ID and updates the application state.
 *
 * @param partyId The identifier of the party whose personal legal entity information is to be retrieved.
 * @param nameSuffix Optional suffix to append to the action name for identification purposes. Defaults to null.
 */
@Markup
fun readPersonalLegalEntity(partyId: String, nameSuffix: String? = null) = Action<BankingApplication, ReadLegalEntity, ApiLegalEntity>(
    name = READ_PERSONAL_LEGAL_ENTITY.suffixed(nameSuffix),
    reader = {_ -> ReadLegalEntity(listOf("party" to partyId))},
    endPoint = ReadLegalEntity::class,
    writer = legalEntity.set contraMap { legalEntity -> legalEntity.toDomainType() }
)
