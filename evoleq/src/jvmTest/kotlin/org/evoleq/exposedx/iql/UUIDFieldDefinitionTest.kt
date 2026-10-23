package org.evoleq.exposedx.iql

import kotlinx.serialization.json.JsonPrimitive
import org.evoleq.exposedx.test.runSimpleH2Test
import org.evoleq.iql.data.*
import org.jetbrains.exposed.sql.Table
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.*

class UUIDIqlTest {

    // -------------------------------------------------------------------------
    // Translation
    // -------------------------------------------------------------------------

    @Test
    fun `UUID translates JSON to java UUID`() {

        val definition =
            UUIDFieldDefinition()

        val uuid =
            UUID.randomUUID()

        val value =
            definition.translate(
                JsonPrimitive(
                    uuid.toString()
                )
            )

        assertEquals(
            uuid,
            value
        )
    }

    // -------------------------------------------------------------------------
    // Registry
    // -------------------------------------------------------------------------

    object Entities : Table("entities") {

        val id =
            uuid("id")

        val parentId =
            uuid("parent_id")
    }

    @Test
    fun `registry resolves UUID definition`() {

        val registry =
            Registry().apply {
                registerFieldDefinition(
                    UUIDFieldDefinition()
                )
            }

        val definition =
            registry.lookup(
                FieldType.UUID
            )

        assertEquals(
            FieldType.UUID,
            definition.type
        )

        val uuid =
            UUID.randomUUID()

        assertEquals(
            uuid,
            definition.translate(
                JsonPrimitive(
                    uuid.toString()
                )
            )
        )
    }

    // -------------------------------------------------------------------------
    // IQL → Exposed
    // -------------------------------------------------------------------------

    @Test
    fun `uuid comparison is compiled`() = runSimpleH2Test(Entities){

        val registry =
            testRegistry()

        val filter =
            ComparisonFilter(
                field = SimpleFieldRef(
                    "Entity.parent_id"
                ),
                operator = Operator.EQ,
                value = JsonPrimitive(
                    UUID.randomUUID().toString()
                )
            )

        val expression =
            ExposedCompiler(registry)
                .compile(
                    filter,
                    Entities
                )

        assertTrue(
            expression.toString().isNotBlank()
        )
    }

    @Test
    fun `uuid IN expression is compiled`() = runSimpleH2Test(Entities){

        val registry =
            testRegistry()

        val filter =
            InFilter(
                field = SimpleFieldRef(
                    "Entity.parent_id"
                ),
                values = listOf(
                    JsonPrimitive(
                        UUID.randomUUID().toString()
                    ),
                    JsonPrimitive(
                        UUID.randomUUID().toString()
                    )
                )
            )

        val expression =
            ExposedCompiler(registry)
                .compile(
                    filter,
                    Entities
                )

        assertTrue(
            expression.toString().isNotBlank()
        )
    }

    // -------------------------------------------------------------------------
    // Test infrastructure
    // -------------------------------------------------------------------------

    private fun testRegistry(): Registry =
        Registry().apply {

            registerEntity(
                entity = EntityType(
                    name = "Entity",
                    table = "entities",
                    fields = mapOf(
                        "id" to FieldInfo(
                            name = "id",
                            type = FieldType.UUID
                        ),
                        "parent_id" to FieldInfo(
                            name = "parent_id",
                            type = FieldType.UUID
                        )
                    ),
                    relations = emptyMap()
                ),
                table = Entities
            )

            registerFieldDefinition(
                UUIDFieldDefinition()
            )
        }
}
