
package org.evoleq.exposedx.iql

import org.evoleq.exposedx.test.runSimpleH2Test
import org.evoleq.iql.data.*
import org.evoleq.iql.dsl.map
import org.evoleq.iql.dsl.max
import org.evoleq.iql.dsl.query
import org.evoleq.iql.dsl.relation
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import kotlin.test.Test
import kotlin.test.assertEquals

class ExposedQueryCompilerExecutionTest {

    object Users : Table("users") {

        val id =
            integer("id")

        val name =
            varchar("name", 100)
    }

    object UserProfiles : Table("user_profiles") {

        val id =
            integer("id")

        val userId =
            integer("user_id")

        val lastName =
            varchar("last_name", 100)
    }

    private val registry =
        Registry().apply {

            registerEntity(
                EntityType(
                    name = "user",
                    table = "users",
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
                    relations = mapOf(
                        "userProfiles" to RelationInfo(
                            name = "userProfiles",
                            type = RelationType.ONE_TO_MANY,
                            targetEntity = "userProfile",
                            joinColumns = listOf("id"),
                            inverseJoinColumn = "user_id"
                        )
                    )
                ),
                Users
            )

            registerEntity(
                EntityType(
                    name = "userProfile",
                    table = "user_profiles",
                    fields = mapOf(
                        "id" to FieldInfo(
                            name = "id",
                            type = FieldType.INTEGER
                        ),
                        "user_id" to FieldInfo(
                            name = "user_id",
                            type = FieldType.INTEGER
                        ),
                        "last_name" to FieldInfo(
                            name = "last_name",
                            type = FieldType.STRING
                        )
                    ),
                    relations = emptyMap()
                ),
                UserProfiles
            )
        }

    private val compiler =
        ExposedQueryCompiler(registry)

    @Test
    fun `expression sort on one-to-many relation is compiled`() =
        runSimpleH2Test(
            Users,
            UserProfiles
        ) {

            Users.insert {
                it[id] = 1
                it[name] = "Alice"
            }

            Users.insert {
                it[id] = 2
                it[name] = "Bob"
            }

            UserProfiles.insert {
                it[id] = 1
                it[userId] = 1
                it[lastName] = "Müller"
            }

            UserProfiles.insert {
                it[id] = 2
                it[userId] = 1
                it[lastName] = "Adams"
            }

            UserProfiles.insert {
                it[id] = 3
                it[userId] = 2
                it[lastName] = "Smith"
            }

            val query =
                query {
                    asc(
                        relation("userProfiles")
                            .map("last_name")
                            .max()
                    )
                }

            val compiled =
                compiler.compile(
                    query = query,
                    table = Users
                )

            assertEquals(
                1,
                compiled.orderBy.size
            )

            assertEquals(
                SortOrder.ASC_NULLS_FIRST,
                compiled.orderBy.first().second
            )

            val result =
                Users
                    .selectAll()
                    .orderBy(
                        *compiled.orderBy.toTypedArray()
                    )
                    .map {
                        it[Users.name]
                    }

            assertEquals(
                listOf("Alice", "Bob"),
                result
            )
        }

    @Test
    fun `ASC expression sort on one-to-many relation returns users in correct order`() =
        runSimpleH2Test(
            Users,
            UserProfiles
        ) {

            Users.insert {
                it[id] = 1
                it[name] = "Alice"
            }

            Users.insert {
                it[id] = 2
                it[name] = "Bob"
            }

            UserProfiles.insert {
                it[id] = 1
                it[userId] = 1
                it[lastName] = "Smith"
            }

            UserProfiles.insert {
                it[id] = 2
                it[userId] = 1
                it[lastName] = "Adams"
            }

            UserProfiles.insert {
                it[id] = 3
                it[userId] = 2
                it[lastName] = "Brown"
            }

            val query =
                query {
                    asc(
                        relation("userProfiles")
                            .map("last_name")
                            .max()
                    )
                }

            val compiled =
                compiler.compile(
                    query = query,
                    table = Users
                )

            val result =
                Users
                    .selectAll()
                    .orderBy(
                        *compiled.orderBy.toTypedArray()
                    )
                    .map {
                        it[Users.name]
                    }

            assertEquals(
                listOf( "Bob", "Alice",),
                result
            )
        }


    @Test
    fun `DESC expression sort on one-to-many relation returns users in correct order`() =
        runSimpleH2Test(
            Users,
            UserProfiles
        ) {

            Users.insert {
                it[id] = 1
                it[name] = "Alice"
            }

            Users.insert {
                it[id] = 2
                it[name] = "Bob"
            }

            UserProfiles.insert {
                it[id] = 1
                it[userId] = 1
                it[lastName] = "Adams"
            }

            UserProfiles.insert {
                it[id] = 2
                it[userId] = 1
                it[lastName] = "Smith"
            }

            UserProfiles.insert {
                it[id] = 3
                it[userId] = 2
                it[lastName] = "Brown"
            }

            val query =
                query {
                    desc(
                        relation("userProfiles")
                            .map("last_name")
                            .max()
                    )
                }

            val compiled =
                compiler.compile(
                    query = query,
                    table = Users
                )

            val result =
                Users
                    .selectAll()
                    .orderBy(
                        *compiled.orderBy.toTypedArray()
                    )
                    .map {
                        it[Users.name]
                    }

            assertEquals(
                listOf("Alice", "Bob",),
                result
            )
        }


    @Test
    fun `expression sort with empty relation is supported`() =
        runSimpleH2Test(
            Users,
            UserProfiles
        ) {

            Users.insert {
                it[id] = 1
                it[name] = "Alice"
            }

            Users.insert {
                it[id] = 2
                it[name] = "Bob"
            }

            UserProfiles.insert {
                it[id] = 1
                it[userId] = 1
                it[lastName] = "Smith"
            }

            val query =
                query {
                    asc(
                        relation("userProfiles")
                            .map("last_name")
                            .max()
                    )
                }

            val compiled =
                compiler.compile(
                    query = query,
                    table = Users
                )

            val result =
                Users
                    .selectAll()
                    .orderBy(
                        *compiled.orderBy.toTypedArray()
                    )
                    .map {
                        it[Users.name]
                    }

            // null sort order NULLS_FIRST
            assertEquals(
                listOf("Bob", "Alice"),
                result
            )
        }


    @Test
    fun `expression sort and field sort can be combined`() =
        runSimpleH2Test(
            Users,
            UserProfiles
        ) {

            Users.insert {
                it[id] = 1
                it[name] = "Alice"
            }

            Users.insert {
                it[id] = 2
                it[name] = "Bob"
            }

            UserProfiles.insert {
                it[id] = 1
                it[userId] = 1
                it[lastName] = "Smith"
            }

            UserProfiles.insert {
                it[id] = 2
                it[userId] = 2
                it[lastName] = "Adams"
            }

            val query =
                query {
                    asc(
                        relation("userProfiles")
                            .map("last_name")
                            .max()
                    )

                    asc("name")
                }

            val compiled =
                compiler.compile(
                    query = query,
                    table = Users
                )

            assertEquals(
                2,
                compiled.orderBy.size
            )

            assertEquals(
                SortOrder.ASC_NULLS_FIRST,
                compiled.orderBy[0].second
            )

            assertEquals(
                SortOrder.ASC_NULLS_FIRST,
                compiled.orderBy[1].second
            )

            val result =
                Users
                    .selectAll()
                    .orderBy(
                        *compiled.orderBy.toTypedArray()
                    )
                    .map {
                        it[Users.name]
                    }

            assertEquals(
                listOf("Bob", "Alice"),
                result
            )
        }

}

