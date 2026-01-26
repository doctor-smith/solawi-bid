package org.solyton.solawi.bid.module.bid.routing

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.util.*
import org.evoleq.exposedx.data.DbEnv
import org.evoleq.ktorx.Base
import org.evoleq.ktorx.ReceiveContextual
import org.evoleq.ktorx.Respond
import org.evoleq.ktorx.data.KTorEnv
import org.evoleq.math.state.runOn
import org.evoleq.math.state.times
import org.solyton.solawi.bid.module.bid.action.api.distributionpoints.CreateDistributionPoint
import org.solyton.solawi.bid.module.bid.action.api.distributionpoints.ReadDistributionPoints
import org.solyton.solawi.bid.module.bid.action.api.distributionpoints.UpdateDistributionPoint
import org.solyton.solawi.bid.module.bid.data.api.CreateDistributionPoint
import org.solyton.solawi.bid.module.bid.data.api.DistributionPoint
import org.solyton.solawi.bid.module.bid.data.api.DistributionPoints
import org.solyton.solawi.bid.module.bid.data.api.UpdateDistributionPoint
import org.solyton.solawi.bid.module.permission.action.db.IsGranted

@KtorDsl
fun <DistributionPointsEnv> Routing.distributionPoints(
    environment: DistributionPointsEnv,
    authenticate: Routing.(Route.() -> Route)-> Route
) where DistributionPointsEnv : KTorEnv, DistributionPointsEnv: DbEnv =
    authenticate {
        val transform = environment.transformException
        route("distribution-points") {
            get("all") {
                @Suppress("UnsafeCallOnNullableType")
                val provider = call.parameters["provider"]!!
                ReceiveContextual<String>(provider) *
                IsGranted("READ_DISTRIBUTION_POINTS") *
                ReadDistributionPoints() *
                Respond<DistributionPoints> { transform() } runOn Base(call, environment)
            }
            post("create") {
                ReceiveContextual<CreateDistributionPoint>() *
                IsGranted("CREATE_DISTRIBUTION_POINT") *
                CreateDistributionPoint() *
                Respond<DistributionPoint> { transform() } runOn Base(call, environment)
            }
            patch("update") {
                ReceiveContextual<UpdateDistributionPoint>() *
                IsGranted("UPDATE_DISTRIBUTION_POINT") *
                UpdateDistributionPoint()   *
                Respond<DistributionPoint> { transform() } runOn Base(call, environment)
            }
        }
    }
