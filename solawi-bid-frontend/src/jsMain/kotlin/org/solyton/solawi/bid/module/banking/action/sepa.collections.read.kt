package org.solyton.solawi.bid.module.banking.action

import org.evoleq.compose.Markup
import org.evoleq.math.contraMap
import org.evoleq.optics.lens.times
import org.evoleq.optics.storage.Action
import org.evoleq.optics.storage.suffixed
import org.solyton.solawi.bid.module.banking.data.api.ApiSepaCollections
import org.solyton.solawi.bid.module.banking.data.api.ReadSepaCollectionsByLegalEntity
import org.solyton.solawi.bid.module.banking.data.application.BankingApplication
import org.solyton.solawi.bid.module.banking.data.application.sepaModule
import org.solyton.solawi.bid.module.banking.data.sepa.sepaCollections
import org.solyton.solawi.bid.module.banking.data.toDomainType
import org.solyton.solawi.bid.module.values.LegalEntityId

const val READ_PERSONAL_SEPA_COLLECTIONS = "READ_PERSONAL_SEPA_COLLECTIONS"

@Markup
fun readPersonalSepaCollections(legalEntityId: LegalEntityId, nameSuffix: String? = null) = Action<BankingApplication, ReadSepaCollectionsByLegalEntity, ApiSepaCollections>(
    name = READ_PERSONAL_SEPA_COLLECTIONS.suffixed(nameSuffix),
    reader = {_ -> ReadSepaCollectionsByLegalEntity(listOf("legal_entity" to legalEntityId.value))},
    endPoint = ReadSepaCollectionsByLegalEntity::class,
    writer = (sepaModule * sepaCollections).set contraMap { collections -> collections.all.map { it.toDomainType() } }
)
