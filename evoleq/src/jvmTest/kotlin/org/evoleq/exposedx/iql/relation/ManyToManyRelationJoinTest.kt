package org.evoleq.exposedx.iql.relation

import org.evoleq.exposedx.iql.references
import org.evoleq.exposedx.iql.registry
import org.evoleq.iql.data.RelationJoin
import org.evoleq.iql.data.RelationType
import org.jetbrains.exposed.sql.Table
import kotlin.test.Test
import kotlin.test.assertEquals

object TestUsers : Table("test_users") {
    val id = integer("id")
    override val primaryKey = PrimaryKey(id)
}

object TestRoles : Table("test_roles") {
    val id = integer("id")
    val name = varchar("name", 100)

    override val primaryKey = PrimaryKey(id)
}

object TestContexts : Table("test_contexts") {
    val id = integer("id")
    val name = varchar("name", 100)

    override val primaryKey = PrimaryKey(id)
}



object TestTenants : Table("test_tenants") {
    val id = integer("id")
    override val primaryKey = PrimaryKey(id)
}

object JoinTestUsers : Table("join_test_users") {
    val id = integer("id")
    override val primaryKey = PrimaryKey(id)
}

object JoinTestRoles : Table("join_test_roles") {
    val id = integer("id")
    val name = varchar("name", 100)

    override val primaryKey = PrimaryKey(id)
}

object JoinTestContexts : Table("join_test_contexts") {
    val id = integer("id")
    val name = varchar("name", 100)

    override val primaryKey = PrimaryKey(id)
}

object JoinTestUserRoles : Table("join_test_user_roles") {
    val userId = integer("user_id")
    val roleId = integer("role_id")
    val contextId = integer("context_id")
}


object TestUserRoleContexts : Table("test_user_role_contexts") {
    val userId = integer("user_id")
    val roleId = integer("role_id")
    val contextId = integer("context_id")
    val tenantId = integer("tenant_id")
}

class ManyToManyRelationJoinTest  {
    @Test
    fun `manyToMany creates additional relation join`() {

        val registry =
            registry {

                entity("user", TestUsers) {

                    manyToMany(
                        "roleContexts",
                        TestRoles,
                        TestUserRoleContexts
                    ) {

                        source(
                            TestUsers.id references TestUserRoleContexts.userId
                        )

                        target(
                            TestRoles.id references TestUserRoleContexts.roleId
                        )

                        join(
                            "context",
                            TestContexts.id references TestUserRoleContexts.contextId
                        )
                    }
                }

                entity("role", TestRoles) {
                    field(TestRoles.name)
                }

                entity("context", TestContexts) {
                    field(TestContexts.name)
                }
            }

        val user =
            registry.getEntity("user")
                ?: error("user not registered")

        val relation =
            user.relations["roleContexts"]
                ?: error("relation not registered")

        assertEquals(
            RelationType.MANY_TO_MANY,
            relation.type
        )

        assertEquals(
            "test_user_role_contexts",
            relation.mapping?.table
        )

        assertEquals(
            1,
            relation.relationJoins.size
        )

        val join =
            relation.relationJoins.single()

        assertEquals(
            "test_contexts",
            join.entity
        )

        assertEquals(
            listOf("context_id"),
            join.mappingColumns
        )

        assertEquals(
            listOf("id"),
            join.targetColumns
        )
    }

    @Suppress("MapGetWithNotNullAssertionOperator")
    @Test
    fun `manyToMany creates multiple additional relation joins`() {

        val registry =
            registry {

                entity("role", TestRoles) {
                    field(TestRoles.name)
                }

                entity("context", TestContexts) {
                    field(TestContexts.name)
                }

                entity("tenant", TestTenants) {

                }

                entity("user", TestUsers) {

                    manyToMany(
                        "roleContexts",
                        TestRoles,
                        TestUserRoleContexts
                    ) {

                        source(
                            TestUsers.id references TestUserRoleContexts.userId
                        )

                        target(
                            TestRoles.id references TestUserRoleContexts.roleId
                        )

                        join(
                            "context",
                            TestContexts.id references TestUserRoleContexts.contextId
                        )

                        join(
                            "tenant",
                            TestTenants.id references TestUserRoleContexts.tenantId
                        )
                    }
                }


            }

        val relation =
            registry
                .getEntity("user")!!
                .relations["roleContexts"]!!

        assertEquals(
            listOf(
                RelationJoin(
                    entity = "test_contexts",
                    mappingColumns = listOf("context_id"),
                    targetColumns = listOf("id")
                ),
                RelationJoin(
                    entity = "test_tenants",
                    mappingColumns = listOf("tenant_id"),
                    targetColumns = listOf("id")
                )
            ),
            relation.relationJoins
        )
    }

    @Test
    fun `manyToMany registers additional relation join`() {

        val registry = joinTestRegistry()

        val relation =
            registry
                .getEntityOrThrow("user")
                .relations["roleContexts"]
                ?: error("Relation not found")

        assertEquals(
            RelationType.MANY_TO_MANY,
            relation.type
        )

        assertEquals(
            1,
            relation.relationJoins.size
        )

        assertEquals(
            RelationJoin(
                entity = JoinTestContexts.tableName,
                mappingColumns = listOf("context_id"),
                targetColumns = listOf("id")
            ),
            relation.relationJoins.single()
        )
    }


}
