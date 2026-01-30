package org.solyton.solawi.bid.module.distribution.action.api

import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.Contextual
import org.evoleq.ktorx.DbAction
import org.evoleq.ktorx.KlAction
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.math.MathDsl
import org.evoleq.math.x
import org.solyton.solawi.bid.module.distribution.data.api.DistributionPoint
import org.solyton.solawi.bid.module.distribution.data.api.UpdateDistributionPoint
import org.solyton.solawi.bid.module.distribution.data.toApiType
import org.solyton.solawi.bid.module.distribution.repository.updateDistributionPoint
import java.util.*

@MathDsl
@Suppress("FunctionName")
fun UpdateDistributionPoint() = KlAction<Result<Contextual<UpdateDistributionPoint>>, Result<DistributionPoint>> {
    auction: Result<Contextual<UpdateDistributionPoint>> -> DbAction {
        database -> auction bindSuspend  { contextual -> resultTransaction(database) {
            val userId = contextual.userId
            val data = contextual.data

            val address = data.address
            val addressId = when{
                address != null -> UUID.fromString(address.id)
                else -> null
            }
            updateDistributionPoint(
                distributionPointId = UUID.fromString(data.id),
                name = data.name,
                addressId = addressId,
                organizationId = UUID.fromString(data.organizationId),
                modifier = userId
            ).toApiType(this)
        } }  x database
    }
}
