package org.solyton.solawi.bid.module.user.action.organization

import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.Contextual
import org.evoleq.ktorx.DbAction
import org.evoleq.ktorx.KlAction
import org.evoleq.ktorx.map
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.math.MathDsl
import org.evoleq.math.x
import org.solyton.solawi.bid.module.permission.PermissionException
import org.solyton.solawi.bid.module.permission.action.db.isGranted
import org.solyton.solawi.bid.module.permission.repository.getUserRightContexts
import org.solyton.solawi.bid.module.user.data.api.organization.*
import org.solyton.solawi.bid.module.user.data.toApiType
import org.solyton.solawi.bid.module.user.exception.OrganizationException
import org.solyton.solawi.bid.module.user.permission.OrganizationRight
import org.solyton.solawi.bid.module.user.schema.OrganizationEntity
import org.solyton.solawi.bid.module.user.schema.OrganizationsTable
import org.solyton.solawi.bid.module.user.schema.repository.createChild
import org.solyton.solawi.bid.module.user.schema.repository.createRootOrganization
import org.solyton.solawi.bid.module.user.schema.repository.hasChildren
import org.solyton.solawi.bid.module.user.schema.repository.remove
import java.util.*

@MathDsl
@Suppress("FunctionName")
fun CreateOrganization(): KlAction<Result<Contextual<CreateOrganization>>, Result<Organization>> = KlAction{ result ->
    DbAction { database -> result bindSuspend {contextual -> resultTransaction(database) {
        val userId = contextual.userId
        val data = contextual.data

        val organization = createRootOrganization(data.name, userId)
        organization.toApiType(this)
    } } x database
} }

@MathDsl
@Suppress("FunctionName")
fun CreateChildOrganization(): KlAction<Result<Contextual<CreateChildOrganization>>, Result<Organization>> = KlAction{ result ->
    DbAction { database -> result bindSuspend {contextual -> resultTransaction(database) {
        val userId = contextual.userId
        val data = contextual.data

        val organization = OrganizationEntity.find {
            OrganizationsTable.id eq UUID.fromString(data.organizationId)
        }.firstOrNull()
            ?: throw OrganizationException.NoSuchChildOrganization(id)

        if(!isGranted(
                userId,
                organization.context.id.value,
                OrganizationRight.Organization.create.value
        )) throw PermissionException.AccessDenied

        val childOrganization = organization.createChild(data.name, userId)
        childOrganization.toApiType(this)
    } } x database
} }

@MathDsl
@Suppress("FunctionName")
fun ReadOrganizations(): KlAction<Result<Contextual<ReadOrganizations>>, Result<Organizations>> = KlAction{ result ->
    DbAction { database -> result bindSuspend {contextual -> resultTransaction(database) {
        val userId = contextual.userId
        val userRightContextIds = getUserRightContexts(userId, listOf("READ_ORGANIZATION"))
        val organizations = OrganizationEntity.find { OrganizationsTable.contextId inList userRightContextIds }
            .map { it.root?: it }.distinctBy{it.id.value}
        Organizations(organizations.map { organization -> organization.toApiType(this) })
    } } x database
} }

@MathDsl
@Suppress("FunctionName", "CognitiveComplexMethod")
fun UpdateOrganization(): KlAction<Result<Contextual<UpdateOrganization>>, Result<Organization>> = KlAction{ result ->
    DbAction { database -> result bindSuspend {contextual -> resultTransaction(database) {
        val userId = contextual.userId
        val data = contextual.data

        val organization = OrganizationEntity.find {
            OrganizationsTable.id eq UUID.fromString(data.id)
        }.firstOrNull()
            ?: throw OrganizationException.NoSuchOrganization(id)

        if(!isGranted(
                userId,
                organization.context.id.value,
                OrganizationRight.Organization.update.value
        )) throw PermissionException.AccessDenied

        var changed: Boolean = false
        if(organization.name != data.name) {
            organization.name = data.name
            changed = true
        }

        // Set auditable data
        if(changed) {
            organization.modifiedAt = org.evoleq.exposedx.joda.now()
            organization.modifiedBy = userId
        }

        organization.toApiType(this)
    } } x database
 } }

// todo:test
@MathDsl
@Suppress("FunctionName")
fun DeleteOrganization(): KlAction<Result<Contextual<DeleteOrganization>>, Result<Contextual<Unit>>> = KlAction{ result ->
    DbAction { database -> result bindSuspend {contextual -> resultTransaction(database) {
        val organizationId = UUID.fromString(contextual.data.id)

        val organization = OrganizationEntity.find { OrganizationsTable.id eq organizationId }.firstOrNull()
            ?: throw OrganizationException.NoSuchOrganization(organizationId.toString())

        if(organization.hasChildren()) throw OrganizationException.CannotDeleteOrganization(
            organizationId.toString(),
            "There are sub organizations"
        )

        organization.remove()
        contextual map {}
    } } x database
} }
