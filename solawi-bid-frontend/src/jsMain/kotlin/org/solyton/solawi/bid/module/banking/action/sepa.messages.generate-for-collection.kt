package org.solyton.solawi.bid.module.banking.action

import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.evoleq.optics.storage.suffixed
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.module.banking.data.api.ApiSepaMessageString
import org.solyton.solawi.bid.module.banking.data.api.GenerateSepaMessageForCollection
import org.solyton.solawi.bid.module.banking.data.application.BankingApplication
import org.solyton.solawi.bid.module.banking.data.application.sepaModule
import org.solyton.solawi.bid.module.banking.data.sepa.sepaMessageString
import org.solyton.solawi.bid.module.banking.data.toDomainType


const val GENERATE_SEPA_MESSAGE_FOR_COLLECTION = "GENERATE_SEPA_MESSAGE_FOR_COLLECTION"

fun generateSepaMessageForCollection(
    data: GenerateSepaMessageForCollection,
    nameSuffix: String = ""
): Action<BankingApplication, GenerateSepaMessageForCollection, ApiSepaMessageString> = Action(
    name = GENERATE_SEPA_MESSAGE_FOR_COLLECTION.suffixed(nameSuffix),
    reader = { _ -> data },
    endPoint = GenerateSepaMessageForCollection::class,
    writer = (sepaModule * sepaMessageString.set) contraMap { message: ApiSepaMessageString -> message.toDomainType()}
)
