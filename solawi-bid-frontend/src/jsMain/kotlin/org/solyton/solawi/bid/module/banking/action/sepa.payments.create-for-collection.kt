package org.solyton.solawi.bid.module.banking.action

import org.evoleq.math.contraMap
import org.evoleq.optics.lens.FirstBy
import org.evoleq.optics.lens.times
import org.evoleq.optics.storage.Action
import org.evoleq.optics.storage.suffixed
import org.evoleq.optics.transform.addList
import org.solyton.solawi.bid.module.banking.data.SepaCollectionId
import org.solyton.solawi.bid.module.banking.data.api.ApiSepaPayments
import org.solyton.solawi.bid.module.banking.data.api.CreateSepaPaymentsForCollection
import org.solyton.solawi.bid.module.banking.data.application.BankingApplication
import org.solyton.solawi.bid.module.banking.data.application.sepaModule
import org.solyton.solawi.bid.module.banking.data.sepa.collection.sepaPayments
import org.solyton.solawi.bid.module.banking.data.sepa.sepaCollections
import org.solyton.solawi.bid.module.banking.data.toDomainType


const val CREATE_SEPA_PAYMENTS_FOR_COLLECTION = "CREATE_SEPA_PAYMENTS_FOR_COLLECTION"

fun createSepaPaymentsForCollection(
    data: CreateSepaPaymentsForCollection,
    targetCollectionId: SepaCollectionId,
    nameSuffix: String = ""
): Action<BankingApplication, CreateSepaPaymentsForCollection, ApiSepaPayments> = Action(
    name = CREATE_SEPA_PAYMENTS_FOR_COLLECTION.suffixed(nameSuffix),
    reader = { _ -> data },
    endPoint = CreateSepaPaymentsForCollection::class,
    writer = (sepaModule * sepaCollections * FirstBy{
        it.sepaCollectionId == targetCollectionId
    } * sepaPayments).addList() contraMap { payments: ApiSepaPayments -> payments.all.map{it.toDomainType()}}
)
