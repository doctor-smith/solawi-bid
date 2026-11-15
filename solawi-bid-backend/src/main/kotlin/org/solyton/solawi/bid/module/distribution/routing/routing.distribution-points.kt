package org.solyton.solawi.bid.module.distribution.routing

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
import org.solyton.solawi.bid.module.distribution.action.api.CreateDistributionPoint
import org.solyton.solawi.bid.module.distribution.action.api.ReadDistributionPoints
import org.solyton.solawi.bid.module.distribution.action.api.ReadPersonalDistributionPoints
import org.solyton.solawi.bid.module.distribution.action.api.UpdateDistributionPoint
import org.solyton.solawi.bid.module.distribution.data.api.CreateDistributionPoint
import org.solyton.solawi.bid.module.distribution.data.api.DistributionPoint
import org.solyton.solawi.bid.module.distribution.data.api.DistributionPoints
import org.solyton.solawi.bid.module.distribution.data.api.UpdateDistributionPoint
import org.solyton.solawi.bid.module.application.repository.contextIdOf
import org.solyton.solawi.bid.module.distribution.schema.DistributionPointEntity
import org.solyton.solawi.bid.module.permission.action.db.IsGrantedInAllDerivedContexts
import org.solyton.solawi.bid.module.permission.action.db.IsGrantedInDerivedContext
import org.solyton.solawi.bid.module.user.schema.UserOrganization
import java.util.UUID

private const val DISTRIBUTION_APPLICATION_NAME = "DISTRIBUTION"

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
                IsGrantedInDerivedContext("READ_DISTRIBUTION_POINTS") { contextual ->
                    contextIdOf(UUID.fromString(contextual.data), DISTRIBUTION_APPLICATION_NAME)
                } *
                ReadDistributionPoints() *
                Respond<DistributionPoints> { transform() } runOn Base(call, environment)
            }
            get("personal") {
                ReceiveContextual {} *
                IsGrantedInAllDerivedContexts("READ_DISTRIBUTION_POINTS") { contextual ->
                    UserOrganization
                        .select(UserOrganization.organizationId)
                        .where { UserOrganization.userId eq contextual.userId }
                        .map { contextIdOf(it[UserOrganization.organizationId].value, DISTRIBUTION_APPLICATION_NAME) }
                        .toSet()
                } *
                ReadPersonalDistributionPoints() *
                Respond<DistributionPoints> { transform() } runOn Base(call, environment)
            }
            post("create") {
                ReceiveContextual<CreateDistributionPoint>() *
                IsGrantedInDerivedContext("CREATE_DISTRIBUTION_POINTS") { contextual ->
                    contextIdOf(UUID.fromString(contextual.data.organizationId), DISTRIBUTION_APPLICATION_NAME)
                } *
                CreateDistributionPoint() *
                Respond<DistributionPoint> { transform() } runOn Base(call, environment)
            }
            patch("update") {
                ReceiveContextual<UpdateDistributionPoint>() *
                IsGrantedInAllDerivedContexts("UPDATE_DISTRIBUTION_POINTS") { contextual ->
                    val sourceOrganizationId = DistributionPointEntity
                        .findById(UUID.fromString(contextual.data.id))
                        ?.organization
                        ?.id
                        ?.value
                        ?: throw org.solyton.solawi.bid.module.distribution.exception.DistributionPointException
                            .NoSuchDistributionPoint(contextual.data.id)
                    setOf(sourceOrganizationId, UUID.fromString(contextual.data.organizationId))
                        .map { contextIdOf(it, DISTRIBUTION_APPLICATION_NAME) }
                        .toSet()
                } *
                UpdateDistributionPoint()   *
                Respond<DistributionPoint> { transform() } runOn Base(call, environment)
            }
        }
    }
