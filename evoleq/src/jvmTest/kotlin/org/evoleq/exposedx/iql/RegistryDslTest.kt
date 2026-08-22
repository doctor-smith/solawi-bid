package org.evoleq.exposedx.iql

import junit.framework.TestCase.assertTrue
import org.evoleq.exposedx.test.runSimpleH2Test
import org.evoleq.iql.data.FieldType
import org.evoleq.iql.data.RelationType
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.jodatime.datetime
import org.joda.time.DateTime
import kotlin.test.Test
import kotlin.test.assertEquals

class RegistryDslTest {

    object UsersTable : Table("users") {
        val id = integer("id")
        val username = varchar("username", 100)
        val age = integer("age").default(0)
    }

    object UserProfilesTable : Table("user_profiles") {
        val id = integer("id")
        val userId = integer("user_id")
        val firstName = varchar("first_name", 100)
    }

    object RolesTable : Table("roles") {
        val id = integer("id")
        val name = varchar("name", 100)
    }

    object UserRolesTable : Table("user_roles") {
        val userId = integer("user_id")
        val roleId = integer("role_id")
    }

    val tables = arrayOf(
        UsersTable,
        UserProfilesTable,
        RolesTable,
        UserRolesTable
    )


    @Test
    fun `registers entity and fields`() = runSimpleH2Test(*tables){
        val registry = registry {
            entity("user", UsersTable) {
                field(UsersTable.id)
                field(UsersTable.username)
            }
        }

        val entity = registry.getEntityOrThrow("user")

        assertEquals("users", entity.table)

        assertEquals(
            FieldType.INTEGER,
            entity.fields["id"]?.type
        )

        assertEquals(
            FieldType.STRING,
            entity.fields["username"]?.type
        )
    }

    @Test
    fun `registers one to many relation`() = runSimpleH2Test(*tables){
        val registry = registry {
            entity("user", UsersTable) {
                field(UsersTable.id)

                oneToMany("userProfiles", UserProfilesTable) {
                    UsersTable.id references UserProfilesTable.userId
                }
            }

            entity("userProfile", UserProfilesTable) {
                field(UserProfilesTable.userId)
                field(UserProfilesTable.firstName)
            }
        }

        val relation =
            registry
                .getEntityOrThrow("user")
                .relations["userProfiles"]
                ?: error("Relation not found")

        assertEquals(
            RelationType.ONE_TO_MANY,
            relation.type
        )

        assertEquals(
            "userProfile",
            relation.targetEntity
        )

        assertEquals(
            listOf("id"),
            relation.joinColumns
        )

        assertEquals(
            "user_id",
            relation.inverseJoinColumn
        )

        assertTrue(
            relation.inverseJoinColumns.isEmpty()
        )
    }

    @Test
    fun `registers many to one relation`() = runSimpleH2Test(*tables){
        val registry = registry {
            entity("UserProfile", UserProfilesTable) {

                manyToOne("user", UsersTable) {
                    UserProfilesTable.userId references UsersTable.id
                }
            }

            entity("User", UsersTable) {
                field(UsersTable.id)
            }
        }

        val relation =
            registry
                .getEntityOrThrow("UserProfile")
                .relations["user"]
                ?: error("Relation not found")

        assertEquals(
            RelationType.MANY_TO_ONE,
            relation.type
        )

        assertEquals(
            "User",
            relation.targetEntity
        )

        assertEquals(
            listOf("user_id"),
            relation.joinColumns
        )

        assertEquals(
            "id",
            relation.inverseJoinColumn
        )

        assertTrue(
            relation.inverseJoinColumns.isEmpty()
        )
    }

    @Test
    fun `registers many to many relation`() = runSimpleH2Test(*tables){
        val registry = registry {

            entity("user", UsersTable) {

                manyToMany(
                    "roles",
                    RolesTable,
                    UserRolesTable
                ) {
                    source(
                        UsersTable.id references UserRolesTable.userId
                    )

                    target(
                        RolesTable.id references UserRolesTable.roleId
                    )
                }
            }

            entity("role", RolesTable) {
                field(RolesTable.id)
                field(RolesTable.name)
            }
        }

        val relation =
            registry
                .getEntityOrThrow("user")
                .relations["roles"]
                ?: error("Relation not found")

        assertEquals(
            RelationType.MANY_TO_MANY,
            relation.type
        )

        assertEquals(
            "role",
            relation.targetEntity
        )

        assertEquals(
            listOf("id"),
            relation.joinColumns
        )

        val mapping =
            relation.mapping
                ?: error("Mapping not found")

        assertEquals(
            "user_roles",
            mapping.table
        )

        assertEquals(
            listOf("id"),
            mapping.sourceColumns
        )

        assertEquals(
            listOf("user_id"),
            mapping.mappingSourceColumns
        )

        assertEquals(
            listOf("role_id"),
            mapping.mappingTargetColumns
        )

        assertEquals(
            listOf("id"),
            mapping.targetColumns
        )
    }




