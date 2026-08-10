package org.solyton.solawi.bid.module.banking.action.sepa

import kotlinx.datetime.LocalDate
import org.evoleq.compose.Markup
import org.evoleq.math.contraMap
import org.evoleq.optics.lens.times
import org.evoleq.optics.storage.Action
import org.evoleq.optics.storage.suffixed
import org.evoleq.optics.transform.upsertAll
import org.solyton.solawi.bid.module.banking.data.RemittanceInformation
import org.solyton.solawi.bid.module.banking.data.SepaMessageId
import org.solyton.solawi.bid.module.banking.data.api.ApiSepaMessages
import org.solyton.solawi.bid.module.banking.data.api.MergeSepaMessages
import org.solyton.solawi.bid.module.banking.data.application.BankingApplication
import org.solyton.solawi.bid.module.banking.data.application.sepaModule
import org.solyton.solawi.bid.module.banking.data.sepa.sepaMessages
import org.solyton.solawi.bid.module.banking.data.toDomainType


const val MERGE_SEPA_MESSAGES = "MERGE_SEPA_MESSAGES"

@Markup
fun mergeSepaMessages(
    sepaMessageIds: List<SepaMessageId>,
    executionDate: LocalDate,
    remittanceInformation: RemittanceInformation,
    nameSuffix: String? = null
) = Action<BankingApplication, MergeSepaMessages, ApiSepaMessages>(
    name = MERGE_SEPA_MESSAGES.suffixed(nameSuffix),
    reader = {_ -> MergeSepaMessages(sepaMessageIds, executionDate, remittanceInformation)},
    endPoint = MergeSepaMessages::class,
    writer = (sepaModule * sepaMessages).upsertAll{
        p,q -> p.sepaMessageId == q.sepaMessageId
    } contraMap { messages -> messages.toDomainType() }
)
