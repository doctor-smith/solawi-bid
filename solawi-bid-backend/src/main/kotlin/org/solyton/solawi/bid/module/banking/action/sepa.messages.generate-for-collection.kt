package org.solyton.solawi.bid.module.banking.action

import org.evoleq.exposedx.joda.toJoda
import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.Contextual
import org.evoleq.ktorx.DbAction
import org.evoleq.ktorx.KlAction
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.math.MathDsl
import org.evoleq.math.x
import org.solyton.solawi.bid.module.banking.data.api.GenerateSepaMessageForCollection
import org.solyton.solawi.bid.module.banking.data.api.SepaMessageString
import org.solyton.solawi.bid.module.banking.data.api.SepaMessageVersion
import org.solyton.solawi.bid.module.banking.repository.generateSepaMessageForCollection
import java.util.*

@MathDsl
@Suppress("FunctionName")
fun GenerateSepaMessageForCollection(): KlAction<Result<Contextual<GenerateSepaMessageForCollection>>, Result<SepaMessageString>> = KlAction { result ->
    DbAction { database ->
        result bindSuspend { contextual ->
            resultTransaction(database) {
                val data = contextual.data
                val userId = contextual.userId
                val message = generateSepaMessageForCollection(
                    creator = userId,
                    collectionId = UUID.fromString(data.sepaCollectionId.value),
                    executionDate = data.executionDate.toJoda(),
                    sepaPaymentIds = data.sepaPaymentIds?.let{ids ->
                        ids.map { id -> UUID.fromString(id.value) }
                    },
                    remittanceInformation = data.remittanceInformation?.value,
                )
                SepaMessageString(
                    version = SepaMessageVersion.PAIN008,
                    message = message.pain008Xml
                )
            }
        } x database
    }
}
