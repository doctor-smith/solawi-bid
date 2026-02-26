package org.solyton.solawi.bid.module.user.repository

import org.jetbrains.exposed.sql.Transaction
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.user.data.api.userprofile.CreateUserProfile
import org.solyton.solawi.bid.module.user.data.api.userprofile.UpdateUserProfile
import org.solyton.solawi.bid.module.user.exception.UserManagementException
import org.solyton.solawi.bid.module.user.schema.UserProfileEntity
import org.solyton.solawi.bid.module.user.schema.UserProfilesTable
import org.solyton.solawi.bid.module.user.service.validateUserExists
import org.solyton.solawil.bid.module.bid.data.api.toUUID
import java.util.*

fun Transaction.createUserProfile(
    data: CreateUserProfile,
    creatorId: UUID
): UserProfileEntity {
    val user = validateUserExists(data.userId.toUUID())
    val profile = UserProfileEntity.new {
        createdBy = creatorId
        this.user = user
        firstName = data.firstname.value
        lastName = data.lastname.value
        title = data.title?.value
        phoneNumber = data.phoneNumber?.value
    }

    val address = createAddress(
        recipientName = data.lastname.value,
        organizationName = data.address.organizationName,
        addressLine1 = data.address.addressLine1,
        addressLine2 = data.address.addressLine2,
        city = data.address.city,
        stateOrProvince = data.address.stateOrProvince,
        postalCode = data.address.postalCode,
        countryCode = data.address.countryCode,
        creator = creatorId
    )
    address.userProfile = profile

    return profile
}

fun Transaction.updateUserProfile(
    data: UpdateUserProfile,
    modifierId: UUID
): UserProfileEntity {
    val userProfile = validatedUserProfile(data.userProfileId.toUUID())
    val changes = booleanArrayOf(
        data.title?.value != userProfile.title,
        data.phoneNumber?.value != userProfile.phoneNumber,
        data.firstname.value != userProfile.firstName,
        data.lastname.value != userProfile.lastName
    )

    data.addresses.forEach { address -> updateAddress(
        UUID.fromString(address.id),
        address.recipientName,
        address.organizationName,
        address.addressLine1,
        address.addressLine2,
        address.city,
        address.stateOrProvince,
        address.postalCode,
        address.countryCode,
        modifierId
    )}

    return userProfile.apply {
        title = data.title?.value ?: title
        phoneNumber = data.phoneNumber?.value ?: phoneNumber
        firstName = data.firstname.value
        lastName = data.lastname.value
        if(changes.any { it }) {
            modifiedBy = modifierId
            modifiedAt = DateTime.now()
        }
    }
}

@Suppress("UNUSED_PARAMETER")
fun Transaction.deleteUserProfile(profile: UserProfileEntity) {
    TODO("implement")
}

fun Transaction.validatedUserProfile(userProfileId: UUID): UserProfileEntity = UserProfileEntity.find {
    UserProfilesTable.id eq userProfileId
}.firstOrNull() ?: throw UserManagementException.NoSuchUserProfile("$userProfileId")
