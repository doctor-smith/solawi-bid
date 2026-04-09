package org.solyton.solawi.bid.module.banking.action

import kotlinx.datetime.LocalDate
import org.evoleq.compose.Markup
import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.evoleq.optics.storage.suffixed
import org.solyton.solawi.bid.module.banking.data.CreditorId
import org.solyton.solawi.bid.module.banking.data.api.ApiCreditorIdentifier
import org.solyton.solawi.bid.module.banking.data.api.CreateCreditorIdentifier
import org.solyton.solawi.bid.module.banking.data.application.BankingApplication
import org.solyton.solawi.bid.module.banking.data.application.creditorIdentifier
import org.solyton.solawi.bid.module.banking.data.toDomainType
import org.solyton.solawi.bid.module.values.LegalEntityId


const val CREATE_CREDITOR_IDENTIFIER = "CREATE_CREDITOR_IDENTIFIER"

@Markup
fun createCreditorIdentifier(
    legalEntityId: LegalEntityId,
    creditorId: CreditorId,
    validFrom: LocalDate,
    validUntil: LocalDate?,
    isActive: Boolean,
    nameSuffix: String = ""
): Action<BankingApplication, CreateCreditorIdentifier, ApiCreditorIdentifier> = Action(
    name = CREATE_CREDITOR_IDENTIFIER.suffixed(nameSuffix),
    reader = { _ -> CreateCreditorIdentifier(
            legalEntityId = legalEntityId,
            creditorId = creditorId,
            validFrom = validFrom,
            validUntil = validUntil,
            isActive = isActive
        )
    },
    endPoint = CreateCreditorIdentifier::class,
    writer = creditorIdentifier.set contraMap { fY: ApiCreditorIdentifier -> fY.toDomainType()}
)
