package org.evoleq.exposedx.iql


import org.evoleq.exposedx.test.runSimpleH2Test
import org.evoleq.iql.data.EntityType
import org.evoleq.iql.data.FieldInfo
import org.evoleq.iql.data.FieldType
import org.evoleq.iql.dsl.query
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExposedQueryCompilerTest {

    object Users : Table("users") {

        val id =
            integer("id")

        val name =
            varchar("name", 100)

        val active =
            bool("active")

        val createdAt =
            integer("created_at")
    }

    private val registry =
        Registry().apply {

            registerEntity(
                EntityType(
                    name = "User",
                    table = "users",
                    fields = mapOf(
                        "id" to FieldInfo(
                            name = "id",
                            type = FieldType.INTEGER
                        ),
                        "name" to FieldInfo(
                            name = "name",
                            type = FieldType.STRING
                        ),
                        "active" to FieldInfo(
                            name = "active",
                            type = FieldType.BOOLEAN
                        ),
                        "created_at" to FieldInfo(
                            name = "created_at",
                            type = FieldType.INTEGER
                        )
                    ),
                    relations = emptyMap()
                ),
                Users
            )
        }

    private val compiler =
        ExposedQueryCompiler(registry)

    @Test
    fun `ASC sort is compiled`() {

        val query =
            query {
                asc("User.name")
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
            SortOrder.ASC,
            compiled.orderBy.first().second
        )
    }

    @Test
    fun `DESC sort is compiled`() {

        val query =
            query {
                desc("User.created_at")
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
            SortOrder.DESC,
            compiled.orderBy.first().second
        )
    }

    @Test
    fun `multiple sort expressions are preserved`() {

        val query =
            query {
                asc("User.name")
                desc("User.created_at")
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
            SortOrder.ASC,
            compiled.orderBy[0].second
        )

        assertEquals(
            SortOrder.DESC,
            compiled.orderBy[1].second
        )
    }

    @Test
    fun `filter and sort are compiled together`() {

        val query =
            query {
                where {
                    p("User.active") eq true
                }

                desc("User.created_at")
            }

        val compiled =
            compiler.compile(
                query = query,
                table = Users
            )

        assertTrue(
            compiled.predicate != null
        )

        assertEquals(
            1,
            compiled.orderBy.size
        )

        assertEquals(
            SortOrder.DESC,
            compiled.orderBy.first().second
        )
    }

    @Test
    fun `no sort produces empty order by`() {

        val query =
            query {
                where {
                    p("User.active") eq true
                }
            }

        val compiled =
            compiler.compile(
                query = query,
                table = Users
            )

        assertTrue(
            compiled.orderBy.isEmpty()
        )
    }

    @Test
    fun `invalid sort field is rejected`() {

        val query =
            query {
                desc("User.doesNotExist")
            }

        kotlin.test.assertFailsWith<IllegalStateException> {
            compiler.compile(
                query = query,
                table = Users
            )
        }
    }


    @Test
    fun `DESC sorting returns newest users first`() = runSimpleH2Test(Users) {

            Users.insert {
                it[id] = 1
                it[name] = "Alice"
                it[active] = true
                it[createdAt] = 100
            }

            Users.insert {
                it[id] = 2
                it[name] = "Bob"
                it[active] = true
                it[createdAt] = 300
            }

            Users.insert {
                it[id] = 3
                it[name] = "Charlie"
                it[active] = true
                it[createdAt] = 200
            }

            val query =
                query {
                    desc("User.created_at")
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
                        it[Users.id]
                    }

            assertEquals(
                listOf(2, 3, 1),
                result
            )
        }
}
