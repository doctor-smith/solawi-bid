package org.evoleq.exposedx.iql.relation

import org.evoleq.exposedx.iql.execute
import org.evoleq.exposedx.iql.references
import org.evoleq.exposedx.iql.registry
import org.evoleq.exposedx.test.runSimpleH2Test
import org.evoleq.iql.dsl.query
import org.jetbrains.exposed.sql.insert
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith


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

    // -----------------------------------------------------------------------------
    // Priority 1
    // Normal relation wins over relationJoin with the same name.
    // -----------------------------------------------------------------------------

    @Test
    fun `normal relation has priority over relation join`() {

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

                    field(JoinTestRoles.name)

                    /*
                     * Deliberately register a normal relation named "context".
                     *
                     * This relation must win over the relationJoin with the
                     * same name.
                     */
                    manyToOne(
                        "context",
                        JoinTestContexts
                    ) {
                        JoinTestRoles.contextId references JoinTestContexts.id
                    }
                }

                entity("context", JoinTestContexts) {
                    field(JoinTestContexts.name)
                }
            }

        runSimpleH2Test(*tables) {

            JoinTestUsers.insert {
                it[id] = 1
            }

            JoinTestRoles.insert {
                it[id] = 10
                it[name] = "admin"
                it[contextId] = 200
            }

            JoinTestContexts.insert {
                it[id] = 100
                it[name] = "production"
            }

            JoinTestContexts.insert {
                it[id] = 200
                it[name] = "development"
            }

            /*
             * M:N relation points to production.
             *
             * Normal role.context relation points to development.
             */
            JoinTestUserRoles.insert {
                it[userId] = 1
                it[roleId] = 10
                it[contextId] = 100
            }

            val query =
                query("user") {
                    where {
                        any("rolesWithContext") {
                            p("context.name") eq "development"
                        }
                    }
                }

            val result =
                execute(
                    query = query,
                    table = JoinTestUsers,
                    registry = registry
                )

            /*
             * If the normal relation wins, the user matches because:
             *
             * role.context -> development
             *
             * If the relationJoin were incorrectly preferred, the result
             * would be empty because the M:N join points to production.
             */
            assertEquals(
                listOf(1),
                result.map { it[JoinTestUsers.id] }
            )
        }

    }
    // -----------------------------------------------------------------------------
    // Priority 2
    // No normal relation exists -> relationJoin must be used.
    // -----------------------------------------------------------------------------

    @Test
    fun `relation join is used when no normal relation exists`() {

        val tables = arrayOf(
            JoinTestUsers,
            JoinTestRoles,
            JoinTestContexts,
            JoinTestUserRoles
        )

        val registry =
            joinTestRegistry()

        runSimpleH2Test(*tables) {

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

            JoinTestUserRoles.insert {
                it[userId] = 1
                it[roleId] = 10
                it[contextId] = 100
            }

            val query =
                query("user") {
                    where {
                        any("roleContexts") {
                            p("context.name") eq "production"
                        }
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


    // -----------------------------------------------------------------------------
    // Priority 3
    // Neither normal relation nor relationJoin exists.
    // -----------------------------------------------------------------------------

    @Test
    fun `unknown nested relation is rejected`() {

        val tables = arrayOf(
            JoinTestUsers,
            JoinTestRoles,
            JoinTestContexts,
            JoinTestUserRoles
        )

        val registry =
            joinTestRegistry()

        runSimpleH2Test(*tables) {

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

            JoinTestUserRoles.insert {
                it[userId] = 1
                it[roleId] = 10
                it[contextId] = 100
            }

            val query =
                query("user") {
                    where {
                        any("roleContexts") {
                            p("doesNotExist.name") eq "production"
                        }
                    }
                }

            assertFailsWith<IllegalArgumentException> {

                execute(
                    query = query,
                    table = JoinTestUsers,
                    registry = registry
                )
            }
        }
    }


    // -----------------------------------------------------------------------------
    // RelationJoin exists, but the requested field does not.
    // -----------------------------------------------------------------------------

    @Test
    fun `relation join with unknown field is rejected`() {

        val tables = arrayOf(
            JoinTestUsers,
            JoinTestRoles,
            JoinTestContexts,
            JoinTestUserRoles
        )

        val registry =
            joinTestRegistry()

        runSimpleH2Test(*tables) {

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

            JoinTestUserRoles.insert {
                it[userId] = 1
                it[roleId] = 10
                it[contextId] = 100
            }

            val query =
                query("user") {
                    where {
                        any("roleContexts") {
                            p("context.doesNotExist") eq "production"
                        }
                    }
                }

            assertFailsWith<IllegalArgumentException> {

                execute(
                    query = query,
                    table = JoinTestUsers,
                    registry = registry
                )
            }
        }
    }


    // -----------------------------------------------------------------------------
    // Unqualified field must still resolve against the M:N target entity.
    // -----------------------------------------------------------------------------

    @Test
    fun `unqualified field still resolves against many to many target`() {

        val tables = arrayOf(
            JoinTestUsers,
            JoinTestRoles,
            JoinTestContexts,
            JoinTestUserRoles
        )

        val registry =
            joinTestRegistry()

        runSimpleH2Test(*tables) {

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

            JoinTestUserRoles.insert {
                it[userId] = 1
                it[roleId] = 10
                it[contextId] = 100
            }

            val query =
                query("user") {
                    where {
                        any("roleContexts") {
                            p("name") eq "admin"
                        }
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
