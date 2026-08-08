package org.solyton.solawi.bid.module.banking.action.sepa

import org.evoleq.compose.Markup
import org.evoleq.math.contraMap
import org.evoleq.optics.lens.times
import org.evoleq.optics.storage.Action
import org.evoleq.optics.storage.suffixed
import org.evoleq.optics.transform.update
import org.solyton.solawi.bid.module.banking.data.SepaMessageId
import org.solyton.solawi.bid.module.banking.data.api.ApiSepaMessage
import org.solyton.solawi.bid.module.banking.data.api.UpdateSepaMessageStatus
import org.solyton.solawi.bid.module.banking.data.application.BankingApplication
import org.solyton.solawi.bid.module.banking.data.application.sepaModule
import org.solyton.solawi.bid.module.banking.data.sepa.message.SepaMessageStatus
import org.solyton.solawi.bid.module.banking.data.sepa.sepaMessages
import org.solyton.solawi.bid.module.banking.data.toApiType
import org.solyton.solawi.bid.module.banking.data.toDomainType


const val UPDATE_SEPA_MESSAGE_STATUS = "UPDATE_SEPA_MESSAGE_STATUS"

@Markup
fun updateSepaMessagesStatus(
    sepaMessageId: SepaMessageId,
    newStatus: SepaMessageStatus,
    nameSuffix: String? = null
) = Action<BankingApplication, UpdateSepaMessageStatus, ApiSepaMessage>(
    name = UPDATE_SEPA_MESSAGE_STATUS.suffixed(nameSuffix),
    reader = {_ -> UpdateSepaMessageStatus(sepaMessageId, newStatus.toApiType())},
    endPoint = UpdateSepaMessageStatus::class,
    writer = (sepaModule * sepaMessages).update{
        p,q -> p.sepaMessageId == q.sepaMessageId
    } contraMap { messages -> messages.toDomainType() }
)
