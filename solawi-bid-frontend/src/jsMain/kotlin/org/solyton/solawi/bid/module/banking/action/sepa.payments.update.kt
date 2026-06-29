package org.solyton.solawi.bid.module.banking.action

import org.evoleq.compose.Markup
import org.evoleq.math.contraMap
import org.evoleq.optics.lens.FirstBy
import org.evoleq.optics.lens.times
import org.evoleq.optics.storage.Action
import org.evoleq.optics.storage.suffixed
import org.evoleq.optics.transform.update
import org.solyton.solawi.bid.module.banking.data.SepaCollectionId
import org.solyton.solawi.bid.module.banking.data.api.ApiSepaPayment
import org.solyton.solawi.bid.module.banking.data.api.UpdateSepaPayment
import org.solyton.solawi.bid.module.banking.data.application.BankingApplication
import org.solyton.solawi.bid.module.banking.data.application.sepaModule
import org.solyton.solawi.bid.module.banking.data.sepa.collection.sepaPayments
import org.solyton.solawi.bid.module.banking.data.sepa.sepaCollections
import org.solyton.solawi.bid.module.banking.data.toDomainType

const val UPDATE_SEPA_PAYMENT = "UPDATE_SEPA_PAYMENT"

@Markup
fun updateSepaPayment(
    data: UpdateSepaPayment,
    targetCollectionId: SepaCollectionId,
    nameSuffix: String = ""
) = Action<BankingApplication, UpdateSepaPayment, ApiSepaPayment>(
    name = UPDATE_SEPA_PAYMENT.suffixed(nameSuffix),
    reader = { _ -> data },
    endPoint = UpdateSepaPayment::class,
    writer = (sepaModule * sepaCollections * FirstBy{
        it.sepaCollectionId == targetCollectionId
    } * sepaPayments).update{
        p,q -> p.sepaPaymentId == q.sepaPaymentId
    } contraMap { payment: ApiSepaPayment -> payment.toDomainType()}
)
