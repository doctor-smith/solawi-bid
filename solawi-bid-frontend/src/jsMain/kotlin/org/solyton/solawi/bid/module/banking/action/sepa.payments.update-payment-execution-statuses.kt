package org.solyton.solawi.bid.module.banking.action

import org.evoleq.math.contraMap
import org.evoleq.optics.lens.FirstBy
import org.evoleq.optics.lens.times
import org.evoleq.optics.storage.Action
import org.evoleq.optics.storage.suffixed
import org.evoleq.optics.transform.updateAll
import org.solyton.solawi.bid.module.banking.data.SepaCollectionId
import org.solyton.solawi.bid.module.banking.data.api.ApiSepaPayments
import org.solyton.solawi.bid.module.banking.data.api.UpdateSepaPaymentExecutionStatuses
import org.solyton.solawi.bid.module.banking.data.application.BankingApplication
import org.solyton.solawi.bid.module.banking.data.application.sepaModule
import org.solyton.solawi.bid.module.banking.data.sepa.collection.sepaPayments
import org.solyton.solawi.bid.module.banking.data.sepa.sepaCollections
import org.solyton.solawi.bid.module.banking.data.toDomainType

const val UPDATE_SEPA_PAYMENT_EXECUTION_STATUSES = "UPDATE_SEPA_PAYMENT_EXECUTION_STATUSES"

fun updateSepaPaymentExecutionStatuses(
    data: UpdateSepaPaymentExecutionStatuses,
    targetCollectionId: SepaCollectionId,
    nameSuffix: String = ""
): Action<BankingApplication, UpdateSepaPaymentExecutionStatuses, ApiSepaPayments> = Action(
    name = UPDATE_SEPA_PAYMENT_EXECUTION_STATUSES.suffixed(nameSuffix),
    reader = { _ -> data },
    endPoint = UpdateSepaPaymentExecutionStatuses::class,
    writer = (sepaModule * sepaCollections * FirstBy{
        it.sepaCollectionId == targetCollectionId
    } * sepaPayments).updateAll {
        p, q -> p.sepaPaymentId == q.sepaPaymentId
    } contraMap { payments: ApiSepaPayments -> payments.all.map{ it.toDomainType() }}
)
