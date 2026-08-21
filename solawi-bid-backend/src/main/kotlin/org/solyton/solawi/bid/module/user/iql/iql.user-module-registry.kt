package org.solyton.solawi.bid.module.user.iql

import org.evoleq.exposedx.iql.FieldNameStrategy
import org.evoleq.exposedx.iql.Registry
import org.evoleq.iql.data.*
import org.solyton.solawi.bid.module.user.schema.AddressesTable
import org.solyton.solawi.bid.module.user.schema.UserProfilesTable
import org.solyton.solawi.bid.module.user.schema.UsersTable

val userModuleRegistry: Registry by lazy { Registry(FieldNameStrategy.SNAKE_CASE).apply {
    registerEntity(
        EntityType(
            name = "user",
            table = "users",
            fields = mapOf(
                "username" to FieldInfo(
                    name = "username",
                    type = FieldType.STRING
                ),
                "password" to FieldInfo(
                    name = "password",
                    type = FieldType.STRING
                )
            ),
            relations = mapOf(
                "userProfiles" to RelationInfo(
                    name = "user_profile",
                    type = RelationType.ONE_TO_MANY,
                    targetEntity = "userProfile",
                    joinColumns = listOf("id"),
                    inverseJoinColumns = listOf("user_id")

                )
            )
        ),
        UsersTable
    )
    registerEntity(
        EntityType(
            name = "userProfile",
            table = "user_profiles",
            fields = mapOf(
                "userId" to FieldInfo(
                    name = "user_id",
                    type = FieldType.STRING
                ),
                "firstName" to FieldInfo(
                    name = "first_name",
                    type = FieldType.STRING
                ),
                "lastName" to FieldInfo(
                    name = "last_name",
                    type = FieldType.STRING
                ),
                "title" to FieldInfo(
                    name = "title",
                    type = FieldType.STRING
                ),
                "phoneNumber" to FieldInfo(
                    name = "phone_number",
                    type = FieldType.STRING
                ),
                "phoneNumber_1" to FieldInfo(
                    name = "phone_number_1",
                    type = FieldType.STRING
                ),
            ),
            relations = mapOf(
                "user" to RelationInfo(
                    name = "user",
                    type = RelationType.MANY_TO_ONE,
                    targetEntity = "userProfile",
                    joinColumns = listOf("user_id"),
                    inverseJoinColumns = listOf("id")
                ),
                "addresses" to RelationInfo(
                    name = "address",
                    type = RelationType.ONE_TO_MANY,
                    targetEntity = "address",
                    joinColumns = listOf("id"),
                    inverseJoinColumns = listOf("user_profile_id"),
                )
            )
        ),
        UserProfilesTable
    )
    registerEntity(
        EntityType(
            name = "address",
            table = "addresses",
            fields = mapOf(
                "recipientName" to FieldInfo(
                    name = "recipient_name",
                    type = FieldType.STRING
                ),
                "organizationName" to FieldInfo(
                    name = "organization_name",
                    type = FieldType.STRING
                ),
                "addressLine_1" to FieldInfo(
                    name = "address_line_1",
                    type = FieldType.STRING
                ),

                "addressLine_2" to FieldInfo(
                    name = "address_line_2",
                    type = FieldType.STRING
                ),
                "city" to FieldInfo(
                    name = "city",
                    type = FieldType.STRING
                ),
                "stateOrProvince" to FieldInfo(
                    name = "state_or_province",
                    type = FieldType.STRING
                ),
                "postalCode" to FieldInfo(
                    name = "postal_code",
                    type = FieldType.STRING
                ),
                "countryCode" to FieldInfo(
                    name = "country_code",
                    type = FieldType.STRING
                ),
            ),
            relations = mapOf(
                "userProfile" to RelationInfo(
                    name = "user_profile",
                    type = RelationType.MANY_TO_ONE,
                    targetEntity = "userProfile",
                    joinColumns = listOf("user_profile_id"),
                    inverseJoinColumns = listOf("id")
                )
            )
        ),
        AddressesTable
    )
} }
