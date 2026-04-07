package org.solyton.solawi.bid.module.banking.action

import org.evoleq.compose.Markup
import org.evoleq.math.contraMap
import org.evoleq.optics.lens.times
import org.evoleq.optics.storage.Action
import org.evoleq.optics.storage.suffixed
import org.evoleq.optics.transform.add
import org.solyton.solawi.bid.module.banking.data.api.ApiSepaCollection
import org.solyton.solawi.bid.module.banking.data.api.CreateSepaCollection
import org.solyton.solawi.bid.module.banking.data.application.BankingApplication
import org.solyton.solawi.bid.module.banking.data.application.sepaModule
import org.solyton.solawi.bid.module.banking.data.sepa.sepaCollections
import org.solyton.solawi.bid.module.banking.data.toDomainType

const val CREATE_SEPA_COLLECTION = "CREATE_SEPA_COLLECTION"

@Markup
fun createSepaCollection(
    data: CreateSepaCollection,
    nameSuffix: String = ""
): Action<BankingApplication, CreateSepaCollection, ApiSepaCollection> = Action(
    name = CREATE_SEPA_COLLECTION.suffixed(nameSuffix),
    reader = { _ -> data },
    endPoint = CreateSepaCollection::class,
    writer = (sepaModule * sepaCollections).add() contraMap { collection: ApiSepaCollection -> collection.toDomainType()}
)
