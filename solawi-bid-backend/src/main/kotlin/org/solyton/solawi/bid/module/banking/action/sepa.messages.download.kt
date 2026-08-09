package org.solyton.solawi.bid.module.banking.action

import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.Contextual
import org.evoleq.ktorx.DbAction
import org.evoleq.ktorx.KlAction
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.math.MathDsl
import org.evoleq.math.x
import org.solyton.solawi.bid.module.banking.data.SepaMessageId
import org.solyton.solawi.bid.module.banking.data.api.SepaMessageString
import org.solyton.solawi.bid.module.banking.data.api.SepaMessageVersion
import org.solyton.solawi.bid.module.banking.service.buildPain008Xml

// DownloadSepaMessage


@MathDsl
@Suppress("FunctionName")
fun DownloadSepaMessage(): KlAction<Result<Contextual<String>>, Result<SepaMessageString>> = KlAction { result ->
    DbAction { database ->
        result bindSuspend { contextual ->
            resultTransaction(database) {
                val data = contextual.data
                val message = buildPain008Xml(SepaMessageId( data))
                SepaMessageString(
                    version = SepaMessageVersion.PAIN008,
                    message = message
                )
            }
        } x database
    }
}
