package org.solyton.solawi.bid.module.user.action.user

import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.Contextual
import org.evoleq.ktorx.DbAction
import org.evoleq.ktorx.KlAction
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.math.MathDsl
import org.evoleq.math.x
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.solyton.solawi.bid.module.user.data.api.CreateUser
import org.solyton.solawi.bid.module.user.data.api.userprofile.ImportUserProfiles
import org.solyton.solawi.bid.module.user.data.api.userprofile.ReadUserProfiles
import org.solyton.solawi.bid.module.user.data.api.userprofile.UserProfiles
import org.solyton.solawi.bid.module.user.data.toApiType
import org.solyton.solawi.bid.module.user.schema.AddressEntity
import org.solyton.solawi.bid.module.user.schema.UserEntity
import org.solyton.solawi.bid.module.user.schema.UserProfileEntity
import org.solyton.solawi.bid.module.user.schema.UserProfilesTable
import org.solyton.solawi.bid.module.user.schema.UsersTable
import org.solyton.solawi.bid.module.user.service.user.createUserEntity
import java.util.*

@MathDsl
@Suppress("FunctionName", "MapGetWithNotNullAssertionOperator", "UnsafeCallOnNullableType")
fun ImportProfiles(): KlAction<Result<Contextual<ImportUserProfiles>>, Result<UserProfiles>> = KlAction{ result ->
    DbAction { database -> result bindSuspend {contextual -> resultTransaction(database) {
        val userId = contextual.userId
        val data = contextual.data

        val userProfileMap = data.profiles.associateBy { it.username }

        val usernames = data.profiles.map { it.username }
        val existingUserProfiles = (UsersTable innerJoin UserProfilesTable).selectAll().where{
            UserProfilesTable.userId eq userId and (UsersTable.username inList usernames )
        }.toList()
        val userProfilesToCreate = usernames.filter { it !in existingUserProfiles.map { userProfile -> userProfile[UsersTable.username]} }
        val existingUsers = UserEntity.find { UsersTable.username inList userProfilesToCreate }.toList()
        val usersToCreate = usernames.filter { it !in existingUsers.map { user -> user.username} }

        val recentlyAddedUserAccounts = usersToCreate.map {
            createUserEntity(CreateUser(it, "NOT_SET"), userId)
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
        // val userId = contextual.userId
        val data = contextual.data
        val userIds = data.userIds.map { UUID.fromString(it) }

        val userProfileList = UserProfileEntity.find { UserProfilesTable.userId inList userIds }.toList().map { it.toApiType(this)  }
        UserProfiles(userProfileList)
    } } x database }
}
