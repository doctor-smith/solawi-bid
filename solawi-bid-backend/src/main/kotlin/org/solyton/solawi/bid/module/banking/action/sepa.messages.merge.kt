package org.solyton.solawi.bid.module.banking.action

import org.evoleq.exposedx.joda.toJoda
import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.kotlinx.date.toDateTime
import org.evoleq.ktorx.Contextual
import org.evoleq.ktorx.DbAction
import org.evoleq.ktorx.KlAction
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.math.MathDsl
import org.evoleq.math.x
import org.evoleq.uuid.toUuid
import org.solyton.solawi.bid.module.banking.data.api.MergeSepaMessages
import org.solyton.solawi.bid.module.banking.data.api.SepaMessages
import org.solyton.solawi.bid.module.banking.data.toApiType
import org.solyton.solawi.bid.module.banking.repository.mergeSepaMessages


@MathDsl
@Suppress("FunctionName")
fun MergeSepaMessages(): KlAction<Result<Contextual<MergeSepaMessages>>, Result<SepaMessages>> = KlAction { result ->
    DbAction { database ->
        result bindSuspend { contextual ->

            resultTransaction(database) {
                val userId = contextual.userId
                val data = contextual.data

                mergeSepaMessages(
                    userId,
                    data.sepaMessageIds.map { it.value.toUuid() },
                    data.executionDate.toDateTime().toJoda(),
                    data.remittanceInformation
                ).toApiType()
            }
        } x database
    }
}
