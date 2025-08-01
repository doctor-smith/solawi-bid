package org.solyton.solawi.bid.module.user

import org.evoleq.exposedx.test.runSimpleH2Test
import org.jetbrains.exposed.sql.insert
import org.solyton.solawi.bid.module.permission.action.db.getRoleRightContexts
import org.solyton.solawi.bid.module.permission.data.api.Context
import org.solyton.solawi.bid.module.permission.data.api.Right
import org.solyton.solawi.bid.module.permission.data.api.Role
import org.solyton.solawi.bid.module.permission.schema.ContextEntity
import org.solyton.solawi.bid.module.permission.schema.ContextsTable
import org.solyton.solawi.bid.module.permission.schema.RightEntity
import org.solyton.solawi.bid.module.permission.schema.RightsTable
import org.solyton.solawi.bid.module.permission.schema.RoleEntity
import org.solyton.solawi.bid.module.permission.schema.RoleRightContexts
import org.solyton.solawi.bid.module.permission.schema.RolesTable
import org.solyton.solawi.bid.module.user.schema.UserEntity
import org.solyton.solawi.bid.module.permission.schema.UserRoleContext
import org.solyton.solawi.bid.module.user.schema.UsersTable
import kotlin.test.assertEquals

class UserRoleRightContextTests {

    //@DbFunctional@Test todo:dev fix and enable - tests interfere when running all together
    fun getRoleRightContextOfUserTest() = runSimpleH2Test(
        RoleRightContexts,
        UserRoleContext,
        UsersTable,
        ContextsTable,
        RightsTable,
        RolesTable,

        ) {
        val ts = System.currentTimeMillis()
        // Setup database entries
        val user = UserEntity.new {
            username = "x-$ts"
            password = "y"
        }
        val readRight = RightEntity.new {
            name = "READ-$ts"
            description = ""
        }
        val createRight = RightEntity.new {
            name = "CREATE-$ts"
            description = ""
        }
        val updateRight = RightEntity.new {
            name = "UPDATE-$ts"
            description = ""
        }
        val creator = RoleEntity.new {
            name = "CREATOR-$ts"
            description = ""
        }

        val reader = RoleEntity.new {
            name = "READER-$ts"
            description = ""
        }
        val updater = RoleEntity.new {
            name = "UPDATER-$ts"
            description = ""
        }
        val context = ContextEntity.new {
            name = "APP-$ts"

        }
        RoleRightContexts.insert {
            it[roleId] = creator.id
            it[contextId] = context.id
            it[rightId] = createRight.id
        }
        RoleRightContexts.insert {
            it[roleId] = reader.id
            it[contextId] = context.id
            it[rightId] = readRight.id
        }

        RoleRightContexts.insert {
            it[roleId] = updater.id
            it[contextId] = context.id
            it[rightId] = updateRight.id
        }
        RoleRightContexts.insert {
            it[roleId] = updater.id
            it[contextId] = context.id
            it[rightId] = readRight.id
        }

        UserRoleContext.insert {
            it[contextId] = context.id
            it[userId] = user.id.value
            it[roleId] = updater.id
        }

        val  permissions = getRoleRightContexts(user.id.value)
        assertEquals(1,permissions.size)

        val readContext = permissions.first()
        val expectedContext = Context(
            id = context.id.value.toString(),
            name = context.name,
            roles = listOf(
                Role(
                    id = updater.id.value.toString(),
                    name = updater.name,
                    description = updater.description,
                    rights = listOf(
                        Right(
                            id = updateRight.id.value.toString(),
                            name = updateRight.name,
                            description = updateRight.description
                        ),
                        Right(
                            id = readRight.id.value.toString(),
                            name = readRight.name,
                            description = readRight.description
                        ),
                    )
                )
            )
        )
        assertEquals(expectedContext, readContext)
    }

}
