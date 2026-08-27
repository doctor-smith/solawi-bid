package org.evoleq.exposedx.iql.relation

import org.evoleq.exposedx.iql.execute
import org.evoleq.exposedx.iql.references
import org.evoleq.exposedx.iql.registry
import org.evoleq.exposedx.test.runSimpleH2Test
import org.evoleq.iql.dsl.query
import org.jetbrains.exposed.sql.insert
import kotlin.test.Test
import kotlin.test.assertEquals


class ManyToManyRelationJoinExecutionTest {
    @Test
    fun `manyToMany additional join filters relation`() {

        val tables = arrayOf(
            JoinTestUsers,
            JoinTestRoles,
            JoinTestContexts,
            JoinTestUserRoles
        )

        val registry =
            registry {

                entity("user", JoinTestUsers) {

                    field(JoinTestUsers.id)

                    manyToMany(
                        "rolesWithContext",
                        JoinTestRoles,
                        JoinTestUserRoles
                    ) {
                        source(
                            JoinTestUsers.id references JoinTestUserRoles.userId
                        )

                        target(
                            JoinTestRoles.id references JoinTestUserRoles.roleId
                        )

                        join(
                            "context",
                            JoinTestContexts.id references JoinTestUserRoles.contextId
                        )
                    }
                }

                entity("role", JoinTestRoles) {
                    field(JoinTestRoles.id)
                    field(JoinTestRoles.name)
                }

                entity("context", JoinTestContexts) {
                    field(JoinTestContexts.id)
                    field(JoinTestContexts.name)
                }
            }

        runSimpleH2Test(*tables) {

            // -----------------------------------------------------------------
            // Seed
            // -----------------------------------------------------------------

            JoinTestUsers.insert {
                it[id] = 1
            }

            JoinTestRoles.insert {
                it[id] = 10
                it[name] = "admin"
            }

            JoinTestContexts.insert {
                it[id] = 100
                it[name] = "production"
            }

            JoinTestContexts.insert {
                it[id] = 200
                it[name] = "development"
            }

            JoinTestUserRoles.insert {
                it[userId] = 1
                it[roleId] = 10
                it[contextId] = 100
            }

            // -----------------------------------------------------------------
            // Test
            // -----------------------------------------------------------------

            val query =
                query("user") {
                    where {
                        any("rolesWithContext"){p("context.name") eq "production" }
                    }
                }

            val result =
                execute(
                    query = query,
                    table = JoinTestUsers,
                    registry = registry
                )

            assertEquals(
                listOf(1),
                result.map { it[JoinTestUsers.id] }
            )
        }
    }

    @Test
    fun `manyToMany additional join excludes unrelated context`() {

        val tables = arrayOf(
            JoinTestUsers,
            JoinTestRoles,
            JoinTestContexts,
            JoinTestUserRoles
        )

        val registry =
            registry {

                entity("user", JoinTestUsers) {

                    field(JoinTestUsers.id)

                    manyToMany(
                        "rolesWithContext",
                        JoinTestRoles,
                        JoinTestUserRoles
                    ) {
                        source(
                            JoinTestUsers.id references JoinTestUserRoles.userId
                        )

                        target(
                            JoinTestRoles.id references JoinTestUserRoles.roleId
                        )

                        join(
                            "context",
                            JoinTestContexts.id references JoinTestUserRoles.contextId
                        )
                    }
                }

                entity("role", JoinTestRoles) {
                    field(JoinTestRoles.id)
                    field(JoinTestRoles.name)
                }

                entity("context", JoinTestContexts) {
                    field(JoinTestContexts.id)
                    field(JoinTestContexts.name)
                }
            }

        runSimpleH2Test(*tables) {

            // -----------------------------------------------------------------
            // Seed
            // -----------------------------------------------------------------

            JoinTestUsers.insert {
                it[id] = 1
            }

            JoinTestRoles.insert {
                it[id] = 10
                it[name] = "admin"
            }

            JoinTestContexts.insert {
                it[id] = 100
                it[name] = "production"
            }

            JoinTestContexts.insert {
                it[id] = 200
                it[name] = "development"
            }

            JoinTestUserRoles.insert {
                it[userId] = 1
                it[roleId] = 10
                it[contextId] = 100
            }

            // -----------------------------------------------------------------
            // Test
            // -----------------------------------------------------------------

            val query =
                query("user") {
                    where {
                       any ("rolesWithContext"){ p("name") eq "development" }
                    }
                }

            val result =
                execute(
                    query = query,
                    table = JoinTestUsers,
                    registry = registry
                )

            assertEquals(
                emptyList(),
                result
            )
        }
    }
}

fun joinTestRegistry() =
    registry {

        entity("user", JoinTestUsers) {
            field(JoinTestUsers.id)

            manyToMany(
                "roleContexts",
                JoinTestRoles,
                JoinTestUserRoles
            ) {
                source(
                    JoinTestUsers.id references JoinTestUserRoles.userId
                )

                target(
                    JoinTestRoles.id references JoinTestUserRoles.roleId
                )

                join(
                    "context",
                    JoinTestContexts.id references JoinTestUserRoles.contextId
                )
            }
        }

        entity("role", JoinTestRoles) {
            field(JoinTestRoles.id)
            field(JoinTestRoles.name)
        }

        entity("context", JoinTestContexts) {
            field(JoinTestContexts.id)
            field(JoinTestContexts.name)
        }
    }
