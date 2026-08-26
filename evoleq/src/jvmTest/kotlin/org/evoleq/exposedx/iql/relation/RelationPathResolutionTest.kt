package org.evoleq.exposedx.iql.relation


import kotlinx.serialization.json.JsonPrimitive
import org.evoleq.exposedx.iql.ExposedCompiler
import org.evoleq.exposedx.iql.Registry
import org.evoleq.exposedx.iql.references
import org.evoleq.exposedx.iql.registry
import org.evoleq.iql.data.*
import org.evoleq.iql.dsl.query
import org.jetbrains.exposed.sql.Table
import kotlin.test.*

/**
 * Test domain:
 *
 * Company
 *   ├── departments          1:N
 *   │     ├── manager         N:1 -> Employee
 *   │     └── projects        1:N
 *   │           └── members   M:N -> Employee
 *   │                 └── department N:1
 *   │
 *   └── headquarters          N:1 -> Address
 *                               └── country N:1 -> Country
 *
 *
 * Employee
 *    │
 *    │ manyToOne
 *    ▼
 * Department ── manyToOne ──► Company ── manyToOne ──► Country
 *    │                          │
 *    │ manyToOne                │ manyToMany
 *    ▼                          ▼
 * Employee                    Project ── manyToOne ──► Customer
 *
 * Department ── manyToMany ──► Project
 * The tests intentionally use normal Int IDs rather than EntityID/UUID.
 */

// -----------------------------------------------------------------------------
// Tables
// -----------------------------------------------------------------------------

object Companies : Table("companies") {
    val id = integer("id")
    val name = varchar("name", 100)
}

object Departments : Table("departments") {
    val id = integer("id")
    val name = varchar("name", 100)
    val companyId = integer("company_id")
    val managerId = integer("manager_id")
}

object Employees : Table("employees") {
    val id = integer("id")
    val name = varchar("name", 100)
    val departmentId = integer("department_id")
}

object Projects : Table("projects") {
    val id = integer("id")
    val name = varchar("name", 100)
    val departmentId = integer("department_id")
}

object ProjectMembers : Table("project_members") {
    val projectId = integer("project_id")
    val employeeId = integer("employee_id")
}

object Addresses : Table("addresses") {
    val id = integer("id")
    val street = varchar("street", 100)
    val countryId = integer("country_id")
}

object Countries : Table("countries") {
    val id = integer("id")
    val name = varchar("name", 100)
}

// -----------------------------------------------------------------------------
// Registry
// -----------------------------------------------------------------------------

private fun testRegistry(): Registry =
    registry {

        entity("company", Companies) {
            field(Companies.name)

            oneToMany("departments", Departments) {
                Companies.id references Departments.companyId
            }

            manyToOne("headquarters", Addresses) {
                Companies.id references Addresses.id
            }
        }

        entity("department", Departments) {
            field(Departments.name)

            manyToOne("company", Companies) {
                Departments.companyId references Companies.id
            }

            manyToOne("manager", Employees) {
                Departments.managerId references Employees.id
            }

            oneToMany("projects", Projects) {
                Departments.id references Projects.departmentId
            }

            oneToMany("employees", Employees) {
                Departments.id references Employees.departmentId
            }
        }



        entity("project", Projects) {
            field(Projects.name)

            manyToOne("department", Departments) {
                Projects.departmentId references Departments.id
            }

            manyToMany("members", Employees, ProjectMembers) {

                source(Projects.id references ProjectMembers.projectId)
                target(Employees.id references ProjectMembers.employeeId)
                /*
                sourceColumns = listOf(Projects.id.name)
                targetColumns = listOf(Employees.id.name)
                mappingSourceColumns = listOf(ProjectMembers.projectId.name)
                mappingTargetColumns = listOf(ProjectMembers.employeeId.name)

                 */
            }
        }

        entity("employee", Employees) {
            field(Employees.name)

            manyToOne("department", Departments) {
                Employees.departmentId references Departments.id
            }
        }

        entity("address", Addresses) {
            field(Addresses.street)

            manyToOne("country", Countries) {
                Addresses.countryId references Countries.id
            }
        }

        entity("country", Countries) {
            field(Countries.name)
        }
    }

