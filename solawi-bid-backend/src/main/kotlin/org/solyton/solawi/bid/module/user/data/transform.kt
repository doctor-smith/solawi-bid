package org.solyton.solawi.bid.module.user.data

import org.jetbrains.exposed.sql.Transaction
import org.solyton.solawi.bid.module.permission.data.api.ApiRole
import org.solyton.solawi.bid.module.permission.repository.getRolesByUserAndContext
import org.solyton.solawi.bid.module.user.data.api.UserD
import org.solyton.solawi.bid.module.user.data.api.organization.ApiMember
import org.solyton.solawi.bid.module.user.data.api.organization.ApiOrganization
import org.solyton.solawi.bid.module.user.data.api.userprofile.ApiAddress
import org.solyton.solawi.bid.module.user.data.api.userprofile.ApiUserProfile
import org.solyton.solawi.bid.module.user.schema.AddressEntity
import org.solyton.solawi.bid.module.user.schema.OrganizationEntity
import org.solyton.solawi.bid.module.user.schema.UserProfileEntity
import org.solyton.solawi.bid.module.user.schema.repository.getChildren
import org.solyton.solawi.bid.module.user.schema.User as UserEntity

fun UserEntity.toApiType(): UserD = UserD(
    id.value,
    username,
    password
)

fun OrganizationEntity.toApiType(transaction: Transaction): ApiOrganization = ApiOrganization(
    id = id.value.toString(),
    name = name,
    contextId = context.id.value.toString(),
    members = members.map {
        user -> ApiMember(
            userId = user.id.value.toString(),
            username = user.username,
            roles = with(transaction) {
                getRolesByUserAndContext(user.id.value, context.id.value)
            }.map { role -> ApiRole(
                role.id.value.toString(),
                name = role.name,
                description = role.description,
                rights = listOf()
            ) }
        )
    },
    subOrganizations = getChildren().map {
        organization -> organization.toApiType(transaction)
    }
)

fun UserProfileEntity.toApiType(transaction: Transaction): ApiUserProfile =
    ApiUserProfile(
        id = id.value.toString(),
        firstName = firstName,
        lastName = lastName,
        title = title,
        phoneNumber = phoneNumber,
        address = with(transaction) {addresses.toList().toApiType().first()},
    )


fun List<AddressEntity>.toApiType(): List<ApiAddress> = map { address -> address.toApiType() }

fun AddressEntity.toApiType(): ApiAddress = ApiAddress(
    id = id.value.toString(),
    recipientName = recipientName,
    organizationName = organizationName,
    addressLine1 = addressLine1,
    addressLine2 = addressLine2,
    city = city,
    postalCode = postalCode,
    countryCode = countryCode,
    stateOrProvince = stateOrProvince
)
