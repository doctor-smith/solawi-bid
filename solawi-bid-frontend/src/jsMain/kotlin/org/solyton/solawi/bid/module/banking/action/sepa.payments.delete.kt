package org.solyton.solawi.bid.module.banking.action

import org.evoleq.math.contraMap
import org.evoleq.optics.lens.FirstBy
import org.evoleq.optics.lens.times
import org.evoleq.optics.storage.Action
import org.evoleq.optics.storage.suffixed
import org.evoleq.optics.transform.removeWhen
import org.solyton.solawi.bid.module.banking.data.SepaCollectionId
import org.solyton.solawi.bid.module.banking.data.SepaPaymentId
import org.solyton.solawi.bid.module.banking.data.api.DeleteSepaPayment
import org.solyton.solawi.bid.module.banking.data.api.DeleteSepaPayments
import org.solyton.solawi.bid.module.banking.data.api.SepaPaymentIds
import org.solyton.solawi.bid.module.banking.data.application.BankingApplication
import org.solyton.solawi.bid.module.banking.data.application.sepaModule
import org.solyton.solawi.bid.module.banking.data.sepa.collection.sepaPayments
import org.solyton.solawi.bid.module.banking.data.sepa.sepaCollections

const val DELETE_SEPA_PAYMENT = "DELETE_SEPA_PAYMENT"
const val DELETE_SEPA_PAYMENTS = "DELETE_SEPA_PAYMENTS"


fun deleteSepaPayment(
    data: DeleteSepaPayment,
    targetCollectionId: SepaCollectionId,
    nameSuffix: String = ""
): Action<BankingApplication, DeleteSepaPayment, SepaPaymentId> = Action(
    name = DELETE_SEPA_PAYMENT.suffixed(nameSuffix),
    reader = { _ -> data },
    endPoint = DeleteSepaPayment::class,
    writer = (sepaModule * sepaCollections * FirstBy{
        it.sepaCollectionId == targetCollectionId
    } * sepaPayments).removeWhen { it.sepaPaymentId == data.id } contraMap { true }
)


fun deleteSepaPayments(
    data: DeleteSepaPayments,
    targetCollectionId: SepaCollectionId,
    nameSuffix: String = ""
): Action<BankingApplication, DeleteSepaPayments, SepaPaymentIds> = Action(
    name = DELETE_SEPA_PAYMENTS.suffixed(nameSuffix),
    reader = { _ -> data },
    endPoint = DeleteSepaPayments::class,
    writer = (sepaModule * sepaCollections * FirstBy{
        it.sepaCollectionId == targetCollectionId
    } * sepaPayments).removeWhen { it.sepaPaymentId in data.paymentIds } contraMap { true }
)
