package org.solyton.solawi.bid.module.banking.action.sepa

import org.evoleq.compose.Markup
import org.evoleq.math.contraMap
import org.evoleq.optics.lens.FirstBy
import org.evoleq.optics.lens.times
import org.evoleq.optics.storage.Action
import org.evoleq.optics.storage.suffixed
import org.evoleq.optics.transform.update
import org.evoleq.optics.transform.updateAll
import org.solyton.solawi.bid.module.banking.data.SepaCollectionId
import org.solyton.solawi.bid.module.banking.data.api.ApiSepaPayment
import org.solyton.solawi.bid.module.banking.data.api.ApiSepaPayments
import org.solyton.solawi.bid.module.banking.data.api.UpdateSepaPayment
import org.solyton.solawi.bid.module.banking.data.api.UpdateSepaPayments
import org.solyton.solawi.bid.module.banking.data.application.BankingApplication
import org.solyton.solawi.bid.module.banking.data.application.sepaModule
import org.solyton.solawi.bid.module.banking.data.sepa.collection.sepaPayments
import org.solyton.solawi.bid.module.banking.data.sepa.sepaCollections
import org.solyton.solawi.bid.module.banking.data.toDomainType

const val UPDATE_SEPA_PAYMENT = "UPDATE_SEPA_PAYMENT"

const val UPDATE_SEPA_PAYMENTS = "UPDATE_SEPA_PAYMENTS"

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

@Markup
fun updateSepaPayments(
    data: UpdateSepaPayments,
    targetCollectionId: SepaCollectionId,
    nameSuffix: String = ""
) = Action<BankingApplication, UpdateSepaPayments, ApiSepaPayments>(
    name = UPDATE_SEPA_PAYMENTS.suffixed(nameSuffix),
    reader = { _ -> data },
    endPoint = UpdateSepaPayments::class,
    writer = (sepaModule * sepaCollections * FirstBy{
        it.sepaCollectionId == targetCollectionId
    } * sepaPayments).updateAll{
            p,q -> p.sepaPaymentId == q.sepaPaymentId
    } contraMap { payments: ApiSepaPayments -> payments.toDomainType()}
)
