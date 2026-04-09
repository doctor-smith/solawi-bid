package org.solyton.solawi.bid.module.banking.action

import org.evoleq.compose.Markup
import org.evoleq.math.contraMap
import org.evoleq.optics.lens.times
import org.evoleq.optics.storage.Action
import org.evoleq.optics.storage.suffixed
import org.evoleq.optics.transform.update
import org.solyton.solawi.bid.module.banking.data.api.ApiSepaCollection
import org.solyton.solawi.bid.module.banking.data.api.UpdateSepaCollection
import org.solyton.solawi.bid.module.banking.data.application.BankingApplication
import org.solyton.solawi.bid.module.banking.data.application.sepaModule
import org.solyton.solawi.bid.module.banking.data.sepa.sepaCollections
import org.solyton.solawi.bid.module.banking.data.toDomainType

const val UPDATE_SEPA_COLLECTION = "UPDATE_SEPA_COLLECTION"

@Markup
fun updateSepaCollection(
    data: UpdateSepaCollection,
    nameSuffix: String = ""
): Action<BankingApplication, UpdateSepaCollection, ApiSepaCollection> = Action(
    name = UPDATE_SEPA_COLLECTION.suffixed(nameSuffix),
    reader = { _ -> data },
    endPoint = UPDATE_SEPA_COLLECTION::class,
    writer = (sepaModule * sepaCollections).update{
        p, q -> p.sepaCollectionId == q.sepaCollectionId
    } contraMap { collection: ApiSepaCollection -> collection.toDomainType()}
)
