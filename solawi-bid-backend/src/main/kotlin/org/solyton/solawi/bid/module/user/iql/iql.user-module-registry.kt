package org.solyton.solawi.bid.module.user.iql

import org.evoleq.exposedx.iql.FieldNameStrategy
import org.evoleq.exposedx.iql.Registry
import org.evoleq.exposedx.iql.mapsTo
import org.evoleq.exposedx.iql.registry
import org.evoleq.iql.data.FieldType
import org.jetbrains.exposed.sql.jodatime.DateColumnType
import org.jetbrains.exposed.sql.jodatime.DateTimeWithTimeZoneColumnType
import org.solyton.solawi.bid.module.user.schema.AddressesTable
import org.solyton.solawi.bid.module.user.schema.UserProfilesTable
import org.solyton.solawi.bid.module.user.schema.UsersTable


val userModuleRegistry: Registry by lazy {
    registry {
        fieldNameStrategy = FieldNameStrategy.SNAKE_CASE

        fieldTypes(
            DateColumnType::class mapsTo FieldType.DATE,
            DateTimeWithTimeZoneColumnType::class mapsTo FieldType.DATETIME
        )

        entity("user", UsersTable) {
            field(UsersTable.username)
            field(UsersTable.password)

            oneToMany("userProfiles", UserProfilesTable) {
                UsersTable.id references UserProfilesTable.userId
            }
        }

        entity("userProfile", UserProfilesTable) {
            field(UserProfilesTable.userId)
            field(UserProfilesTable.firstName)
            field(UserProfilesTable.lastName)
            field(UserProfilesTable.title)
            field(UserProfilesTable.phoneNumber)
            field(UserProfilesTable.phoneNumber1)

            manyToOne("user", UsersTable) {
                UserProfilesTable.userId references UsersTable.id
            }

            oneToMany("addresses", AddressesTable) {
                UserProfilesTable.id references AddressesTable.userProfileId
            }
        }

        entity("address", AddressesTable) {
            field(AddressesTable.recipientName)
            field(AddressesTable.organizationName)
            field(AddressesTable.addressLine1)
            field(AddressesTable.addressLine2)
            field(AddressesTable.city)
            field(AddressesTable.stateOrProvince)
            field(AddressesTable.postalCode)
            field(AddressesTable.countryCode)

            manyToOne("userProfile", UserProfilesTable) {
                AddressesTable.userProfileId references UserProfilesTable.id
            }
        }
    }
}
