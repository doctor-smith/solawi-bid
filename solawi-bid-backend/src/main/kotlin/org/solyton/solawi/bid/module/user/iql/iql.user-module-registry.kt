package org.solyton.solawi.bid.module.user.iql

import org.evoleq.exposedx.iql.*
import org.evoleq.iql.data.FieldType
import org.jetbrains.exposed.sql.jodatime.DateColumnType
import org.jetbrains.exposed.sql.jodatime.DateTimeWithTimeZoneColumnType
import org.solyton.solawi.bid.module.permission.iql.permissionModuleRegistry
import org.solyton.solawi.bid.module.permission.schema.ContextsTable
import org.solyton.solawi.bid.module.permission.schema.RolesTable
import org.solyton.solawi.bid.module.permission.schema.UserRoleContext
import org.solyton.solawi.bid.module.user.schema.*


val userModuleRegistry: Registry by lazy {
    registry {
        fieldNameStrategy = FieldNameStrategy.SNAKE_CASE

        include(permissionModuleRegistry)

        fieldTypes(
            DateColumnType::class mapsTo FieldType.DATE,
            DateTimeWithTimeZoneColumnType::class mapsTo FieldType.DATETIME
        )

        entity("user", UsersTable) {
            field(UsersTable.username)
            field(UsersTable.password)
            field(UsersTable.status)

            oneToMany("userProfiles", UserProfilesTable) {
                UsersTable.id references UserProfilesTable.userId
            }

            manyToMany("organizations", OrganizationsTable, UserOrganization) {

                source(
                    UsersTable.id references UserOrganization.userId
                )

                target(
                    OrganizationsTable.id references UserOrganization.organizationId
                )
            }

            manyToMany("roles", RolesTable, UserRoleContext) {
                source(UsersTable.id references UserRoleContext.userId)
                target(RolesTable.id references UserRoleContext.roleId )
            }

            manyToMany("contexts", ContextsTable, UserRoleContext) {
                source(UsersTable.id references UserRoleContext.contextId)
                target(ContextsTable.id references UserRoleContext.contextId)
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


        entity("organization", OrganizationsTable) {
            field(OrganizationsTable.name)
            field(OrganizationsTable.left)
            field(OrganizationsTable.right)
            field(OrganizationsTable.level)

            manyToOne("root", OrganizationsTable) {
                OrganizationsTable.rootId references OrganizationsTable.id
            }

            manyToOne("contexts", ContextsTable) {
                OrganizationsTable.contextId references ContextsTable.id
            }

            manyToMany("members", UsersTable, UserOrganization) {
                source(
                    OrganizationsTable.id references UserOrganization.organizationId
                )
                target(
                    UsersTable.id references UserOrganization.userId
                )
            }
        }
    }
}