    @Test
    fun `single column relation uses inverseJoinColumn not inverseJoinColumns`() {
        val registry = registry {
            entity("user", UsersTable) {
                oneToMany("profiles", UserProfilesTable) {
                    UsersTable.id references UserProfilesTable.userId
                }
            }
        }

        val relation =
            registry
                .getEntityOrThrow("user")
                .relations["profiles"]
                ?: error("Relation not found")

        assertEquals("user_id", relation.inverseJoinColumn)
        assertTrue(relation.inverseJoinColumns.isEmpty())
    }


    @Test
    fun `relation uses registered logical entity name for target table`() = runSimpleH2Test(*tables) {
        val registry = registry {
            entity("user", UsersTable) {
                oneToMany("profiles", UserProfilesTable) {
                    UsersTable.id references UserProfilesTable.userId
                }
            }

            entity("UserProfile", UserProfilesTable) {
                field(UserProfilesTable.firstName)
            }
        }

        val relation =
            registry
                .getEntityOrThrow("user")
                .relations["profiles"]
                ?: error("Relation not found")

        assertEquals("UserProfile", relation.targetEntity)
    }

    @Test
    fun `relation uses transformed logical entity name`() = runSimpleH2Test(*tables) {
        val registry = registry {
            entityNameStrategy = EntityNameStrategy.CAMEL_CASE

            entity("user", UsersTable) {
                oneToMany("profiles", UserProfilesTable) {
                    UsersTable.id references UserProfilesTable.userId
                }
            }

            entity("UserProfile", UserProfilesTable) {
                field(UserProfilesTable.firstName)
            }
        }

        val relation =
            registry
                .getEntityOrThrow("user")
                .relations["profiles"]
                ?: error("Relation not found")

        assertEquals("userProfile", relation.targetEntity)
    }

    @Test
    fun `relation falls back to physical table name when target entity is not registered`() {
        val registry = registry {
            entity("user", UsersTable) {
                oneToMany("profiles", UserProfilesTable) {
                    UsersTable.id references UserProfilesTable.userId
                }
            }
        }

        val relation =
            registry
                .getEntityOrThrow("user")
                .relations["profiles"]
                ?: error("Relation not found")

        assertEquals("user_profiles", relation.targetEntity)
    }

    @Test
    fun `many-to-one stores single source and target column`() {
        val registry = registry {
            entity("UserProfile", UserProfilesTable) {
                manyToOne("user", UsersTable) {
                    UserProfilesTable.userId references UsersTable.id
                }
            }
        }

        val relation =
            registry
                .getEntityOrThrow("UserProfile")
                .relations["user"]
                ?: error("Relation not found")

        assertEquals(RelationType.MANY_TO_ONE, relation.type)
        assertEquals("id", relation.inverseJoinColumn)
        assertTrue(relation.inverseJoinColumns.isEmpty())
        assertEquals(listOf("user_id"), relation.joinColumns)
    }

    @Test
    fun `relation resolves target entity by table`() = runSimpleH2Test(*tables) {
        val registry = registry {
            entity("User", UsersTable) {
                oneToMany("profiles", UserProfilesTable) {
                    UsersTable.id references UserProfilesTable.userId
                }
            }

            entity("PersonProfile", UserProfilesTable) {
                field(UserProfilesTable.firstName)
            }
        }

        val relation =
            registry
                .getEntityOrThrow("User")
                .relations["profiles"]
                ?: error("Relation not found")

        assertEquals("PersonProfile", relation.targetEntity)
    }

    @Test
    fun `relation resolution does not depend on entity declaration order`() = runSimpleH2Test(*tables) {
        val registry = registry {
            entity("User", UsersTable) {
                oneToMany("profiles", UserProfilesTable) {
                    UsersTable.id references UserProfilesTable.userId
                }
            }

            entity("UserProfile", UserProfilesTable) {
                field(UserProfilesTable.firstName)
            }
        }

        val relation =
            registry
                .getEntityOrThrow("User")
                .relations["profiles"]
                ?: error("Relation not found")

        assertEquals("UserProfile", relation.targetEntity)
    }

