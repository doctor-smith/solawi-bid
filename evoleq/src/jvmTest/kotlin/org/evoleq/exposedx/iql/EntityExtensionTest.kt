package org.evoleq.exposedx.iql

import junit.framework.TestCase.assertTrue
import org.evoleq.iql.data.RelationType
import org.evoleq.iql.dsl.query
import org.jetbrains.exposed.sql.Table
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class EntityExtensionTest {

    private object Companies : Table("companies") {
        val id = integer("id")
        val name = varchar("name", 100)
        val description = varchar("description", 200)
    }

    private object Departments : Table("departments") {
        val id = integer("id")
        val name = varchar("name", 100)
        val companyId = integer("company_id")
    }

    private object Employees : Table("employees") {
        val id = integer("id")
        val name = varchar("name", 100)
        val departmentId = integer("department_id")
    }

    private object Projects : Table("projects") {
        val id = integer("id")
        val name = varchar("name", 100)
        val departmentId = integer("department_id")
    }

    private object ProjectMembers : Table("project_members") {
        val projectId = integer("project_id")
        val employeeId = integer("employee_id")
    }

    private fun baseRegistry(): Registry =
        registry {

            entity("company", Companies) {
                field(Companies.name)

                oneToMany("departments", Departments) {
                    Companies.id references Departments.companyId
                }
            }

            entity("department", Departments) {
                field(Departments.name)

                manyToOne("company", Companies) {
                    Departments.companyId references Companies.id
                }
            }
        }

    @Test
    fun `extends existing entity with field`() {

        val registry =
            registry {

                include(baseRegistry())

                extend("company") {
                    field(Companies.description)
                }
            }

        val company =
            registry.getEntityOrThrow("company")

        assertNotNull(
            company.fields["name"]
        )

        assertNotNull(
            company.fields["description"]
        )
    }

    @Test
    fun `extension preserves existing fields`() {

        val registry =
            registry {

                include(baseRegistry())

                extend("company") {
                    field(Companies.description)
                }
            }

        val company =
            registry.getEntityOrThrow("company")

        assertEquals(
            setOf("name", "description"),
            company.fields.keys
        )
    }

    @Test
    fun `extension preserves existing relations`() {

        val registry =
            registry {

                include(baseRegistry())

                extend("company") {

                    field(Companies.description)

                    manyToOne("manager", Employees) {
                        Companies.id references Employees.id
                    }
                }
            }

        val company =
            registry.getEntityOrThrow("company")

        assertNotNull(
            company.relations["departments"]
        )

        assertNotNull(
            company.relations["manager"]
        )
    }

    @Test
    fun `extends entity with many to one relation`() {

        val registry =
            registry {

                include(baseRegistry())

                extend("company") {

                    manyToOne("manager", Employees) {
                        Companies.id references Employees.id
                    }
                }
            }

        val relation =
            registry
                .getEntityOrThrow("company")
                .relations["manager"]
                ?: error("Missing relation")

        assertEquals(
            RelationType.MANY_TO_ONE,
            relation.type
        )

        assertEquals(
            "employees",
            relation.targetEntity
        )

        assertEquals(
            listOf(Companies.id.name),
            relation.joinColumns
        )

        assertEquals(
            Employees.id.name,
            relation.inverseJoinColumn
        )
    }

    @Test
    fun `extends entity with one to many relation`() {

        val registry =
            registry {

                include(baseRegistry())

                extend("company") {

                    oneToMany("projects", Projects) {
                        Companies.id references Projects.departmentId
                    }
                }
            }

        val relation =
            registry
                .getEntityOrThrow("company")
                .relations["projects"]
                ?: error("Missing relation")

        assertEquals(
            RelationType.ONE_TO_MANY,
            relation.type
        )

        assertEquals(
            "projects",
            relation.targetEntity
        )
    }

    @Test
    fun `extends entity with many to many relation`() {

        val registry =
            registry {

                entity("company", Companies) {
                    field(Companies.name)
                }

                extend("company") {

                    manyToMany(
                        "employees",
                        Employees,
                        ProjectMembers
                    ) {
                        source(
                            Companies.id references ProjectMembers.projectId
                        )

                        target(
                            Employees.id references ProjectMembers.employeeId
                        )
                    }
                }

                entity("employee", Employees) {
                    field(Employees.name)
                }
            }

        val relation =
            registry
                .getEntityOrThrow("company")
                .relations["employees"]
                ?: error("Missing relation")

        assertEquals(
            RelationType.MANY_TO_MANY,
            relation.type
        )

        assertEquals(
            "employee",
            relation.targetEntity
        )

        assertNotNull(
            relation.mapping
        )
    }

    @Test
    fun `extension can add multiple fields and relations`() {

        val registry =
            registry {

                entity("company", Companies) {
                    field(Companies.name)
                }

                extend("company") {

                    field(Companies.description)

                    manyToOne("manager", Employees) {
                        Companies.id references Employees.id
                    }

                    oneToMany("departments", Departments) {
                        Companies.id references Departments.companyId
                    }
                }
            }

        val company =
            registry.getEntityOrThrow("company")

        assertEquals(
            setOf("name", "description"),
            company.fields.keys
        )

        assertEquals(
            setOf("manager", "departments"),
            company.relations.keys
        )
    }

    @Test
    fun `cannot extend unknown entity`() {

        assertFailsWith<IllegalArgumentException> {

            registry {

                extend("doesNotExist") {
                    field(Companies.name)
                }
            }
        }
    }

    @Test
    fun `extends existing entity with additional field`() {

        val result =
            registry {

                entity("company", Companies) {
                    field(Companies.name)
                }

                extend("company") {
                    field(Companies.id)
                }
            }

        val company =
            result.getEntityOrThrow("company")

        assertTrue("name" in company.fields)
        assertTrue("id" in company.fields)
    }

    @Test
    fun `cannot add duplicate field`() {

        assertFailsWith<IllegalArgumentException> {

            registry {

                entity("company", Companies) {
                    field(Companies.name)
                }

                extend("company") {
                    field(Companies.name)
                }
            }
        }
    }

    @Test
    fun `cannot add duplicate relation`() {

        assertFailsWith<IllegalArgumentException> {

            registry {

                entity("company", Companies) {
                    field(Companies.name)

                    manyToOne("manager", Employees) {
                        Companies.id references Employees.id
                    }
                }

                extend("company") {

                    manyToOne("manager", Employees) {
                        Companies.id references Employees.id
                    }
                }
            }
        }
    }

    @Test
    fun `can extend existing entity with different field and relation`() {
        val result =
            registry {
                entity("company", Companies) {
                    field(Companies.name)
                }

                extend("company") {
                    field(Companies.id)

                    manyToOne("manager", Employees) {
                        Companies.id references Employees.id
                    }
                }
            }

        val company =
            result.getEntityOrThrow("company")

        assertTrue("name" in company.fields)
        assertTrue("id" in company.fields)
        assertTrue("manager" in company.relations)
    }

    @Suppress("MapGetWithNotNullAssertionOperator")
    @Test
    fun `extension preserves relation target`() {

        val registry =
            registry {

                entity("company", Companies) {
                    field(Companies.name)

                    oneToMany("departments", Departments) {
                        Companies.id references Departments.companyId
                    }
                }

                extend("company") {

                    manyToOne("manager", Employees) {
                        Companies.id references Employees.id
                    }
                }
            }

        val company =
            registry.getEntityOrThrow("company")

        assertEquals(
            "departments",
            company.relations["departments"]!!.targetEntity
        )

        assertEquals(
            "employees",
            company.relations["manager"]!!.targetEntity
        )
    }

    @Test
    fun `extension works after include`() {

        val base =
            registry {

                entity("company", Companies) {
                    field(Companies.name)
                }
            }

        val registry =
            registry {

                include(base)

                extend("company") {
                    field(Companies.description)
                }
            }

        val company =
            registry.getEntityOrThrow("company")

        assertEquals(
            "company",
            company.name
        )

        assertEquals(
            setOf("name", "description"),
            company.fields.keys
        )
    }

    @Test
    fun `extension can be applied multiple times`() {

        val registry =
            registry {

                entity("company", Companies) {
                    field(Companies.name)
                }

                extend("company") {
                    field(Companies.description)
                }

                extend("company") {

                    manyToOne("manager", Employees) {
                        Companies.id references Employees.id
                    }
                }
            }

        val company =
            registry.getEntityOrThrow("company")

        assertNotNull(
            company.fields["name"]
        )

        assertNotNull(
            company.fields["description"]
        )

        assertNotNull(
            company.relations["manager"]
        )
    }

    @Test
    fun `extension relation can be resolved afterwards`() {

        val registry =
            registry {

                entity("company", Companies) {
                    field(Companies.name)
                }

                entity("department", Departments) {
                    field(Departments.name)
                }

                extend("company") {

                    oneToMany("departments", Departments) {
                        Companies.id references Departments.companyId
                    }
                }
            }

        val compiler =
            ExposedCompiler(registry)

        val filter =
            query("company") {
                where {
                    p("departments.name") eq "Engineering"
                }
            }.filter!!

        val resolved =
            compiler.resolveForTest(
                filter,
                registry.getEntityOrThrow("company")
            )

        assertEquals(
            "department",
            resolved.entity
        )

        assertEquals(
            "name",
            resolved.field
        )

        assertEquals(
            listOf(
                "company -> departments -> department"
            ),
            resolved.relationPath.map {
                "${it.sourceEntity.name} -> " +
                        "${it.relation.name} -> " +
                        it.targetEntity.name
            }
        )
    }
}
