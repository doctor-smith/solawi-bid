package org.solyton.solawi.bid.module.banking.action

import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.Contextual
import org.evoleq.ktorx.DbAction
import org.evoleq.ktorx.KlAction
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.math.MathDsl
import org.evoleq.math.x
import org.solyton.solawi.bid.module.banking.data.api.SepaMessage
import org.solyton.solawi.bid.module.banking.data.api.UpdateSepaMessageStatus
import org.solyton.solawi.bid.module.banking.data.toApiType
import org.solyton.solawi.bid.module.banking.data.toDomainType
import org.solyton.solawi.bid.module.banking.repository.updateSepaMessageStatus
import java.util.*

// UpdateSepaMessageStatus

@MathDsl
@Suppress("FunctionName")
fun UpdateSepaMessageStatus(): KlAction<Result<Contextual<UpdateSepaMessageStatus>>, Result<SepaMessage>> = KlAction { result ->
    DbAction { database ->
        result bindSuspend { contextual ->
            resultTransaction(database) {
                val userId = contextual.userId
                val data = contextual.data

                updateSepaMessageStatus(
                    modifierId = userId,
                    sepaMessageId = UUID.fromString(data.sepaMessageId.value),
                    status = data.newStatus.toDomainType(),
                    updatePayments = true
                ).toApiType()

            }
        } x database
    }
}
