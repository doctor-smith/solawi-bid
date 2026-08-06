package org.solyton.solawi.bid.module.banking.action.sepa

import org.evoleq.compose.Markup
import org.evoleq.math.contraMap
import org.evoleq.optics.lens.times
import org.evoleq.optics.storage.Action
import org.evoleq.optics.storage.suffixed
import org.solyton.solawi.bid.module.banking.data.api.ApiSepaMandates
import org.solyton.solawi.bid.module.banking.data.api.ReadPersonalSepaMandates
import org.solyton.solawi.bid.module.banking.data.application.BankingApplication
import org.solyton.solawi.bid.module.banking.data.application.sepaModule
import org.solyton.solawi.bid.module.banking.data.sepa.sepaMandates
import org.solyton.solawi.bid.module.banking.data.toDomainType


const val READ_PERSONAL_SEPA_MANDATES = "READ_PERSONAL_SEPA_MANDATES"

@Markup
fun readPersonalSepaMandates(nameSuffix: String? = null) = Action<BankingApplication, ReadPersonalSepaMandates, ApiSepaMandates>(
    name = READ_PERSONAL_SEPA_MANDATES.suffixed(nameSuffix),
    reader = {_ -> ReadPersonalSepaMandates()},
    endPoint = ReadPersonalSepaMandates::class,
    writer = (sepaModule * sepaMandates).set contraMap { messages -> messages.toDomainType() }
)
