package org.solyton.solawi.bid.module.banking.action

import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.Contextual
import org.evoleq.ktorx.DbAction
import org.evoleq.ktorx.KlAction
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.math.MathDsl
import org.evoleq.math.x
import org.evoleq.uuid.toUuid
import org.solyton.solawi.bid.module.banking.data.api.AttachSepaMandateReferenceData
import org.solyton.solawi.bid.module.banking.data.api.SepaMandate
import org.solyton.solawi.bid.module.banking.data.toApiType
import org.solyton.solawi.bid.module.banking.repository.attachSepaMandate

@MathDsl
@Suppress("FunctionName")
fun AttachSepaMandateReferenceData(): KlAction<Result<Contextual<AttachSepaMandateReferenceData>>, Result<SepaMandate>> = KlAction { result ->
    DbAction { database ->
        result bindSuspend { contextual ->
            resultTransaction(database) {
                val data = contextual.data
                val userId = contextual.userId

                attachSepaMandate(
                    userId,
                    data.sepaMandateId.value.toUuid(),
                    data.referenceId.value.toUuid(),
                    data.amount,
                    data.collectionId?.value?.toUuid()
                ).toApiType()
            }
        } x database
    }
}
