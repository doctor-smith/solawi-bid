package org.solyton.solawi.bid.module.permission.iql

import org.evoleq.exposedx.iql.FieldNameStrategy
import org.evoleq.exposedx.iql.Registry
import org.evoleq.exposedx.iql.references
import org.evoleq.exposedx.iql.registry
import org.solyton.solawi.bid.module.permission.schema.*

val permissionModuleRegistry : Registry by lazy {
    registry {
        fieldNameStrategy = FieldNameStrategy.SNAKE_CASE

        entity("right", RightsTable) {
            field( RightsTable.name)
            field( RightsTable.description)

            manyToMany("contexts", ContextsTable, RoleRightContexts) {
                source(
                    RightsTable.id references RoleRightContexts.rightId
                )
                target(
                    ContextsTable.id references RoleRightContexts.contextId
                )
            }

            manyToMany("roles", RolesTable, RoleRightContexts) {
                source(
                    RightsTable.id references RoleRightContexts.rightId
                )
                target(
                    RolesTable.id references RoleRightContexts.roleId
                )
            }
        }
        entity("role", RolesTable) {
            field( RolesTable.name)
            field( RolesTable.description)

            manyToMany("contexts", ContextsTable, RoleRightContexts) {
                source(
                    RolesTable.id references RoleRightContexts.roleId
                )
                target(
                    ContextsTable.id references RoleRightContexts.contextId
                )
            }

            manyToMany("rights", RightsTable, RoleRightContexts) {
                source(
                    RolesTable.id references RoleRightContexts.rightId
                )
                target(
                    RightsTable.id references RoleRightContexts.rightId
                )
            }
        }
        entity("context", ContextsTable) {
            field(ContextsTable.id)
            field( ContextsTable.name)
            field(ContextsTable.left)
            field(ContextsTable.right)
            field(ContextsTable.level)

            manyToOne("root", ContextsTable) {
                ContextsTable.rootId references ContextsTable.id
            }


            manyToMany("rights", RightsTable, RoleRightContexts) {
                source(
                    ContextsTable.id references RoleRightContexts.contextId
                )
                target(
                    RightsTable.id references RoleRightContexts.rightId
                )
            }

            manyToMany("roles", RolesTable, RoleRightContexts) {
                source(
                    ContextsTable.id references RoleRightContexts.roleId
                )
                target(
                    RolesTable.id references RoleRightContexts.roleId
                )
            }
        }

        entity("userRoleContext", UserRoleContext) {
            field(UserRoleContext.userId)
            field(UserRoleContext.contextId)
            field(UserRoleContext.roleId)
        }
    }
}
