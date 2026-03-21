package org.solyton.solawi.bid.module.user.data.api.userprofile

import kotlinx.serialization.Serializable
import org.solyton.solawi.bid.module.values.*


typealias ApiUserProfile = UserProfile
typealias ApiUserProfiles = UserProfiles


@Serializable
data class UserProfiles(val all: List<UserProfile>)

@Serializable
data class UserProfile(
    val id: String,
    val userId: String,
    val firstName: String,
    val lastName: String,
    val title: String?,
    val phoneNumber: String?,
    val phoneNumber1: String?,
    val addresses: List<Address>
)

@Serializable
data class ReadUserProfiles(val userIds: List<String>)

@Serializable
data class ReadUserProfile(val userId: String)

@Serializable
data class CreateUserProfile(
    val userId: UserId,
    val firstname: Firstname,
    val lastname: Lastname,
    val title: Title?,
    val phoneNumber: PhoneNumber?,
    val phoneNumber1: PhoneNumber?,
    val address: CreateAddress
)

@Serializable
data class UpdateUserProfile(
    val userProfileId: UserProfileId,
    val userId: UserId, // needed for access check
    val firstname: Firstname,
    val lastname: Lastname,
    val title: Title?,
    val phoneNumber: PhoneNumber?,
    val phoneNumber1: PhoneNumber?,
    val addresses: List<UpdateAddress>
)

@Serializable
data class DeleteUserProfile(val userProfileId: UserProfileId)

@Serializable
data class UserProfileToImport(
    val username: String,
    val firstName: String,
    val lastName: String,
    val title: String?,
    val phoneNumber: String?,
    val phoneNumber1: String?,
    val address: CreateAddress
)

@Serializable
data class ImportUserProfiles(val profiles: List<UserProfileToImport>)
