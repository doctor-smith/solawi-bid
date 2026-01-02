package org.solyton.solawi.bid.module.user.data.api.userprofile

import kotlinx.serialization.Serializable


typealias ApiUserProfile = UserProfile
typealias ApiUserProfiles = UserProfiles


@Serializable
data class UserProfiles(val all: List<UserProfile>)

@Serializable
data class UserProfile(
    val id: String,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String?,
    val address: Address
)

@Serializable
data object ReadUserProfiles

@Serializable
data class ReadUserProfile(val userId: String)

@Serializable
data class CreateUserProfile(
    val userId: String,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String?,
    val address: Address
)

@Serializable
data class UpdateUserProfile(
    val userProfileId: String,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String?,
    val address: Address
)

@Serializable
data class DeleteUserProfile(val userProfileId: String)
