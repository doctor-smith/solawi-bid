package org.solyton.solawi.bid.module.banking.action

import org.evoleq.math.contraMap
import org.evoleq.optics.lens.FirstBy
import org.evoleq.optics.lens.times
import org.evoleq.optics.storage.Action
import org.evoleq.optics.storage.suffixed
import org.evoleq.optics.transform.add
import org.solyton.solawi.bid.module.banking.data.SepaCollectionId
import org.solyton.solawi.bid.module.banking.data.api.ApiSepaPayment
import org.solyton.solawi.bid.module.banking.data.api.CreateSepaPayment
import org.solyton.solawi.bid.module.banking.data.application.BankingApplication
import org.solyton.solawi.bid.module.banking.data.application.sepaModule
import org.solyton.solawi.bid.module.banking.data.sepa.collection.sepaPayments
import org.solyton.solawi.bid.module.banking.data.sepa.sepaCollections
import org.solyton.solawi.bid.module.banking.data.toDomainType


const val CREATE_SEPA_PAYMENT = "CREATE_SEPA_PAYMENT"

fun createSepaPayment(
    data: CreateSepaPayment,
    targetCollectionId: SepaCollectionId,
    nameSuffix: String = ""
): Action<BankingApplication, CreateSepaPayment, ApiSepaPayment> = Action(
    name = CREATE_SEPA_PAYMENT.suffixed(nameSuffix),
    reader = { _ -> data },
    endPoint = CreateSepaPayment::class,
    writer = (sepaModule * sepaCollections * FirstBy{
        it.sepaCollectionId == targetCollectionId
    } * sepaPayments).add() contraMap { payment: ApiSepaPayment -> payment.toDomainType()}
)
