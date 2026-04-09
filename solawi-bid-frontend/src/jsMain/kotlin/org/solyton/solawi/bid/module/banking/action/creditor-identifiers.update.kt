package org.solyton.solawi.bid.module.banking.action

import kotlinx.datetime.LocalDate
import org.evoleq.compose.Markup
import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.evoleq.optics.storage.suffixed
import org.solyton.solawi.bid.module.banking.data.CreditorId
import org.solyton.solawi.bid.module.banking.data.CreditorIdentifierId
import org.solyton.solawi.bid.module.banking.data.api.ApiCreditorIdentifier
import org.solyton.solawi.bid.module.banking.data.api.UpdateCreditorIdentifier
import org.solyton.solawi.bid.module.banking.data.application.BankingApplication
import org.solyton.solawi.bid.module.banking.data.application.creditorIdentifier
import org.solyton.solawi.bid.module.banking.data.toDomainType
import org.solyton.solawi.bid.module.values.LegalEntityId

const val UPDATE_CREDITOR_IDENTIFIER = "UPDATE_CREDITOR_IDENTIFIER"

@Markup
fun updateCreditorIdentifier(
    creditorIdentifierId: CreditorIdentifierId,
    legalEntityId: LegalEntityId,
    creditorId: CreditorId,
    validFrom: LocalDate,
    validUntil: LocalDate?,
    isActive: Boolean,
    nameSuffix: String = ""
): Action<BankingApplication, UpdateCreditorIdentifier, ApiCreditorIdentifier> = Action(
    name = UPDATE_CREDITOR_IDENTIFIER.suffixed(nameSuffix),
    reader = { _ -> UpdateCreditorIdentifier(
        creditorIdentifierId = creditorIdentifierId,
        legalEntityId = legalEntityId,
        creditorId = creditorId,
        validFrom = validFrom,
        validUntil = validUntil,
        isActive = isActive
    )
    },
    endPoint = UpdateCreditorIdentifier::class,
    writer = creditorIdentifier.set contraMap { fY: ApiCreditorIdentifier -> fY.toDomainType()}
)
