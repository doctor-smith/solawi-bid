package org.solyton.solawi.bid.module.application.iql

import kotlinx.serialization.json.JsonPrimitive
import org.evoleq.exposedx.iql.ExposedCompiler
import org.evoleq.exposedx.iql.ExposedQueryCompiler
import org.evoleq.exposedx.iql.FieldNameStrategy
import org.evoleq.exposedx.test.runSimpleH2Test
import org.evoleq.iql.data.*
import org.evoleq.iql.dsl.query
import org.evoleq.iql.dsl.where
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.joda.time.DateTime
import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.DbFunctional
import org.solyton.solawi.bid.module.application.schema.*
import org.solyton.solawi.bid.module.permission.iql.permissionModuleRegistry
import org.solyton.solawi.bid.module.permission.schema.*
import org.solyton.solawi.bid.module.shares.service.UUID_1
import org.solyton.solawi.bid.module.user.iql.userModuleRegistry
import org.solyton.solawi.bid.module.user.schema.UserStatus
import org.solyton.solawi.bid.module.user.schema.Users
import org.solyton.solawi.bid.module.user.schema.UsersTable
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class ApplicationModuleRegistryTest {

    val tables = arrayOf(
        ApplicationsTable,
        ModulesTable,
        UserApplicationsTable,
        UserModulesTable,
        LifecycleStagesTable,

        UsersTable,

        ContextsTable,
        RightsTable,
        RolesTable,
        RoleRightContexts,
        UserRoleContext
    )

    @DbFunctional@Test
    fun `registry setup is valid`() {
        val registry = applicationModuleRegistry

        assertEquals(FieldNameStrategy.SNAKE_CASE, registry.fieldNameStrategy())
    }

    @DbFunctional@Test
    fun `application module registry includes permission module registry` () {
        val registry = applicationModuleRegistry

        val permissionEntities = permissionModuleRegistry.entities().keys

        assertTrue { registry.entities().keys.containsAll(permissionEntities) }

    }

    @DbFunctional@Test
    fun `application module registry includes user module registry` () {
        val registry = applicationModuleRegistry

        val userEntities = userModuleRegistry.entities().keys

        assertTrue { registry.entities().keys.containsAll(userEntities) }

    }


    @DbFunctional@Test
    fun `application is found by name of default context`() = runSimpleH2Test(*tables) {
        // val userId = UUID_1

        val defaultContextId = ContextsTable.insertAndGetId{
            it[name] = "TEST_CONTEXT"
            it[this.level] = 0
            it[left] = 0
            it[right] = 1
            it[createdBy] = UUID_1
            it[createdAt] = DateTime.now()
        }

        ApplicationsTable.insert{
            it[name] = "TEST_APP"
            it[description] = "TEST_APP_DESCRIPTION"
            it[this.defaultContextId] = defaultContextId
            it[createdBy] = UUID_1
            it[createdAt] = DateTime.now()
        }


        val query = query{
            where {
               p("defaultContext.name") eq "TEST_CONTEXT"
            }
        }

        val compiler = ExposedQueryCompiler(applicationModuleRegistry)

        val compiledQuery = compiler.compile(query, ApplicationsTable)

        val predicate = requireNotNull(compiledQuery.predicate)

        val applications = ApplicationEntity.find(predicate).toList()

        assertEquals(1, applications.size)

        val application = applications.first()

        assertEquals("TEST_APP", application.name)
    }




    @DbFunctional@Test
    fun `many-to-one relation filter returns matching rows`() =
        runSimpleH2Test(
            ApplicationsTable,
            ContextsTable
        ) {
            // Application 1 -> TEST
            // Application 2 -> OTHER

            val testContextId = ContextsTable.insertAndGetId {
                it[name] = "TEST_CONTEXT"
                it[this.level] = 0
                it[left] = 0
                it[right] = 1
                it[createdBy] = UUID_1
                it[createdAt] = DateTime.now()
            }

            val otherContextId = ContextsTable.insertAndGetId {
                it[name] = "OTHER_CONTEXT"
                it[this.level] = 0
                it[left] = 0
                it[right] = 1
                it[createdBy] = UUID_1
                it[createdAt] = DateTime.now()
            }

            ApplicationsTable.insert {

                it[name] = "App 1"
                it[defaultContextId] = testContextId
                it[description] = "TEST_APP_DESCRIPTION"
                it[createdBy] = UUID_1
                it[createdAt] = DateTime.now()
            }

            ApplicationsTable.insert {
                it[name] = "App 2"
                it[defaultContextId] = otherContextId
                it[description] = "TEST_APP_DESCRIPTION"
                it[createdBy] = UUID_1
                it[createdAt] = DateTime.now()
            }

            val filter =
                where {
                    p("defaultContext.name") eq "TEST_CONTEXT"
                }

            val result =
                ApplicationsTable
                    .selectAll()
                    .where {
                        ExposedCompiler(applicationModuleRegistry)
                            .compile(filter, ApplicationsTable)
                    }
                    .map {
                        it[ApplicationsTable.name]
                    }

            assertEquals(
                listOf("App 1"),
                result
            )
        }


    @DbFunctional@Test
    fun `filter roles `() = runSimpleH2Test(*tables) {

        val compiler = ExposedCompiler(applicationModuleRegistry)

        val testUserId = UsersTable.insertAndGetId {
            it[username] = "username"
            it[password] = "password"
            it[status] = UserStatus.ACTIVE
            it[createdBy] = UUID_1
            it[createdAt] = DateTime.now()
        }.value

        val testContextId = ContextsTable.insertAndGetId {
            it[name] = "TEST_CONTEXT"
            it[this.level] = 0
            it[left] = 0
            it[right] = 1
            it[createdBy] = UUID_1
            it[createdAt] = DateTime.now()
        }


        // val testAppId =
        ApplicationsTable.insertAndGetId {
            it[name] = "TEST_APP"
            it[defaultContextId] = testContextId
            it[description] = "TEST_APP_DESCRIPTION"
            it[createdBy] = UUID_1
            it[createdAt] = DateTime.now()
        }

        val testRoleId = RolesTable.insertAndGetId {
            it[name] = "TEST_ROLE"
            it[description] = "TEST_ROLE_DESCRIPTION"
            it[createdBy] = UUID_1
            it[createdAt] = DateTime.now()
        }

        val testRightId = RightsTable.insertAndGetId {
            it[name] = "TEST_RIGHT"
            it[description] = "TEST_RIGHT_DESCRIPTION"
            it[createdBy] = UUID_1
            it[createdAt] = DateTime.now()
        }

        RoleRightContexts.insert {
            it[roleId] = testRoleId
            it[rightId] = testRightId
            it[contextId] = testContextId
        }

        UserRoleContext.insert {
            it[userId] = testUserId
            it[roleId] = testRoleId
            it[contextId] = testContextId
        }



        val query = query {
            select("user")
            where {
                p("username") eq "username"
                any("roleContexts") {
                    and {
                        p("role.name") eq "TEST_ROLE"
                        p("context.name") eq "TEST_CONTEXT"
                    }
                }
            }
        }
        val compiledQuery = compiler.compile(query.filter!!, Users)

        val users =
            Users
                .selectAll()
                .where { compiledQuery }
                .toList()

        assertEquals(1, users.size)
        assertEquals(testUserId, users.single()[Users.id].value)
    }



    @DbFunctional@Test
    fun `resolves application default context root name`() {

        val compiler = ExposedCompiler(applicationModuleRegistry)

        val filter =
            query("application") {
                where {
                    p("defaultContext.root.name") eq "Global"
                }
            }.filter!!

        val resolved =
            compiler.resolveForTest(
                filter,
                applicationModuleRegistry.getEntityOrThrow("application")
            )

        assertEquals("context", resolved.entity)
        assertEquals("name", resolved.field)

        assertEquals(
            listOf(
                "application -> defaultContext -> context",
                "context -> root -> context"
            ),
            resolved.relationPath.map {
                "${it.sourceEntity.name} -> " +
                        "${it.relation.name} -> " +
                        it.targetEntity.name
            }
        )
    }


    @DbFunctional@Test
    fun `resolves application module application name`() {

        val compiler = ExposedCompiler(applicationModuleRegistry)

        val filter =
            query("application") {
                where {
                    p("modules.application.name") eq "Main"
                }
            }.filter!!

        val resolved =
            compiler.resolveForTest(
                filter,
                applicationModuleRegistry.getEntityOrThrow("application")
            )

        assertEquals("application", resolved.entity)
        assertEquals("name", resolved.field)

        assertEquals(
            listOf(
                "application -> modules -> module",
                "module -> application -> application"
            ),
            resolved.relationPath.map {
                "${it.sourceEntity.name} -> " +
                        "${it.relation.name} -> " +
                        it.targetEntity.name
            }
        )
    }

    @DbFunctional@Test
    fun `resolves application module default context name`() {
        val compiler = ExposedCompiler(applicationModuleRegistry)
        val filter =
            query("application") {
                where {
                    p("modules.defaultContext.name") eq "TEST"
                }
            }.filter!!

        val resolved =
            compiler.resolveForTest(
                filter,
                applicationModuleRegistry.getEntityOrThrow("application")
            )

        assertEquals("context", resolved.entity)
        assertEquals("name", resolved.field)

        assertEquals(
            listOf(
                "application -> modules -> module",
                "module -> defaultContext -> context"
            ),
            resolved.relationPath.map {
                "${it.sourceEntity.name} -> " +
                        "${it.relation.name} -> " +
                        it.targetEntity.name
            }
        )
    }

    @DbFunctional@Test
    fun `resolves application module default context root`() {
        val compiler = ExposedCompiler(applicationModuleRegistry)
        val filter =
            query("application") {
                where {
                    p("modules.defaultContext.root.name") eq "GLOBAL"
                }
            }.filter!!

        val resolved =
            compiler.resolveForTest(
                filter,
                applicationModuleRegistry.getEntityOrThrow("application")
            )

        assertEquals("context", resolved.entity)
        assertEquals("name", resolved.field)

        assertEquals(
            listOf(
                "application -> modules -> module",
                "module -> defaultContext -> context",
                "context -> root -> context"
            ),
            resolved.relationPath.map {
                "${it.sourceEntity.name} -> " +
                        "${it.relation.name} -> " +
                        it.targetEntity.name
            }
        )
    }

    @DbFunctional@Test
    fun `resolves application context roles rights`() {
        val compiler = ExposedCompiler(applicationModuleRegistry)
        val filter =
            query("application") {
                where {
                    p("defaultContext.roles.rights.name") eq "READ"
                }
            }.filter!!

        val resolved =
            compiler.resolveForTest(
                filter,
                applicationModuleRegistry.getEntityOrThrow("application")
            )

        assertEquals("right", resolved.entity)
        assertEquals("name", resolved.field)

        assertEquals(
            listOf(
                "application -> defaultContext -> context",
                "context -> roles -> role",
                "role -> rights -> right"
            ),
            resolved.relationPath.map {
                "${it.sourceEntity.name} -> " +
                        "${it.relation.name} -> " +
                        it.targetEntity.name
            }
        )
    }

    @DbFunctional@Test
    fun `resolves application context rights roles`() {
        val compiler = ExposedCompiler(applicationModuleRegistry)
        val filter =
            query("application") {
                where {
                    p("defaultContext.rights.roles.name") eq "ADMIN"
                }
            }.filter!!

        val resolved =
            compiler.resolveForTest(
                filter,
                applicationModuleRegistry.getEntityOrThrow("application")
            )

        assertEquals("role", resolved.entity)
        assertEquals("name", resolved.field)

        assertEquals(
            listOf(
                "application -> defaultContext -> context",
                "context -> rights -> right",
                "right -> roles -> role"
            ),
            resolved.relationPath.map {
                "${it.sourceEntity.name} -> " +
                        "${it.relation.name} -> " +
                        it.targetEntity.name
            }
        )
    }

    @DbFunctional@Test
    fun `resolves application modules context roles rights`() {
        val compiler = ExposedCompiler(applicationModuleRegistry)
        val filter =
            query("application") {
                where {
                    p("modules.defaultContext.roles.rights.description") eq "Can read"
                }
            }.filter!!

        val resolved =
            compiler.resolveForTest(
                filter,
                applicationModuleRegistry.getEntityOrThrow("application")
            )

        assertEquals("right", resolved.entity)
        assertEquals("description", resolved.field)

        assertEquals(
            listOf(
                "application -> modules -> module",
                "module -> defaultContext -> context",
                "context -> roles -> role",
                "role -> rights -> right"
            ),
            resolved.relationPath.map {
                "${it.sourceEntity.name} -> " +
                        "${it.relation.name} -> " +
                        it.targetEntity.name
            }
        )
    }

    @DbFunctional@Test
    fun `resolves module context root roles rights`() {
        val compiler = ExposedCompiler(applicationModuleRegistry)
        val filter =
            query("module") {
                where {
                    p("defaultContext.root.roles.rights.name") eq "WRITE"
                }
            }.filter!!

        val resolved =
            compiler.resolveForTest(
                filter,
                applicationModuleRegistry.getEntityOrThrow("module")
            )

        assertEquals("right", resolved.entity)
        assertEquals("name", resolved.field)

        assertEquals(
            listOf(
                "module -> defaultContext -> context",
                "context -> root -> context",
                "context -> roles -> role",
                "role -> rights -> right"
            ),
            resolved.relationPath.map {
                "${it.sourceEntity.name} -> " +
                        "${it.relation.name} -> " +
                        it.targetEntity.name
            }
        )
    }

    @DbFunctional
    @Test
    fun `resolves relation path from application`() {
        val compiler = ExposedCompiler(applicationModuleRegistry)

        val filter =
            query("application") {
                where {
                    p("defaultContext.name") eq "TEST"
                }
            }.filter!!

        val resolved =
            compiler.resolveForTest(
                filter,
                applicationModuleRegistry.getEntityOrThrow("application")
            )

        assertEquals("context", resolved.entity)
        assertEquals("name", resolved.field)

        assertEquals(
            listOf(
                "application -> defaultContext -> context"
            ),
            resolved.relationPath.map {
                "${it.sourceEntity.name} -> " +
                        "${it.relation.name} -> " +
                        it.targetEntity.name
            }
        )
    }

    @Test
    fun `resolves explicit entity prefix without current entity`() {
        val compiler = ExposedCompiler(applicationModuleRegistry)

        val resolved =
            compiler.resolveForTest(
                eq(
                    "context.name",
                    "TEST"
                ),
                null
            )

        assertEquals("context", resolved.entity)
        assertEquals("name", resolved.field)
        assertTrue(resolved.relationPath.isEmpty())
    }

    @DbFunctional@Test
    fun `relation wins over entity name when current entity has relation`() {
        val compiler = ExposedCompiler(applicationModuleRegistry)
        val filter =
            query("context") {
                where {
                    p("root.name") eq "ROOT"
                }
            }.filter!!

        val resolved =
            compiler.resolveForTest(
                filter,
                applicationModuleRegistry.getEntityOrThrow("context")
            )

        assertEquals("context", resolved.entity)
        assertEquals("name", resolved.field)

        assertEquals(
            listOf(
                "context -> root -> context"
            ),
            resolved.relationPath.map {
                "${it.sourceEntity.name} -> " +
                        "${it.relation.name} -> " +
                        it.targetEntity.name
            }
        )
    }

    @DbFunctional@Test
    fun `resolves deepest valid mixed relation path`() {
        val compiler = ExposedCompiler(applicationModuleRegistry)

        val filter =
            query("application") {
                where {
                    p(
                        "modules.defaultContext.roles.rights.contexts.name"
                    ) eq "GLOBAL"
                }
            }.filter!!

        val resolved =
            compiler.resolveForTest(
                filter,
                applicationModuleRegistry.getEntityOrThrow("application")
            )

        assertEquals("context", resolved.entity)
        assertEquals("name", resolved.field)

        assertEquals(
            listOf(
                "application -> modules -> module",
                "module -> defaultContext -> context",
                "context -> roles -> role",
                "role -> rights -> right",
                "right -> contexts -> context"
            ),
            resolved.relationPath.map {
                "${it.sourceEntity.name} -> " +
                        "${it.relation.name} -> " +
                        it.targetEntity.name
            }
        )
    }
}


internal fun ExposedCompiler.resolveForTest(
    filter: Filter,
    currentEntity: EntityType?
): ExposedCompiler.ResolvedField {

    require(filter is ComparisonFilter) {
        "resolveForTest currently expects ComparisonFilter"
    }

    return resolveField(
        field = filter.field,
        currentEntity = currentEntity
    )
}
internal data class ResolvedRelationPath(
    val entity: String,
    val field: String,
    val path: List<Triple<String, String, String>>
)

internal fun ExposedCompiler.resolveRelationPathForTest(
    field: FieldRef,
    currentEntity: EntityType
): ResolvedRelationPath {

    val resolved =
        resolveField(field, currentEntity)

    return ResolvedRelationPath(
        entity = resolved.entity,
        field = resolved.field,
        path =
            resolved.relationPath.map {
                Triple(
                    it.sourceEntity.name,
                    it.relation.name,
                    it.targetEntity.name
                )
            }
    )
}


private fun eq(
    path: String,
    value: String
): ComparisonFilter =
    ComparisonFilter(
        field = SimpleFieldRef(path),
        operator = Operator.EQ,
        value = JsonPrimitive(value)
    )

