package org.solyton.solawi.bid.module.banking.action

import org.evoleq.compose.Markup
import org.evoleq.math.contraMap
import org.evoleq.optics.lens.times
import org.evoleq.optics.storage.Action
import org.evoleq.optics.storage.suffixed
import org.solyton.solawi.bid.module.banking.data.api.ApiSepaMessages
import org.solyton.solawi.bid.module.banking.data.api.ReadSepaMessagesByLegalEntityId
import org.solyton.solawi.bid.module.banking.data.application.BankingApplication
import org.solyton.solawi.bid.module.banking.data.application.sepaModule
import org.solyton.solawi.bid.module.banking.data.sepa.sepaMessages
import org.solyton.solawi.bid.module.banking.data.toDomainType
import org.solyton.solawi.bid.module.values.LegalEntityId

const val READ_SEPA_MESSAGES_BY_LEGAL_ENTITY = "READ_SEPA_MESSAGES_BY_LEGAL_ENTITY"

@Markup
fun readSepaMessagesByLegalEntity(legalEntityId: LegalEntityId, nameSuffix: String? = null) = Action<BankingApplication, ReadSepaMessagesByLegalEntityId, ApiSepaMessages>(
    name = READ_SEPA_MESSAGES_BY_LEGAL_ENTITY.suffixed(nameSuffix),
    reader = {_ -> ReadSepaMessagesByLegalEntityId(listOf("legal_entity" to legalEntityId.value))},
    endPoint = ReadSepaMessagesByLegalEntityId::class,
    writer = (sepaModule * sepaMessages).set contraMap { messages -> messages.toDomainType() }
)
