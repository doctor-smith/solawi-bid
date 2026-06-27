package org.solyton.solawi.bid.module.banking.action

import org.evoleq.compose.Markup
import org.evoleq.math.contraMap
import org.evoleq.optics.lens.times
import org.evoleq.optics.storage.Action
import org.evoleq.optics.storage.suffixed
import org.solyton.solawi.bid.module.banking.data.api.ApiSepaMandates
import org.solyton.solawi.bid.module.banking.data.api.ReadSepaMandatesByCreditorsLegalEntity
import org.solyton.solawi.bid.module.banking.data.application.BankingApplication
import org.solyton.solawi.bid.module.banking.data.application.sepaModule
import org.solyton.solawi.bid.module.banking.data.sepa.sepaMandates
import org.solyton.solawi.bid.module.banking.data.toDomainType
import org.solyton.solawi.bid.module.values.LegalEntityId

const val READ_SEPA_MANDATES_BY_CREDITORS_LEGAL_ENTITY = "READ_SEPA_MANDATES_BY_CREDITORS_LEGAL_ENTITY"

@Markup
fun readSepaMandatesByCreditorsLegalEntity(legalEntityId: LegalEntityId, nameSuffix: String? = null) = Action<BankingApplication, ReadSepaMandatesByCreditorsLegalEntity, ApiSepaMandates>(
    name = READ_SEPA_MANDATES_BY_CREDITORS_LEGAL_ENTITY.suffixed(nameSuffix),
    reader = {_ -> ReadSepaMandatesByCreditorsLegalEntity(listOf("legal_entity" to legalEntityId.value))},
    endPoint = ReadSepaMandatesByCreditorsLegalEntity::class,
    writer = (sepaModule * sepaMandates).set contraMap { messages -> messages.toDomainType() }
)
