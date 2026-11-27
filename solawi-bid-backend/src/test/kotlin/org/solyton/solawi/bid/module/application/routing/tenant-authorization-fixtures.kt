package org.solyton.solawi.bid.module.application.routing

import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.evoleq.ktorx.headers.Header
import org.evoleq.uuid.UUID_ZERO
import org.jetbrains.exposed.sql.Transaction
import org.solyton.solawi.bid.module.application.repository.createApplication
import org.solyton.solawi.bid.module.application.repository.createUserRoleContext
import org.solyton.solawi.bid.module.application.repository.registerOrganizationForApplication
import org.solyton.solawi.bid.module.permission.repository.*
import org.solyton.solawi.bid.module.permission.schema.ContextEntity
import org.solyton.solawi.bid.module.user.schema.OrganizationEntity
import org.solyton.solawi.bid.module.user.schema.UserEntity
import org.solyton.solawi.bid.module.user.schema.UsersTable
import java.util.UUID

internal fun Transaction.createProviders(
    applicationName: String,
    sourceRight: String,
    destinationRight: String
): Providers {
    val application = createApplication(
        applicationName, "Tenant authorization fixture", UUID_ZERO,
        defaultContextId = createRootContext(applicationName).id.value
    )
    val sourceUserId = findUserId(SOURCE_USER)
    val source = createProvider("Source provider", application.id.value, sourceRight, sourceUserId)
    val destination = createProvider(
        "Destination provider", application.id.value, destinationRight, findUserId(DESTINATION_USER)
    )
    return Providers(source, destination, sourceUserId)
}

private fun Transaction.createProvider(
    name: String,
    applicationId: UUID,
    rightName: String,
    userId: UUID
): Provider {
    val organizationContext = createRootContext(name)
    val organization = OrganizationEntity.new {
        this.name = name
        context = organizationContext
        createdBy = userId
    }
    val applicationContext = registerOrganizationForApplication(organization.id.value, applicationId).context
    val role = createRole("$name editor", "Can edit the fixture resource", UUID_ZERO)
    val right = createRightIfNotExists(rightName, "Can edit the fixture resource", UUID_ZERO)
    (role of applicationContext).grant(right)
    return Provider(organization.id.value, applicationContext.id.value, role.name).also { it.grantTo(userId) }
}

internal fun Transaction.grantProviderRight(provider: Provider, rightName: String) {
    val context = requireNotNull(ContextEntity.findById(provider.contextId))
    val right = createRightIfNotExists(rightName, "Test authorization right", UUID_ZERO)
    (readRoleByName(provider.role) of context).grant(right)
}

internal fun findUserId(username: String) =
    UserEntity.find { UsersTable.username eq username }.single().id.value

internal suspend inline fun <reified T> HttpClient.patchInContext(
    path: String,
    token: String,
    contextId: UUID,
    update: T
): HttpStatusCode = patch(path) {
    bearerAuth(token)
    header(Header.CONTEXT, contextId.toString())
    contentType(ContentType.Application.Json)
    setBody(Json.encodeToString(update))
}.status

internal data class Providers(val source: Provider, val destination: Provider, val sourceUserId: UUID)

internal data class Provider(val organizationId: UUID, val contextId: UUID, val role: String) {
    fun grantTo(userId: UUID) = createUserRoleContext(userId, role, contextId)
}

internal const val SOURCE_USER = "dummy_0@solyton.org"
internal const val DESTINATION_USER = "dummy_1@solyton.org"
