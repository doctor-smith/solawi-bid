package org.solyton.solawi.bid.module.user.iql

import kotlinx.serialization.json.JsonPrimitive
import org.evoleq.exposedx.iql.ExposedCompiler
import org.evoleq.exposedx.test.runSimpleH2Test
import org.evoleq.iql.data.ComparisonFilter
import org.evoleq.iql.data.Operator
import org.evoleq.iql.data.RelationType
import org.evoleq.iql.data.SimpleFieldRef
import org.jetbrains.exposed.sql.selectAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.DbFunctional
import org.solyton.solawi.bid.Unit
import org.solyton.solawi.bid.module.user.schema.UsersTable
import kotlin.test.assertEquals

class UserModuleRegistryTest {
    @Unit@Test
    fun `user module registry is correctly configured`() {
        val registry = userModuleRegistry

        val user =
            registry.getEntityOrThrow("user")

        assertEquals(

            "users",
            user.table
        )

        Assertions.assertTrue(user.fields.containsKey("username"))

        Assertions.assertTrue(user.fields.containsKey("password"))

        val profiles =
            user.relations["userProfiles"]
                ?: error("userProfiles relation not found")

        assertEquals(
            RelationType.ONE_TO_MANY,
            profiles.type
        )

        assertEquals(
            "userProfile",
            profiles.targetEntity
        )

        assertEquals(
            listOf("id"),
            profiles.joinColumns
        )

        assertEquals(
            "user_id",
            profiles.inverseJoinColumn
        )

        Assertions.assertTrue(profiles.inverseJoinColumns.isEmpty())
    }

    @Unit@Test
    fun `user profile relations are correctly configured`() {
        val registry = userModuleRegistry

        val profile =
            registry.getEntityOrThrow("userProfile")

        val user =
            profile.relations["user"]
                ?: error("user relation not found")

        assertEquals(
            RelationType.MANY_TO_ONE,
            user.type
        )

        assertEquals(
            "user",
            user.targetEntity
        )

        assertEquals(
            listOf("user_id"),
            user.joinColumns
        )

        assertEquals(
            "id",
            user.inverseJoinColumn
        )

        val addresses =
            profile.relations["addresses"]
                ?: error("addresses relation not found")

        assertEquals(
            RelationType.ONE_TO_MANY,
            addresses.type
        )

        assertEquals(
            "address",
            addresses.targetEntity
        )
    }


    val tables = arrayOf(
        UsersTable
    )

    @DbFunctional@Test
    fun `user module registry compiles user profile query`() =
        runSimpleH2Test(*tables) {

            val compiler =
                ExposedCompiler(userModuleRegistry)

            val filter =
                ComparisonFilter(
                    field = SimpleFieldRef("user.username"),
                    operator = Operator.EQ,
                    value = JsonPrimitive("alice")
                )
                    val expression =
                compiler.compile(
                    filter,
                    UsersTable
                )

            UsersTable
                .selectAll()
                .where { expression }
                .toList()
        }
}
