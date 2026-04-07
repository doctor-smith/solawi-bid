package org.solyton.solawi.bid.module.banking.action

import org.evoleq.compose.Markup
import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.evoleq.optics.storage.suffixed
import org.solyton.solawi.bid.module.banking.data.api.ApiCreditorIdentifier
import org.solyton.solawi.bid.module.banking.data.api.ReadCreditorIdentifierByLegalEntity
import org.solyton.solawi.bid.module.banking.data.application.BankingApplication
import org.solyton.solawi.bid.module.banking.data.application.creditorIdentifier
import org.solyton.solawi.bid.module.banking.data.toDomainType
import org.solyton.solawi.bid.module.values.LegalEntityId


const val READ_PERSONAL_CREDITOR_IDENTIFIER = "ReadPersonalCreditorIdentifier"

/**
 * Reads the personal creditor identifier associated with the specified legal entity ID and updates the application state.
 *
 * @param legalEntityId The legal entity requesting the information.
 * @param nameSuffix Optional suffix to append to the action name for identification purposes. Defaults to null.
 */
@Markup
fun readPersonalCreditorIdentifier(legalEntityId: LegalEntityId, nameSuffix: String? = null) = Action<BankingApplication, ReadCreditorIdentifierByLegalEntity, ApiCreditorIdentifier>(
    name = READ_PERSONAL_LEGAL_ENTITY.suffixed(nameSuffix),
    reader = {_ -> ReadCreditorIdentifierByLegalEntity(listOf("legal_entity" to legalEntityId.value))},
    endPoint = ReadCreditorIdentifierByLegalEntity::class,
    writer = creditorIdentifier.set contraMap { creditorIdentifier -> creditorIdentifier.toDomainType() }
)
