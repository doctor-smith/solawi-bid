package org.solyton.solawi.bid.module.bid.action.api.distributionpoints

import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.Contextual
import org.evoleq.ktorx.DbAction
import org.evoleq.ktorx.KlAction
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.math.MathDsl
import org.evoleq.math.x
import org.solyton.solawi.bid.module.bid.data.api.DistributionPoints
import org.solyton.solawi.bid.module.bid.data.toApiType
import org.solyton.solawi.bid.module.bid.repository.readDistributionPointsByOrganization
import java.util.*

@MathDsl
@Suppress("FunctionName")
fun ReadDistributionPoints() = KlAction<Result<Contextual<String>>, Result<DistributionPoints>> {
    auction: Result<Contextual<String>> -> DbAction {
        database -> auction bindSuspend  { contextual -> resultTransaction(database) {
            val data = contextual.data

            readDistributionPointsByOrganization(UUID.fromString(data)).toApiType(this)
        } }  x database
    }
}
