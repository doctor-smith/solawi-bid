package org.solyton.solawi.bid.module.user.data.transform

import org.solyton.solawi.bid.module.user.data.api.organization.ApiMember
import org.solyton.solawi.bid.module.user.data.api.organization.ApiOrganization
import org.solyton.solawi.bid.module.user.data.api.organization.ApiOrganizations
import org.solyton.solawi.bid.module.user.data.member.Member
import org.solyton.solawi.bid.module.user.data.organization.Organization
import org.solyton.solawi.bid.module.permissions.data.transform.toDomainType
import org.solyton.solawi.bid.module.user.data.address.Address
import org.solyton.solawi.bid.module.user.data.api.userprofile.ApiAddress
import org.solyton.solawi.bid.module.user.data.api.userprofile.ApiUserProfile
import org.solyton.solawi.bid.module.user.data.profile.UserProfile

fun ApiOrganizations.toDomainType(): List<Organization> = all.map {
    organization -> organization.toDomainType()
}

fun ApiOrganization.toDomainType(): Organization = Organization(
    organizationId = id,
    name = name,
    contextId = contextId,
    subOrganizations = subOrganizations.map {
        organization -> organization.toDomainType()
    },
    members = members.map {
        member -> member.toDomainType()
    }
)

fun ApiMember.toDomainType(): Member = Member(
    memberId = userId,
    username = username,
    roles = roles.map { role -> role.toDomainType() }
)

fun ApiUserProfile.toDomainType(): UserProfile = UserProfile(
    userProfileId = id,
    firstname = firstName,
    lastname = lastName,
    title = title,
    phoneNumber = phoneNumber,
    addresses = addresses.map{it.toDomainType()}
)

fun ApiAddress.toDomainType(): Address = Address(
    addressId = id,
    recipientName = recipientName,
    organizationName = organizationName,
    addressLine1 = addressLine1,
    addressLine2 = addressLine2,
    city = city,
    stateOrProvince = stateOrProvince,
    postalCode = postalCode,
    countryCode = countryCode
)