private fun eq(
    path: String,
    value: String
): ComparisonFilter =
    ComparisonFilter(
        field = SimpleFieldRef(path),
        operator = Operator.EQ,
        value = JsonPrimitive(value)
    )

// -----------------------------------------------------------------------------
// Path resolution tests
// -----------------------------------------------------------------------------

class RelationPathResolutionTest {

    private val registry = testRegistry()

    /**RelationPathResolutionTest
     * company.departments.manager.name
     *
     * company
     *   -> departments -> department
     *   -> manager     -> employee
     *   -> name
     */
    @Test
    fun `resolves 1N followed by N1`() {

        val compiler =
            ExposedCompiler(registry)

        val filter =
            query("company") {
                where {
                    p("departments.manager.name") eq "Alice"
                }
            }.filter!!

        val resolved =
            compiler.resolveForTest(
                filter = filter,
                currentEntity = registry.getEntityOrThrow("company")
            )

        assertEquals("employee", resolved.entity)
        assertEquals("name", resolved.field)

        assertEquals(
            listOf(
                "company -> departments -> department",
                "department -> manager -> employee"
            ),
            resolved.relationPath.map {
                "${it.sourceEntity.name} -> " +
                        "${it.relation.name} -> " +
                        it.targetEntity.name
            }
        )
    }

    /**
     * company.departments.projects.name
     *
     * 1:N -> 1:N
     */
    @Test
    fun `resolves two consecutive one to many relations`() {

        val compiler =
            ExposedCompiler(registry)

        val filter =
            query("company") {
                where {
                    p("departments.projects.name") eq "Platform"
                }
            }.filter!!

        val resolved =
            compiler.resolveForTest(
                filter,
                registry.getEntityOrThrow("company")
            )

        assertEquals("project", resolved.entity)
        assertEquals("name", resolved.field)

        assertEquals(
            listOf(
                "company -> departments -> department",
                "department -> projects -> project"
            ),
            resolved.relationPath.map {
                "${it.sourceEntity.name} -> " +
                        "${it.relation.name} -> " +
                        it.targetEntity.name
            }
        )
    }

    /**
     * company.departments.projects.members.name
     *
     * 1:N -> 1:N -> M:N
     */
    @Test
    fun `resolves mixed one to many and many to many path`() {

        val compiler =
            ExposedCompiler(registry)

        val filter =
            query("company") {
                where {
                    p("departments.projects.members.name") eq "Alice"
                }
            }.filter!!

        val resolved =
            compiler.resolveForTest(
                filter,
                registry.getEntityOrThrow("company")
            )

        assertEquals("employee", resolved.entity)
        assertEquals("name", resolved.field)

        assertEquals(
            listOf(
                "company -> departments -> department",
                "department -> projects -> project",
                "project -> members -> employee"
            ),
            resolved.relationPath.map {
                "${it.sourceEntity.name} -> " +
                        "${it.relation.name} -> " +
                        it.targetEntity.name
            }
        )
    }

    /**
     * employee.department.company.name
     *
     * N:1 -> N:1
     */
    @Test
    fun `resolves two many to one relations`() {

        val compiler =
            ExposedCompiler(registry)

        val filter =
            query("employee") {
                where {
                    p("department.company.name") eq "Acme"
                }
            }.filter!!

        val resolved =
            compiler.resolveForTest(
                filter,
                registry.getEntityOrThrow("employee")
            )

        assertEquals("company", resolved.entity)
        assertEquals("name", resolved.field)

        assertEquals(
            listOf(
                "employee -> department -> department",
                "department -> company -> company"
            ),
            resolved.relationPath.map {
                "${it.sourceEntity.name} -> " +
                        "${it.relation.name} -> " +
                        it.targetEntity.name
            }
        )
    }

