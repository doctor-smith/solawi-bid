package org.evoleq.exposedx.iql

import org.evoleq.exposedx.test.runSimpleH2Test
import org.evoleq.iql.dsl.where
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import kotlin.test.Test
import kotlin.test.assertEquals

object Companies : Table("test_companies") {
    val id = integer("id")
    val name = varchar("name", 100)
    val ownerId = integer("owner_id")

    override val primaryKey = PrimaryKey(id)
}

object Persons : Table("test_persons") {
    val id = integer("id")
    val name = varchar("name", 100)

    override val primaryKey = PrimaryKey(id)
}

object Teams : Table("test_teams") {
    val id = integer("id")
    val name = varchar("name", 100)
    val ownerId = integer("owner_id")

    override val primaryKey = PrimaryKey(id)
}

object Projects : Table("test_projects") {
    val id = integer("id")
    val name = varchar("name", 100)

    override val primaryKey = PrimaryKey(id)
}

object TeamProjects : Table("test_team_projects") {
    val teamId = integer("team_id")
    val projectId = integer("project_id")
}

fun testRelationRegistry(): Registry =
    registry {

        fieldNameStrategy =
            FieldNameStrategy.SNAKE_CASE

        entity("company", Companies) {

            field(Companies.name)

            manyToOne("owner", Persons) {
                Companies.ownerId references Persons.id
            }
        }

        entity("person", Persons) {

            field(Persons.name)

            oneToMany("teams", Teams) {
                Persons.id references Teams.ownerId
            }
        }

        entity("team", Teams) {

            field(Teams.name)

            manyToOne("owner", Persons) {
                Teams.ownerId references Persons.id
            }

            manyToMany(
                "projects",
                Projects,
                TeamProjects
            ) {
                source(Teams.id references TeamProjects.teamId)
                target(Projects.id references TeamProjects.projectId)
            }
        }

        entity("project", Projects) {

            field(Projects.name)
        }
    }

class ExposedCompilerExecutionTest {
    @Test
    fun `many-to-one path resolves target field`() =
        runSimpleH2Test(
            Companies,
            Persons
        ) {

            Persons.insert {
                it[id] = 1
                it[name] = "Alice"
            }

            Persons.insert {
                it[id] = 2
                it[name] = "Bob"
            }

            Companies.insert {
                it[id] = 1
                it[name] = "Company A"
                it[ownerId] = 1
            }

            Companies.insert {
                it[id] = 2
                it[name] = "Company B"
                it[ownerId] = 2
            }

            val filter = where {
                    p("owner.name") eq "Alice"
                }


            val result =
                Companies
                    .selectAll()
                    .where {
                        ExposedCompiler(
                            testRelationRegistry()
                        ).compile(
                            filter,
                            Companies
                        )
                    }
                    .map {
                        it[Companies.name]
                    }

            assertEquals(
                listOf("Company A"),
                result
            )
        }

    @Test
    fun `one-to-many path resolves target field`() =
        runSimpleH2Test(
            Persons,
            Teams
        ) {

            Persons.insert {
                it[id] = 1
                it[name] = "Alice"
            }

            Persons.insert {
                it[id] = 2
                it[name] = "Bob"
            }

            Teams.insert {
                it[id] = 1
                it[name] = "Red"
                it[ownerId] = 1
            }

            Teams.insert {
                it[id] = 2
                it[name] = "Blue"
                it[ownerId] = 2
            }

            val filter =
                where {
                    p("teams.name") eq "Red"
                }

            val result =
                Persons
                    .selectAll()
                    .where {
                        ExposedCompiler(
                            testRelationRegistry()
                        ).compile(
                            filter,
                            Persons
                        )
                    }
                    .map {
                        it[Persons.name]
                    }

            assertEquals(
                listOf("Alice"),
                result
            )
        }

    @Test
    fun `many-to-many path resolves target field`() =
        runSimpleH2Test(
            Teams,
            Projects,
            TeamProjects
        ) {

            Teams.insert {
                it[id] = 1
                it[name] = "Red"
                it[ownerId] = 1
            }

            Teams.insert {
                it[id] = 2
                it[name] = "Blue"
                it[ownerId] = 2
            }

            Projects.insert {
                it[id] = 1
                it[name] = "Apollo"
            }

            Projects.insert {
                it[id] = 2
                it[name] = "Gemini"
            }

            TeamProjects.insert {
                it[teamId] = 1
                it[projectId] = 1
            }

            TeamProjects.insert {
                it[teamId] = 2
                it[projectId] = 2
            }

            val filter =
                where {
                    p("projects.name") eq "Apollo"
                }

            val result =
                Teams
                    .selectAll()
                    .where {
                        ExposedCompiler(
                            testRelationRegistry()
                        ).compile(
                            filter,
                            Teams
                        )
                    }
                    .map {
                        it[Teams.name]
                    }

            assertEquals(
                listOf("Red"),
                result
            )
        }

