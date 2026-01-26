package org.solyton.solawi.bid.module.bid.action.api.distributionpoints

import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.Contextual
import org.evoleq.ktorx.DbAction
import org.evoleq.ktorx.KlAction
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.math.MathDsl
import org.evoleq.math.x
import org.solyton.solawi.bid.module.bid.data.api.CreateDistributionPoint
import org.solyton.solawi.bid.module.bid.data.api.CreateOrUseAddress
import org.solyton.solawi.bid.module.bid.data.api.DistributionPoint
import org.solyton.solawi.bid.module.bid.data.toApiType
import org.solyton.solawi.bid.module.bid.repository.createDistributionPoint
import org.solyton.solawi.bid.module.user.repository.createAddress
import java.util.*

@MathDsl
@Suppress("FunctionName")
fun CreateDistributionPoint() = KlAction<Result<Contextual<CreateDistributionPoint>>, Result<DistributionPoint>> {
    auction: Result<Contextual<CreateDistributionPoint>> -> DbAction {
        database -> auction bindSuspend  { contextual -> resultTransaction(database) {
            val userId = contextual.userId
            val data = contextual.data
            // Create address if necessary
            val addressId = when(val address = data.address){
                is CreateOrUseAddress.Create -> with(address){
                    createAddress(
                        this.recipientName,
                        organizationName = organizationName,
                        addressLine1 =addressLine1,
                        addressLine2 = addressLine2,
                        city = city,
                        stateOrProvince = stateOrProvince,
                        postalCode = postalCode,
                        countryCode = countryCode,
                        creator = userId
                    ).id.value
                }
                is CreateOrUseAddress.Use -> UUID.fromString(address.addressId)
                null -> null
            }

            createDistributionPoint(
                name = data.name,
                addressId = addressId,
                organizationId =UUID.fromString(data.organizationId),
                creator = userId
            ).toApiType(this)
        } }  x database
    }
}
