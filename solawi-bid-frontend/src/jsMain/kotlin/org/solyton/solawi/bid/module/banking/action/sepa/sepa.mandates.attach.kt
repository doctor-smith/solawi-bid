package org.solyton.solawi.bid.module.banking.action.sepa

import org.evoleq.math.contraMap
import org.evoleq.optics.lens.times
import org.evoleq.optics.storage.Action
import org.evoleq.optics.storage.suffixed
import org.evoleq.optics.transform.update
import org.solyton.solawi.bid.module.banking.data.api.AttachSepaMandateReferenceData
import org.solyton.solawi.bid.module.banking.data.api.SepaMandate
import org.solyton.solawi.bid.module.banking.data.application.BankingApplication
import org.solyton.solawi.bid.module.banking.data.application.sepaModule
import org.solyton.solawi.bid.module.banking.data.sepa.sepaMandates
import org.solyton.solawi.bid.module.banking.data.toDomainType

const val ATTACH_SEPA_MANDATE_REFERENCE_DATA = "ATTACH_SEPA_MANDATE_REFERENCE_DATA"

fun attachSepaMandateReferenceData(
    data: AttachSepaMandateReferenceData,
    nameSuffix: String? = null
): Action<BankingApplication, AttachSepaMandateReferenceData, SepaMandate> = Action(
    name = ATTACH_SEPA_MANDATE_REFERENCE_DATA.suffixed(nameSuffix),
    reader = {data},
    endPoint = AttachSepaMandateReferenceData::class,
    writer = (sepaModule * sepaMandates).update{
        p,q -> p.sepaMandateId == q.sepaMandateId
    } contraMap {mandate -> mandate.toDomainType()}
)
