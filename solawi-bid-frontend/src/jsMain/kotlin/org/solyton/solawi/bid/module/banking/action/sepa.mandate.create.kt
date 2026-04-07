package org.solyton.solawi.bid.module.banking.action

import org.evoleq.math.contraMap
import org.evoleq.optics.lens.FirstBy
import org.evoleq.optics.lens.times
import org.evoleq.optics.storage.Action
import org.evoleq.optics.storage.suffixed
import org.evoleq.optics.transform.add
import org.solyton.solawi.bid.module.banking.data.SepaCollectionId
import org.solyton.solawi.bid.module.banking.data.api.ApiSepaMandate
import org.solyton.solawi.bid.module.banking.data.api.CreateSepaCollection
import org.solyton.solawi.bid.module.banking.data.api.CreateSepaMandate
import org.solyton.solawi.bid.module.banking.data.application.BankingApplication
import org.solyton.solawi.bid.module.banking.data.application.sepaModule
import org.solyton.solawi.bid.module.banking.data.sepa.collection.sepaMandates
import org.solyton.solawi.bid.module.banking.data.sepa.sepaCollections
import org.solyton.solawi.bid.module.banking.data.toDomainType

const val CREATE_SEPA_MANDATE = "CREATE_SEPA_MANDATE"

fun createSepaMandate(
    data: CreateSepaMandate,
    targetCollectionId: SepaCollectionId,
    nameSuffix: String = ""
): Action<BankingApplication, CreateSepaMandate, ApiSepaMandate> = Action(
    name = CREATE_SEPA_MANDATE.suffixed(nameSuffix),
    reader = { _ -> data },
    endPoint = CreateSepaCollection::class,
    writer = (sepaModule * sepaCollections * FirstBy{
        it.sepaCollectionId == targetCollectionId
    } * sepaMandates).add() contraMap { mandate: ApiSepaMandate -> mandate.toDomainType()}
)