    /**
     * company.headquarters.country.name
     *
     * N:1 -> N:1
     */
    @Test
    fun `resolves relation path through address and country`() {

        val compiler =
            ExposedCompiler(registry)

        val filter =
            query("company") {
                where {
                    p("headquarters.country.name") eq "Germany"
                }
            }.filter!!

        val resolved =
            compiler.resolveForTest(
                filter,
                registry.getEntityOrThrow("company")
            )

        assertEquals("country", resolved.entity)
        assertEquals("name", resolved.field)

        assertEquals(
            listOf(
                "company -> headquarters -> address",
                "address -> country -> country"
            ),
            resolved.relationPath.map {
                "${it.sourceEntity.name} -> " +
                        "${it.relation.name} -> " +
                        it.targetEntity.name
            }
        )
    }

    // -------------------------------------------------------------------------
    // Explicit entity prefix
    // -------------------------------------------------------------------------

    @Test
    fun `explicit entity prefix starts relation resolution from that entity`() {

        val compiler =
            ExposedCompiler(registry)

        val filter =
            eq(
                "department.manager.name",
                "Alice"
            )


        val resolved =
            compiler.resolveForTest(
                filter,
                registry.getEntityOrThrow("department")
            )

        assertEquals("employee", resolved.entity)
        assertEquals("name", resolved.field)

        assertEquals(
            listOf(
                "department -> manager -> employee"
            ),
            resolved.relationPath.map {
                "${it.sourceEntity.name} -> " +
                        "${it.relation.name} -> " +
                        it.targetEntity.name
            }
        )
    }

    // -------------------------------------------------------------------------
    // Same relation name on different entities
    // -------------------------------------------------------------------------

    @Test
    fun `each relation is resolved against the current target entity`() {

        val compiler =
            ExposedCompiler(registry)

        val filter =
            query("employee") {
                where {
                    p("department.manager.name") eq "Alice"
                }
            }.filter!!

        val resolved =
            compiler.resolveForTest(
                filter,
                registry.getEntityOrThrow("employee")
            )

        assertEquals(
            listOf(
                "employee -> department -> department",
                "department -> manager -> employee"
            ),
            resolved.relationPath.map {
                "${it.sourceEntity.name} -> " +
                        "${it.relation.name} -> " +
                        it.targetEntity.name
            }
        )
    }

    // -------------------------------------------------------------------------
    // Invalid paths
    // -------------------------------------------------------------------------

    @Test
    fun `unknown relation is rejected`() {

        val compiler =
            ExposedCompiler(registry)

        val filter =
            query("company") {
                where {
                    p("departments.unknown.name") eq "Alice"
                }
            }.filter!!

        assertFailsWith<IllegalArgumentException> {
            compiler.resolveForTest(
                filter,
                registry.getEntityOrThrow("company")
            )
        }
    }

    @Test
    fun `field on wrong target entity is rejected`() {

        val compiler =
            ExposedCompiler(registry)

        val filter =
            query("company") {
                where {
                    p("departments.projects.street") eq "Main Street"
                }
            }.filter!!

        assertFailsWith<IllegalArgumentException> {
            compiler.resolveForTest(
                filter,
                registry.getEntityOrThrow("company")
            )
        }
    }


    // -------------------------------------------------------------------------
    // Heavy tests
    // -------------------------------------------------------------------------
    @Test
    fun `resolves company headquarters country`() {

        val compiler =
            ExposedCompiler(registry)

        val filter =
            query("company") {
                where {
                    p("headquarters.country.name") eq "Germany"
                }
            }.filter!!

        val resolved =
            compiler.resolveForTest(
                filter,
                registry.getEntityOrThrow("company")
            )

        assertEquals("country", resolved.entity)
        assertEquals("name", resolved.field)

        assertEquals(
            listOf(
                "company -> headquarters -> address",
                "address -> country -> country"
            ),
            resolved.relationPath.map {
                "${it.sourceEntity.name} -> " +
                        "${it.relation.name} -> " +
                        it.targetEntity.name
            }
        )
    }

    @Test
    fun `resolves department company headquarters country`() {

        val compiler =
            ExposedCompiler(registry)

        val filter =
            query("department") {
                where {
                    p("company.headquarters.country.name") eq "Germany"
                }
            }.filter!!

        val resolved =
            compiler.resolveForTest(
                filter,
                registry.getEntityOrThrow("department")
            )

        assertEquals("country", resolved.entity)
        assertEquals("name", resolved.field)

        assertEquals(
            listOf(
                "department -> company -> company",
                "company -> headquarters -> address",
                "address -> country -> country"
            ),
            resolved.relationPath.map {
                "${it.sourceEntity.name} -> " +
                        "${it.relation.name} -> " +
                        it.targetEntity.name
            }
        )
    }

