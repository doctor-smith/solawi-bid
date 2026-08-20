package org.evoleq.exposedx.iql


import kotlinx.serialization.json.JsonPrimitive
import org.evoleq.exposedx.test.runSimpleH2Test
import org.evoleq.iql.data.*
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@Suppress("LargeClass")
class IqlEdgeCaseTest {

    // -------------------------------------------------------------------------
    // Tables
    // -------------------------------------------------------------------------

    object Users : Table("users") {
        val tenantId = integer("tenant_id")
        val id = integer("id")
        val name = varchar("name", 100)
    }

    object Roles : Table("roles") {
        val id = integer("id")
        val name = varchar("name", 100)
    }

    object UserRoles : Table("user_roles") {
        val userTenantId = integer("user_tenant_id")
        val userId = integer("user_id")
        val roleId = integer("role_id")
    }

    object Orders : Table("orders") {
        val id = integer("id")
        val userTenantId = integer("user_tenant_id")
        val userId = integer("user_id")
        val productId = integer("product_id")
    }

    object Products : Table("products") {
        val id = integer("id")
        val name = varchar("name", 100)
    }

    // -------------------------------------------------------------------------
    // Registry
    // -------------------------------------------------------------------------

    private fun registry(): Registry {

        val registry = Registry(FieldNameStrategy.SNAKE_CASE)

        registry.registerEntity(
            EntityType(
                name = "User",
                table = "users",
                fields = mapOf(
                    "tenantId" to FieldInfo(
                        name = "tenant_id",
                        type = FieldType.INTEGER
                    ),
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
                    "roles" to RelationInfo(
                        name = "roles",
                        type = RelationType.MANY_TO_MANY,
                        targetEntity = "Role",
                        joinColumns = listOf("tenant_id", "id"),
                        inverseJoinColumn = "id",
                        mapping = MappingInfo(
                            table = "user_roles",
                            sourceColumns =
                                listOf("tenant_id", "id"),
                            mappingSourceColumns =
                                listOf(
                                    "user_tenant_id",
                                    "user_id"
                                ),
                            mappingTargetColumns =
                                listOf("role_id"),
                            targetColumns =
                                listOf("id")
                        )
                    ),
                    "orders" to RelationInfo(
                        name = "orders",
                        type = RelationType.ONE_TO_MANY,
                        targetEntity = "Order",

                        joinColumns = listOf(
                            "tenant_id",
                            "id"
                        ),

                        inverseJoinColumn = null,

                        inverseJoinColumns = listOf(
                            "user_tenant_id",
                            "user_id"
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

        registry.registerEntity(
            EntityType(
                name = "Order",
                table = "orders",
                fields = mapOf(
                    "id" to FieldInfo(
                        name = "id",
                        type = FieldType.INTEGER
                    ),
                    "userTenantId" to FieldInfo(
                        name = "user_tenant_id",
                        type = FieldType.INTEGER
                    ),
                    "userId" to FieldInfo(
                        name = "user_id",
                        type = FieldType.INTEGER
                    ),
                    "productId" to FieldInfo(
                        name = "product_id",
                        type = FieldType.INTEGER
                    )
                ),
                relations = mapOf(
                    "product" to RelationInfo(
                        name = "product",
                        type = RelationType.MANY_TO_ONE,
                        targetEntity = "Product",
                        joinColumns = listOf("product_id"),
                        inverseJoinColumn = "id"
                    )
                )
            ),
            Orders
        )

        registry.registerEntity(
            EntityType(
                name = "Product",
                table = "products",
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
            Products
        )

        registry.registerMappingTable(
            "user_roles",
            UserRoles
        )

        return registry
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private fun eq(
        path: String,
        value: String
    ): ComparisonFilter =
        ComparisonFilter(
            field = SimpleFieldRef(path),
            operator = Operator.EQ,
            value = JsonPrimitive(value)
        )

    @Suppress("UnusedPrivateMember")
    private fun eq(
        path: String,
        value: Int
    ): ComparisonFilter =
        ComparisonFilter(
            field = SimpleFieldRef(path),
            operator = Operator.EQ,
            value = JsonPrimitive(value)
        )

    private fun any(
        entity: String,
        relation: String,
        filter: Filter
    ): QuantifierFilter =
        QuantifierFilter(
            quantifier = Quantifier.ANY,
            relation = RelationRef(
                entity = entity,
                relation = relation
            ),
            filter = filter
        )

    // -------------------------------------------------------------------------
    // 1. Empty logical expression
    // -------------------------------------------------------------------------

    @Test
    fun `empty AND is rejected by compiler`() {

        val filter =
            AndFilter(emptyList())

        val compiler =
            ExposedCompiler(registry())

        assertFailsWith<IllegalArgumentException> {
            compiler.compile(
                filter,
                Users
            )
        }
    }

    // -------------------------------------------------------------------------
    // 2. ANY with no related rows
    // -------------------------------------------------------------------------

    @Test
    fun `ANY returns no users when there are no related rows`() =
        runSimpleH2Test(
            Users,
            Roles,
            UserRoles
        ) {

            Users.insert {
                it[tenantId] = 1
                it[id] = 1
                it[name] = "Alice"
            }

            Roles.insert {
                it[id] = 10
                it[name] = "admin"
            }

            val filter =
                any(
                    "User",
                    "roles",
                    eq("Role.name", "admin")
                )

            val result =
                Users
                    .selectAll()
                    .where {
                        ExposedCompiler(registry())
                            .compile(filter, Users)
                    }
                    .map {
                        it[Users.id]
                    }

            assertEquals(
                emptyList(),
                result
            )
        }

    // -------------------------------------------------------------------------
    // 3. NONE with no related rows
    // -------------------------------------------------------------------------

    @Test
    fun `NONE returns users when there are no related rows`() =
        runSimpleH2Test(
            Users,
            Roles,
            UserRoles
        ) {

            Users.insert {
                it[tenantId] = 1
                it[id] = 1
                it[name] = "Alice"
            }

            val filter =
                QuantifierFilter(
                    quantifier = Quantifier.NONE,
                    relation = RelationRef(
                        entity = "User",
                        relation = "roles"
                    ),
                    filter =
                        eq("Role.name", "admin")
                )

            val result =
                Users
                    .selectAll()
                    .where {
                        ExposedCompiler(registry())
                            .compile(filter, Users)
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
    // 4. ALL with no related rows
    // -------------------------------------------------------------------------

    @Test
    fun `ALL over empty relation is true`() =
        runSimpleH2Test(
            Users,
            Roles,
            UserRoles
        ) {

            Users.insert {
                it[tenantId] = 1
                it[id] = 1
                it[name] = "Alice"
            }

            val filter =
                QuantifierFilter(
                    quantifier = Quantifier.ALL,
                    relation = RelationRef(
                        entity = "User",
                        relation = "roles"
                    ),
                    filter =
                        eq("Role.name", "admin")
                )

            val result =
                Users
                    .selectAll()
                    .where {
                        ExposedCompiler(registry())
                            .compile(filter, Users)
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
    // 5. MANY-TO-MANY
    // -------------------------------------------------------------------------

    @Test
    fun `ANY many-to-many finds matching users`() =
        runSimpleH2Test(
            Users,
            Roles,
            UserRoles
        ) {

            Users.insert {
                it[tenantId] = 1
                it[id] = 1
                it[name] = "Alice"
            }

            Users.insert {
                it[tenantId] = 1
                it[id] = 2
                it[name] = "Bob"
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
                it[userTenantId] = 1
                it[userId] = 1
                it[roleId] = 10
            }

            UserRoles.insert {
                it[userTenantId] = 1
                it[userId] = 2
                it[roleId] = 20
            }

            val filter =
                any(
                    "User",
                    "roles",
                    eq("Role.name", "admin")
                )

            val result =
                Users
                    .selectAll()
                    .where {
                        ExposedCompiler(registry())
                            .compile(filter, Users)
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
    // 6. Duplicate mapping rows must not duplicate source rows
    // -------------------------------------------------------------------------

    @Test
    fun `ANY does not duplicate users when multiple mappings match`() =
        runSimpleH2Test(
            Users,
            Roles,
            UserRoles
        ) {

            Users.insert {
                it[tenantId] = 1
                it[id] = 1
                it[name] = "Alice"
            }

            Roles.insert {
                it[id] = 10
                it[name] = "admin"
            }

            UserRoles.insert {
                it[userTenantId] = 1
                it[userId] = 1
                it[roleId] = 10
            }

            UserRoles.insert {
                it[userTenantId] = 1
                it[userId] = 1
                it[roleId] = 10
            }

            val filter =
                any(
                    "User",
                    "roles",
                    eq("Role.name", "admin")
                )

            val result =
                Users
                    .selectAll()
                    .where {
                        ExposedCompiler(registry())
                            .compile(filter, Users)
                    }
                    .map {
                        it[Users.id]
                    }
                    .distinct()

            assertEquals(
                listOf(1),
                result
            )
        }

    // -------------------------------------------------------------------------
    // 7. Composite mapping key
    // -------------------------------------------------------------------------

    @Test
    fun `composite mapping keys keep tenants isolated`() =
        runSimpleH2Test(
            Users,
            Roles,
            UserRoles
        ) {

            Users.insert {
                it[tenantId] = 1
                it[id] = 10
                it[name] = "Tenant1"
            }

            Users.insert {
                it[tenantId] = 2
                it[id] = 10
                it[name] = "Tenant2"
            }

            Roles.insert {
                it[id] = 100
                it[name] = "admin"
            }

            UserRoles.insert {
                it[userTenantId] = 1
                it[userId] = 10
                it[roleId] = 100
            }

            val filter =
                any(
                    "User",
                    "roles",
                    eq("Role.name", "admin")
                )

            val result =
                Users
                    .selectAll()
                    .where {
                        ExposedCompiler(registry())
                            .compile(filter, Users)
                    }
                    .map {
                        it[Users.tenantId] to
                                it[Users.id]
                    }

            assertEquals(
                listOf(1 to 10),
                result
            )
        }

    // -------------------------------------------------------------------------
    // 8. Nested ANY -> ANY
    // -------------------------------------------------------------------------

    @Test
    fun `nested ANY is correctly correlated`() =
        runSimpleH2Test(
            Users,
            Orders,
            Products
        ) {

            Users.insert {
                it[tenantId] = 1
                it[id] = 1
                it[name] = "Alice"
            }

            Users.insert {
                it[tenantId] = 1
                it[id] = 2
                it[name] = "Bob"
            }

            Products.insert {
                it[id] = 10
                it[name] = "Coffee"
            }

            Products.insert {
                it[id] = 20
                it[name] = "Tea"
            }

            Orders.insert {
                it[id] = 100
                it[userTenantId] = 1
                it[userId] = 1
                it[productId] = 10
            }

            Orders.insert {
                it[id] = 200
                it[userTenantId] = 1
                it[userId] = 2
                it[productId] = 20
            }

            val filter =
                QuantifierFilter(
                    quantifier = Quantifier.ANY,
                    relation = RelationRef(
                        entity = "User",
                        relation = "orders"
                    ),
                    filter =
                        QuantifierFilter(
                            quantifier = Quantifier.ANY,
                            relation = RelationRef(
                                entity = "Order",
                                relation = "product"
                            ),
                            filter =
                                eq(
                                    "Product.name",
                                    "Coffee"
                                )
                        )
                )

            val result =
                Users
                    .selectAll()
                    .where {
                        ExposedCompiler(registry())
                            .compile(filter, Users)
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
    // 9. Empty IN
    // -------------------------------------------------------------------------

    @Test
    fun `empty IN matches no rows`() =
        runSimpleH2Test(
            Users
        ) {

            Users.insert {
                it[tenantId] = 1
                it[id] = 1
                it[name] = "Alice"
            }

            val filter =
                InFilter(
                    field =
                        SimpleFieldRef(
                            "User.id"
                        ),
                    values = emptyList()
                )

            val result =
                Users
                    .selectAll()
                    .where {
                        ExposedCompiler(registry())
                            .compile(filter, Users)
                    }
                    .map {
                        it[Users.id]
                    }

            assertEquals(
                emptyList(),
                result
            )
        }

    // -------------------------------------------------------------------------
    // 10. Invalid value type
    // -------------------------------------------------------------------------

    @Test
    fun `invalid integer value is rejected`() =
        runSimpleH2Test(
            Users
        ) {

            val filter =
                ComparisonFilter(
                    field =
                        SimpleFieldRef(
                            "User.id"
                        ),
                    operator = Operator.EQ,
                    value = JsonPrimitive("not-an-int")
                )

            assertFailsWith<Exception> {
                ExposedCompiler(registry())
                    .compile(
                        filter,
                        Users
                    )
            }
        }


    @Test
    fun `unknown field is rejected by compiler`() =
        runSimpleH2Test(
            Users
        ) {

            val filter =
                eq(
                    "User.doesNotExist",
                    "Alice"
                )

            assertFailsWith<IllegalStateException> {
                ExposedCompiler(registry())
                    .compile(
                        filter,
                        Users
                    )
            }
        }


    @Test
    fun `field without entity is rejected by compiler`() =
        runSimpleH2Test(Users) {

            val filter =
                eq(
                    "name",
                    "Alice"
                )

            assertFailsWith<IllegalArgumentException> {
                ExposedCompiler(registry())
                    .compile(
                        filter,
                        Users
                    )
            }
        }
}
