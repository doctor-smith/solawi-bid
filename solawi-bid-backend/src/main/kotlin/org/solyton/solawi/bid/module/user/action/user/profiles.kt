package org.solyton.solawi.bid.module.user.action.user

import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.Contextual
import org.evoleq.ktorx.DbAction
import org.evoleq.ktorx.KlAction
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.math.MathDsl
import org.evoleq.math.x
import org.jetbrains.exposed.sql.Transaction
import org.solyton.solawi.bid.module.permission.PermissionException
import org.solyton.solawi.bid.module.permission.action.db.isGranted
import org.jetbrains.exposed.sql.selectAll
import org.solyton.solawi.bid.module.user.data.api.CreateUser
import org.solyton.solawi.bid.module.user.data.api.UserStatus as ApiUserStatus
import org.solyton.solawi.bid.module.user.data.api.userprofile.*
import org.solyton.solawi.bid.module.user.data.toApiType
import org.solyton.solawi.bid.module.user.repository.createUserProfile
import org.solyton.solawi.bid.module.user.repository.updateUserProfile
import org.solyton.solawi.bid.module.user.schema.AddressEntity
import org.solyton.solawi.bid.module.user.schema.OrganizationEntity
import org.solyton.solawi.bid.module.user.schema.UserEntity
import org.solyton.solawi.bid.module.user.schema.UserProfileEntity
import org.solyton.solawi.bid.module.user.schema.UserProfilesTable
import org.solyton.solawi.bid.module.user.schema.UsersTable
import org.solyton.solawi.bid.module.user.service.user.createUserEntity
import java.util.*


@MathDsl
@Suppress("FunctionName")
fun CreateUserProfile(): KlAction<Result<Contextual<CreateUserProfile>>, Result<UserProfile>> = KlAction{ result ->
    DbAction { database -> result bindSuspend {contextual -> resultTransaction(database) {
        val userId = contextual.userId
        val data = contextual.data
        createUserProfile(data, userId).toApiType(this)
    } } x database }
}


@MathDsl
@Suppress("FunctionName", "MapGetWithNotNullAssertionOperator", "UnsafeCallOnNullableType")
fun ImportProfiles(): KlAction<Result<Contextual<ImportUserProfiles>>, Result<UserProfiles>> = KlAction{ result ->
    DbAction { database -> result bindSuspend {contextual -> resultTransaction(database) {
        val userId = contextual.userId
        val data = contextual.data

        val userProfileMap = data.profiles.associateBy { it.username }

        val usernames = data.profiles.map { it.username }
        requireProfileImportAccess(contextual, usernames)
        val existingUserProfiles = (UsersTable innerJoin UserProfilesTable).selectAll().where{
            UsersTable.username inList usernames
        }.toList()
        val userProfilesToCreate = usernames.filter { it !in existingUserProfiles.map { userProfile -> userProfile[UsersTable.username]} }
        val existingUsers = UserEntity.find { UsersTable.username inList userProfilesToCreate }.toList()
        val usersToCreate = userProfilesToCreate.filter { it !in existingUsers.map { user -> user.username} }

        val recentlyAddedUserAccounts = usersToCreate.map {
            createUserEntity(CreateUser(it, null, ApiUserStatus.PENDING), userId)
        }
        val userProfiles: List<UserProfileEntity> = listOf(existingUsers, recentlyAddedUserAccounts).flatten().distinctBy { it.username }.map { user ->
            val userProfileData = userProfileMap[user.username]!!
            val userProfile = UserProfileEntity.new {
                this.user = user
                this.title = userProfileData.title
                this.firstName = userProfileData.firstName
                this.lastName = userProfileData.lastName
                createdBy = userId
            }

            val addressData = userProfileData.address
            val address = AddressEntity.new {
                this.userProfile = userProfile
                createdBy = userId
                recipientName = addressData.recipientName
                organizationName = addressData.organizationName
                addressLine1 = addressData.addressLine1
                addressLine2 = addressData.addressLine2
                city = addressData.city
                postalCode = addressData.postalCode
                countryCode = addressData.countryCode
                stateOrProvince = addressData.stateOrProvince
            }

            userProfile.addresses + address
            userProfile
        }

        UserProfiles(userProfiles.map { userProfile ->
            userProfile.toApiType(this)
        })
    } } x database }
}

@MathDsl
@Suppress("FunctionName")
fun ReadUserProfiles(): KlAction<Result<Contextual<ReadUserProfiles>>, Result<UserProfiles>> = KlAction{ result ->
    DbAction { database -> result bindSuspend {contextual -> resultTransaction(database) {
        val userIds = contextual.data.userIds.map(UUID::fromString)
        requireProfileReadAccess(contextual, userIds)

        val userProfileList = UserProfileEntity.find { UserProfilesTable.userId inList userIds }.toList().map { it.toApiType(this)  }
        UserProfiles(userProfileList)
    } } x database }
}

private fun Transaction.requireProfileReadAccess(request: Contextual<ReadUserProfiles>, userIds: List<UUID>) {
    val organizationId = request.data.organizationId
    if (organizationId == null && userIds.isNotEmpty() && userIds.all { it == request.userId }) return

    if (organizationId == null) {
        if (!isGranted(request.userId, UUID.fromString(request.context), "MANAGE_USERS")) throw PermissionException.AccessDenied
        return
    }
    val organization = requireManagedProfileOrganization(request.userId, organizationId)
    val members = organization.members.map { it.id.value }.toSet()
    if (!members.containsAll(userIds)) throw PermissionException.AccessDenied
}

private fun Transaction.requireProfileImportAccess(request: Contextual<ImportUserProfiles>, usernames: List<String>) {
    val organizationId = request.data.organizationId
    if (organizationId == null) {
        if (!isGranted(request.userId, UUID.fromString(request.context), "MANAGE_USERS")) throw PermissionException.AccessDenied
        return
    }
    val organization = requireManagedProfileOrganization(request.userId, organizationId)
    val members = organization.members.map { it.username }.toSet()
    if (!members.containsAll(usernames)) throw PermissionException.AccessDenied
}

private fun Transaction.requireManagedProfileOrganization(actorId: UUID, organizationId: String): OrganizationEntity {
    val organization = OrganizationEntity.findById(UUID.fromString(organizationId)) ?: throw PermissionException.AccessDenied
    if (organization.members.none { it.id.value == actorId } || !isGranted(actorId, organization.context.id.value, "MANAGE_USERS")) {
        throw PermissionException.AccessDenied
    }
    return organization
}

@MathDsl
@Suppress("FunctionName")
fun UpdateUserProfile(): KlAction<Result<Contextual<UpdateUserProfile>>, Result<UserProfile>> = KlAction{ result ->
    DbAction { database -> result bindSuspend {contextual -> resultTransaction(database) {
        val userId = contextual.userId
        val data = contextual.data
        updateUserProfile(data, userId).toApiType(this)
    } } x database }
}
