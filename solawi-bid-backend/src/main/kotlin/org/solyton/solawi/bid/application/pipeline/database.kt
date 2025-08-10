package org.solyton.solawi.bid.application.pipeline

import io.ktor.server.application.*
import kotlinx.coroutines.runBlocking
import org.evoleq.exposedx.migrations.Migration
import org.evoleq.exposedx.migrations.runOn
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ExperimentalDatabaseMigrationApi
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.vendors.H2Dialect
import org.solyton.solawi.bid.application.environment.Environment
import org.solyton.solawi.bid.module.user.schema.UsersTable

@OptIn(ExperimentalDatabaseMigrationApi::class)
fun Application.installDatabase(environment: Environment, migrations: ArrayList<Database.()-> Migration> = arrayListOf()): Database = runBlocking {
    val database = environment.connectToDatabase()
    if(database.dialect !is H2Dialect) {
        transaction(database) {
            val script = SchemaUtils.generateMigrationScript(
                UsersTable,
                scriptDirectory = "/app",
                scriptName = "migration",
                withLogs = true
            )
            val queryString = script.readText()

            println(
                """
            |
            |
            |queryString = $queryString
            |
            |
            |""".trimMargin()
            )

            exec(queryString)
        }
    }

    migrations.runOn(database)
    database
}

fun Application.installUsers(environment: Environment, database: Database) {
    environment.injectUsers(database)
}