    @Test
    fun `resolves employee department company headquarters country`() {

        val compiler =
            ExposedCompiler(registry)

        val filter =
            query("employee") {
                where {
                    p("department.company.headquarters.country.name") eq "Germany"
                }
            }.filter!!

        val resolved =
            compiler.resolveForTest(
                filter,
                registry.getEntityOrThrow("employee")
            )

        assertEquals("country", resolved.entity)
        assertEquals("name", resolved.field)

        assertEquals(
            listOf(
                "employee -> department -> department",
                "department -> company -> company",
                "company -> headquarters -> address",
                "address -> country -> country"
            ),
            resolved.relationPath.map {
                "${it.sourceEntity.name} -> " +
                        "${it.relation.name} -> " +
                        it.targetEntity.name
            }
        )
    }

    // -------------------------------------------------------------------------
    // Relation path used in AND
    // -------------------------------------------------------------------------

    @Test
    fun `resolves multiple independent deep relation paths in and`() {
        val filter =
            query("employee") {
                where {
                    p("department.company.name") eq "Acme"
                    p("department.manager.name") eq "Bob"
                }
            }.filter!!

        assertNotNull(filter)

        // The important part here is that both paths can coexist
        // and are resolved relative to the same source entity.
        val predicates =
            (filter as AndFilter).filters

        assertEquals(2, predicates.size)
    }

    // -------------------------------------------------------------------------
    // Relation path in OR
    // -------------------------------------------------------------------------

    @Test
    fun `resolves deep relation paths inside or`() {
        val filter =
            query("employee") {
                where {
                    or {
                        p("department.company.name") eq "Acme"
                        p("department.manager.name") eq "Alice"
                        p("department.location.country.name") eq "Germany"
                    }
                }
            }.filter!!

        assertNotNull(filter)

        val or =
            filter as OrFilter

        assertEquals(3, or.filters.size)
    }

    // -------------------------------------------------------------------------
    // Relation path inside NOT
    // -------------------------------------------------------------------------

    @Test
    fun `resolves deep relation path inside not`() {
        val filter =
            query("employee") {
                where {
                    not {
                        p("department.company.country.name") eq "Germany"
                    }
                }
            }.filter!!

        assertTrue(filter is NotFilter)
    }

    // -------------------------------------------------------------------------
    // Mixed relation paths in one query
    // -------------------------------------------------------------------------

    @Test
    fun `resolves mixed deep paths in one realistic query`() {
        val filter =
            query("employee") {
                where {
                    p("department.company.name") eq "Acme"
                    p("department.manager.name") eq "Alice"
                    p("department.company.country.name") eq "Germany"
                    p("department.projects.customer.name") eq "ACME Customer"
                }
            }.filter!!

        assertTrue(filter is AndFilter)

        assertEquals(
            4,
            (filter as AndFilter).filters.size
        )
    }


}

internal fun ExposedCompiler.resolveForTest(
    filter: Filter,
    currentEntity: EntityType?
): ExposedCompiler.ResolvedField {

    require(filter is ComparisonFilter) {
        "resolveForTest currently expects ComparisonFilter"
    }

    return resolveField(
        field = filter.field,
        currentEntity = currentEntity
    )
}
internal data class ResolvedRelationPath(
    val entity: String,
    val field: String,
    val path: List<Triple<String, String, String>>
)

internal fun ExposedCompiler.resolveRelationPathForTest(
    field: FieldRef,
    currentEntity: EntityType
): ResolvedRelationPath {

    val resolved =
        resolveField(field, currentEntity)

    return ResolvedRelationPath(
        entity = resolved.entity,
        field = resolved.field,
        path =
            resolved.relationPath.map {
                Triple(
                    it.sourceEntity.name,
                    it.relation.name,
                    it.targetEntity.name
                )
            }
    )
}
