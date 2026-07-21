package org.solyton.solawi.bid.module.banking.action

import org.evoleq.compose.Markup
import org.evoleq.math.contraMap
import org.evoleq.optics.lens.times
import org.evoleq.optics.storage.Action
import org.evoleq.optics.storage.suffixed
import org.solyton.solawi.bid.module.banking.data.api.ApiSepaPaymentLinks
import org.solyton.solawi.bid.module.banking.data.api.ReadSepaPaymentLinksByLegalEntity
import org.solyton.solawi.bid.module.banking.data.application.BankingApplication
import org.solyton.solawi.bid.module.banking.data.application.sepaModule
import org.solyton.solawi.bid.module.banking.data.sepa.sepaPaymentLinks
import org.solyton.solawi.bid.module.banking.data.toDomainType
import org.solyton.solawi.bid.module.values.LegalEntityId

const val READ_SEPA_PAYMENT_LINKS_BY_LEGAL_ENTITY = "READ_SEPA_PAYMENT_LINKS_BY_LEGAL_ENTITY"

@Markup
fun readSepaPaymentLInksByLegalEntity(legalEntityId: LegalEntityId, nameSuffix: String? = null) = Action<BankingApplication, ReadSepaPaymentLinksByLegalEntity, ApiSepaPaymentLinks>(
    name = READ_SEPA_PAYMENT_LINKS_BY_LEGAL_ENTITY.suffixed(nameSuffix),
    reader = {_ -> ReadSepaPaymentLinksByLegalEntity(listOf("legal_entity" to legalEntityId.value)) },
    endPoint = ReadSepaPaymentLinksByLegalEntity::class,
    writer = (sepaModule * sepaPaymentLinks).set contraMap { links -> links.toDomainType() }
)