    @Test
    fun `many-to-one followed by one-to-many resolves correctly`() =
        runSimpleH2Test(
            Companies,
            Persons,
            Teams
        ) {

            Persons.insert {
                it[id] = 1
                it[name] = "Alice"
            }

            Persons.insert {
                it[id] = 2
                it[name] = "Bob"
            }

            Teams.insert {
                it[id] = 1
                it[name] = "Red"
                it[ownerId] = 1
            }

            Companies.insert {
                it[id] = 1
                it[name] = "Company A"
                it[ownerId] = 1
            }

            Companies.insert {
                it[id] = 2
                it[name] = "Company B"
                it[ownerId] = 2
            }

            val filter =
                where {
                    p("owner.teams.name") eq "Red"
                }

            val result =
                Companies
                    .selectAll()
                    .where {
                        ExposedCompiler(
                            testRelationRegistry()
                        ).compile(
                            filter,
                            Companies
                        )
                    }
                    .map {
                        it[Companies.name]
                    }

            assertEquals(
                listOf("Company A"),
                result
            )
        }

    @Test
    fun `one-to-many followed by many-to-many resolves correctly`() =
        runSimpleH2Test(
            Persons,
            Teams,
            Projects,
            TeamProjects
        ) {

            Persons.insert {
                it[id] = 1
                it[name] = "Alice"
            }

            Persons.insert {
                it[id] = 2
                it[name] = "Bob"
            }

            Teams.insert {
                it[id] = 1
                it[name] = "Red"
                it[ownerId] = 1
            }

            Teams.insert {
                it[id] = 2
                it[name] = "Blue"
                it[ownerId] = 2
            }

            Projects.insert {
                it[id] = 1
                it[name] = "Apollo"
            }

            Projects.insert {
                it[id] = 2
                it[name] = "Gemini"
            }

            TeamProjects.insert {
                it[teamId] = 1
                it[projectId] = 1
            }

            TeamProjects.insert {
                it[teamId] = 2
                it[projectId] = 2
            }

            val filter =
                where {
                    p("teams.projects.name") eq "Apollo"
                }

            val result =
                Persons
                    .selectAll()
                    .where {
                        ExposedCompiler(
                            testRelationRegistry()
                        ).compile(
                            filter,
                            Persons
                        )
                    }
                    .map {
                        it[Persons.name]
                    }

            assertEquals(
                listOf("Alice"),
                result
            )
        }


    /**
     * Company
     *   │
     *   │ MANY_TO_ONE
     *   ▼
     * Person
     *   │
     *   │ ONE_TO_MANY
     *   ▼
     * Team
     *   │
     *   │ MANY_TO_MANY
     *   ▼
     * Project
     */
    @Test
    fun `mixed deep relation path resolves correctly`() =
        runSimpleH2Test(
            Companies,
            Persons,
            Teams,
            Projects,
            TeamProjects
        ) {

            Persons.insert {
                it[id] = 1
                it[name] = "Alice"
            }

            Persons.insert {
                it[id] = 2
                it[name] = "Bob"
            }

            Teams.insert {
                it[id] = 1
                it[name] = "Red"
                it[ownerId] = 1
            }

            Teams.insert {
                it[id] = 2
                it[name] = "Blue"
                it[ownerId] = 2
            }

            Projects.insert {
                it[id] = 1
                it[name] = "Apollo"
            }

            Projects.insert {
                it[id] = 2
                it[name] = "Gemini"
            }

            TeamProjects.insert {
                it[teamId] = 1
                it[projectId] = 1
            }

            TeamProjects.insert {
                it[teamId] = 2
                it[projectId] = 2
            }

            Companies.insert {
                it[id] = 1
                it[name] = "Company A"
                it[ownerId] = 1
            }

            Companies.insert {
                it[id] = 2
                it[name] = "Company B"
                it[ownerId] = 2
            }

            val filter =
                where {
                    p(
                        "owner.teams.projects.name"
                    ) eq "Apollo"
                }

            val result =
                Companies
                    .selectAll()
                    .where {
                        ExposedCompiler(
                            testRelationRegistry()
                        ).compile(
                            filter,
                            Companies
                        )
                    }
                    .map {
                        it[Companies.name]
                    }

            assertEquals(
                listOf("Company A"),
                result
            )
        }
}
