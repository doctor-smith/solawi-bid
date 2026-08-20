package org.evoleq.exposedx.iql

import kotlinx.serialization.json.Json
import org.evoleq.exposedx.test.runSimpleH2Test
import org.evoleq.iql.data.*
import org.evoleq.iql.dsl.query
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManyToManyIqlTest {

    // -------------------------------------------------------------------------
    // Tables
    // -------------------------------------------------------------------------

    object Users : Table("users") {
        val id =
            integer("id")
    }

    object Roles : Table("roles") {
        val id =
            integer("id")

        val name =
            varchar("name", 100)
    }

    object UserRoles : Table("user_roles") {
        val userId =
            integer("user_id")

        val roleId =
            integer("role_id")
    }

    // -------------------------------------------------------------------------
    // Registry
    // -------------------------------------------------------------------------

    private fun registry(): Registry {

        val registry = Registry()

        registry.registerEntity(
            EntityType(
                name = "User",
                table = "users",

                fields = mapOf(
                    "id" to FieldInfo(
                        name = "id",
                        type = FieldType.INTEGER
                    )
                ),

                relations = mapOf(
                    "roles" to RelationInfo(
                        name = "roles",
                        type = RelationType.MANY_TO_MANY,
                        targetEntity = "Role",

                        joinColumns = listOf("id"),
                        inverseJoinColumn = "id",

                        mapping = MappingInfo(
                            table = "user_roles",

                            sourceColumns =
                                listOf("id"),

                            mappingSourceColumns =
                                listOf("user_id"),

                            mappingTargetColumns =
                                listOf("role_id"),

                            targetColumns =
                                listOf("id")
                        )
                    )
                )
            ),
            Users
        )

        registry.registerEntity(
            EntityType(
                name = "Role",
                table = "roles",

                fields = mapOf(
                    "id" to FieldInfo(
                        name = "id",
                        type = FieldType.INTEGER
                    ),

                    "name" to FieldInfo(
                        name = "name",
                        type = FieldType.STRING
                    )
                ),

                relations = emptyMap()
            ),
            Roles
        )

        registry.registerMappingTable(
            "user_roles",
            UserRoles
        )

        return registry
    }

    // -------------------------------------------------------------------------
    // Metadata
    // -------------------------------------------------------------------------

    @Test
    fun `many-to-many relation contains mapping information`() {

        val registry =
            registry()

        val relation =
            registry.getRelation(
                entityName = "User",
                relationName = "roles"
            )

        assertEquals(
            RelationType.MANY_TO_MANY,
            relation.type
        )

        assertEquals(
            "Role",
            relation.targetEntity
        )

        assertEquals(
            "user_roles",
            relation.mapping?.table
        )

        assertEquals(
            listOf("id"),
            relation.mapping?.sourceColumns
        )

        assertEquals(
            listOf("user_id"),
            relation.mapping?.mappingSourceColumns
        )

        assertEquals(
            listOf("role_id"),
            relation.mapping?.mappingTargetColumns
        )

        assertEquals(
            listOf("id"),
            relation.mapping?.targetColumns
        )
    }

    // -------------------------------------------------------------------------
    // Serialization
    // -------------------------------------------------------------------------

    @Test
    fun `many-to-many relation is serializable`() {

        val relation =
            RelationInfo(
                name = "roles",
                type = RelationType.MANY_TO_MANY,
                targetEntity = "Role",

                joinColumns = listOf("id"),
                inverseJoinColumn = "id",

                mapping = MappingInfo(
                    table = "user_roles",
                    sourceColumns = listOf("id"),
                    mappingSourceColumns = listOf("user_id"),
                    mappingTargetColumns = listOf("role_id"),
                    targetColumns = listOf("id")
                )
            )

        val json =
            Json.encodeToString(
                RelationInfo.serializer(),
                relation
            )

        val restored =
            Json.decodeFromString(
                RelationInfo.serializer(),
                json
            )

        assertEquals(
            relation,
            restored
        )
    }

    // -------------------------------------------------------------------------
    // SQL execution
    // -------------------------------------------------------------------------

    @Test
    fun `ANY roles finds users through mapping table`() =
        runSimpleH2Test(
            Users,
            Roles,
            UserRoles
        ) {

            Users.insert {
                it[id] = 1
            }

            Users.insert {
                it[id] = 2
            }

            Roles.insert {
                it[id] = 10
                it[name] = "admin"
            }

            Roles.insert {
                it[id] = 20
                it[name] = "user"
            }

            UserRoles.insert {
                it[userId] = 1
                it[roleId] = 10
            }

            UserRoles.insert {
                it[userId] = 2
                it[roleId] = 20
            }

            val filter =
                QuantifierFilter(
                    quantifier = Quantifier.ANY,

                    relation = RelationRef(
                        entity = "User",
                        relation = "roles"
                    ),

                    filter =
                        ComparisonFilter(
                            field =
                                SimpleFieldRef(
                                    "Role.name"
                                ),

                            operator =
                                Operator.EQ,

                            value =
                                kotlinx.serialization.json.JsonPrimitive(
                                    "admin"
                                )
                        )
                )

            val compiler =
                ExposedCompiler(
                    registry()
                )

            val predicate =
                compiler.compile(
                    filter,
                    Users
                )

            val result =
                Users
                    .selectAll()
                    .where {
                        predicate
                    }
                    .map {
                        it[Users.id]
                    }

            assertEquals(
                listOf(1),
                result
            )
        }

    // -------------------------------------------------------------------------
    // DSL
    // -------------------------------------------------------------------------

    @Test
    fun `DSL creates ANY relation filter`() {

        val query =
            query {

                where {

                    any("roles") {

                        p("Role.name") eq "admin"
                    }
                }
            }

        assertTrue(
            query.filter is QuantifierFilter
        )

        val filter =
            query.filter as QuantifierFilter

        assertEquals(
            Quantifier.ANY,
            filter.quantifier
        )

        assertEquals(
            "roles",
            filter.relation.relation
        )

        assertEquals(
            "Role.name",
            (
                    filter.filter
                            as ComparisonFilter
                    ).field.path
        )
    }
}

