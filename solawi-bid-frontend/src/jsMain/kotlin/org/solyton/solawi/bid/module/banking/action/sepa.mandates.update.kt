package org.solyton.solawi.bid.module.banking.action

import org.evoleq.math.contraMap
import org.evoleq.optics.lens.FirstBy
import org.evoleq.optics.lens.times
import org.evoleq.optics.storage.Action
import org.evoleq.optics.storage.suffixed
import org.evoleq.optics.transform.update
import org.solyton.solawi.bid.module.banking.data.SepaCollectionId
import org.solyton.solawi.bid.module.banking.data.api.ApiSepaMandate
import org.solyton.solawi.bid.module.banking.data.api.UpdateSepaMandate
import org.solyton.solawi.bid.module.banking.data.application.BankingApplication
import org.solyton.solawi.bid.module.banking.data.application.sepaModule
import org.solyton.solawi.bid.module.banking.data.sepa.collection.sepaMandates as sepaMandatesOfCollection
import org.solyton.solawi.bid.module.banking.data.sepa.sepaCollections
import org.solyton.solawi.bid.module.banking.data.sepa.sepaMandates
import org.solyton.solawi.bid.module.banking.data.toDomainType


const val UPDATE_SEPA_MANDATE_IN_SEPA_COLLECTION = "UPDATE_SEPA_MANDATE_IN_SEPA_COLLECTION"
const val UPDATE_SEPA_MANDATE = "UPDATE_SEPA_MANDATE"
fun updateSepaMandateInSepaCollection(
    data: UpdateSepaMandate,
    targetCollectionId: SepaCollectionId,
    nameSuffix: String = ""
): Action<BankingApplication, UpdateSepaMandate, ApiSepaMandate> = Action(
    name = UPDATE_SEPA_MANDATE_IN_SEPA_COLLECTION.suffixed(nameSuffix),
    reader = { _ -> data },
    endPoint = UpdateSepaMandate::class,
    writer = (sepaModule * sepaCollections * FirstBy{
        it.sepaCollectionId == targetCollectionId
    } * sepaMandatesOfCollection).update{
            p, q -> p.sepaMandateId == q.sepaMandateId
    } contraMap { mandate: ApiSepaMandate -> mandate.toDomainType()}
)

fun updateSepaMandate(
    data: UpdateSepaMandate,
    nameSuffix: String = ""
): Action<BankingApplication, UpdateSepaMandate, ApiSepaMandate> = Action(
    name = UPDATE_SEPA_MANDATE.suffixed(nameSuffix),
    reader = { _ -> data },
    endPoint = UpdateSepaMandate::class,
    writer = (sepaModule * sepaMandates).update{
            p, q -> p.sepaMandateId == q.sepaMandateId
    } contraMap { mandate: ApiSepaMandate -> mandate.toDomainType()}
)
