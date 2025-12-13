package org.solyton.solawi.bid.module.application.action

import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.Contextual
import org.evoleq.ktorx.DbAction
import org.evoleq.ktorx.KlAction
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.math.MathDsl
import org.evoleq.math.x
import org.solyton.solawi.bid.module.application.data.ApplicationOrganizationRelation
import org.solyton.solawi.bid.module.application.data.ApplicationOrganizationRelations
import org.solyton.solawi.bid.module.application.data.ConnectApplicationToOrganization
import org.solyton.solawi.bid.module.application.data.ReadApplicationOrganizationContextRelations
import org.solyton.solawi.bid.module.application.repository.connectApplicationToOrganization
import org.solyton.solawi.bid.module.application.schema.OrganizationApplicationContextEntity
import org.solyton.solawi.bid.module.application.schema.OrganizationApplicationContextsTable
import org.solyton.solawi.bid.module.user.schema.UserOrganization
import java.util.*

@MathDsl
@Suppress("FunctionName")
fun ConnectApplicationToOrganization(): KlAction<Result<Contextual<ConnectApplicationToOrganization>>, Result<ApplicationOrganizationRelations>> = KlAction {
    result -> DbAction { database ->
        result bindSuspend { contextual ->
            resultTransaction(database) {
                connectApplicationToOrganization(
                    UUID.fromString(contextual.data.applicationId),
                    UUID.fromString(contextual.data.organizationId),
                    contextual.data.moduleIds.map { UUID.fromString(it) },
                    contextual.userId
                )
            }
        } x database
    }
}

@MathDsl
@Suppress("FunctionName")
fun ReadApplicationOrganizationContextRelations(): KlAction<Result<Contextual<ReadApplicationOrganizationContextRelations>>, Result<ApplicationOrganizationRelations>> = KlAction {
    result -> DbAction { database ->
        result bindSuspend { contextual ->
            resultTransaction(database) {
                val organizationIds = UserOrganization
                    .select(UserOrganization.organizationId)
                    .where { UserOrganization.userId eq contextual.userId }
                    .map { row -> row[UserOrganization.organizationId].value }

                val organizationRelations = OrganizationApplicationContextEntity.find {
                    OrganizationApplicationContextsTable.organizationId inList organizationIds
                }.toList().map { value ->
                    ApplicationOrganizationRelation(
                        applicationId = value.application.id.value.toString(),
                        organizationId = value.organizationId.toString(),
                        contextId = value.context.id.value.toString(),
                        moduleIds = value.application.modules.map { it.id.value.toString() }
                    )
                }

                ApplicationOrganizationRelations(organizationRelations)
            }
        } x database
    }
}
