package org.solyton.solawi.bid.module.banking.action.sepa

import org.evoleq.compose.Markup
import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.evoleq.optics.storage.suffixed
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.module.banking.data.SepaMessageId
import org.solyton.solawi.bid.module.banking.data.api.ApiSepaMessageString
import org.solyton.solawi.bid.module.banking.data.api.DownloadSepaMessage
import org.solyton.solawi.bid.module.banking.data.application.BankingApplication
import org.solyton.solawi.bid.module.banking.data.application.sepaModule
import org.solyton.solawi.bid.module.banking.data.sepa.sepaMessageString
import org.solyton.solawi.bid.module.banking.data.toDomainType

const val DOWNLOAD_SEPA_MESSAGE = "DOWNLOAD_SEPA_MESSAGE"


@Markup
fun downloadSepaMessage(
    sepaMessageId: SepaMessageId,
    nameSuffix: String? = null
) = Action<BankingApplication, DownloadSepaMessage, ApiSepaMessageString>(
    name = DOWNLOAD_SEPA_MESSAGE.suffixed(nameSuffix),
    reader = {_ -> DownloadSepaMessage(listOf("message_id" to sepaMessageId.value))},
    endPoint = DownloadSepaMessage::class,
    writer = (sepaModule * sepaMessageString.set) contraMap { message: ApiSepaMessageString -> message.toDomainType()}
)