    @Test
    fun `relation resolution works when target entity is declared first`() = runSimpleH2Test(*tables) {
        val registry = registry {
            entity("UserProfile", UserProfilesTable) {
                field(UserProfilesTable.firstName)
            }

            entity("User", UsersTable) {
                oneToMany("profiles", UserProfilesTable) {
                    UsersTable.id references UserProfilesTable.userId
                }
            }
        }

        val relation =
            registry
                .getEntityOrThrow("User")
                .relations["profiles"]
                ?: error("Relation not found")

        assertEquals("UserProfile", relation.targetEntity)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `one-to-many rejects source column from wrong table`() {
        registry {
            entity("User", UsersTable) {
                oneToMany("profiles", UserProfilesTable) {
                    UserProfilesTable.userId references UserProfilesTable.id
                }
            }
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `one-to-many rejects target column from wrong table`() {
        registry {
            entity("User", UsersTable) {
                oneToMany("profiles", UserProfilesTable) {
                    UsersTable.id references UsersTable.id
                }
            }
        }
    }

    class TestStringColumnType : StringColumnType() {
        override fun sqlType(): String {
            return "TEST_STRING_SQL_TYPE"
        }
    }
    class TestIntegerColumnType : ColumnType() {
        override fun sqlType(): String {
            return "TEST_INTEGER_SQL_TYPE"
        }
    }
    @Test
    fun `fieldTypes registers custom column type`() {
        val registry = registry {
            fieldTypes(
                TestStringColumnType::class to FieldType.STRING
            )

            entity("User", UsersTable) {
                field(UsersTable.username)
            }
        }

        val field =
            registry
                .getEntityOrThrow("User")
                .fields["username"]
                ?: error("Field not found")

        assertEquals(FieldType.STRING, field.type)
    }

    @Test
    fun `fieldTypes registers multiple mappings`() {
        val registry = registry {
            fieldTypes(
                TestStringColumnType::class to FieldType.STRING,
                TestIntegerColumnType::class to FieldType.INTEGER
            )

            entity("User", UsersTable) {
                field(UsersTable.username)
                field(UsersTable.age)
            }
        }

        val entity =
            registry.getEntityOrThrow("User")

        assertEquals(
            FieldType.STRING,
            entity.fields["username"]?.type
        )

        assertEquals(
            FieldType.INTEGER,
            entity.fields["age"]?.type
        )
    }

    @Test
    fun `custom field type mappings do not replace defaults`() {
        val registry = registry {
            fieldTypes(
                TestStringColumnType::class to FieldType.STRING
            )

            entity("User", UsersTable) {
                field(UsersTable.username)
            }
        }

        val field =
            registry
                .getEntityOrThrow("User")
                .fields["username"]
                ?: error("Field not found")

        assertEquals(FieldType.STRING, field.type)
    }

    @Test(expected = IllegalStateException::class)
    fun `unknown column type is rejected`() {
        val table = object : Table("test") {
            val unsupported: Column<DateTime> = datetime("unsupported")
        }

        registry {
            entity("User", table) {
                field(table.unsupported)
            }
        }
    }

    @Test(expected = IllegalStateException::class)
    fun `unsupported binary column type is rejected`() {
        val table = object : Table("test") {
            val data =
                binary(
                    "data",
                    32
                )
        }

        registry {
            entity("User", table) {
                field(table.data)
            }
        }
    }

    @Test
    fun `EntityID column type resolves using wrapped column type`() {
        val registry = registry {
            entity("User", UsersTable) {
                field(UsersTable.id)
            }
        }

        val field =
            registry
                .getEntityOrThrow("User")
                .fields["id"]
                ?: error("Field not found")

        assertEquals(FieldType.INTEGER, field.type)
    }



    @Test(expected = IllegalArgumentException::class)
    fun `duplicate field type mapping is rejected`() {
        registry {
            fieldTypes(
                TestStringColumnType::class to FieldType.STRING,
                TestStringColumnType::class to FieldType.UUID
            )
        }
    }


    @Test(expected = IllegalArgumentException::class)
    fun `duplicate field type registration fails`() {
        registry {
            fieldTypes(
                TestStringColumnType::class to FieldType.STRING,
                TestStringColumnType::class to FieldType.UUID
            )
        }
    }

    @Test
    fun `explicit field type override replaces mapping`() {
        val registry = registry {


            overrideFieldTypes(
                VarCharColumnType::class to FieldType.UUID
            )

            entity("User", UsersTable) {
                field(UsersTable.username)
            }
        }

        assertEquals(
            FieldType.UUID,
            registry
                .getEntityOrThrow("User")
                .fields["username"]
                ?.type
        )
    }
}
