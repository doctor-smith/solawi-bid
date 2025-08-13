package org.solyton.solawi.bid.application.pipeline

import io.ktor.server.application.*
import kotlinx.coroutines.runBlocking
import org.evoleq.exposedx.migrations.Migration
import org.evoleq.exposedx.migrations.runOn
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ExperimentalDatabaseMigrationApi
import org.solyton.solawi.bid.application.data.db.migrations.structuralMigrations
import org.solyton.solawi.bid.application.environment.Environment

@OptIn(ExperimentalDatabaseMigrationApi::class)
fun Application.installDatabase(environment: Environment, migrations: ArrayList<Database.()-> Migration> = arrayListOf()): Database = runBlocking {
    val database = environment.connectToDatabase()
    /*
    val tables = listOf(
        AddMissingColumns(
            UsersTable, listOf(ColumnDef<UUID>("CREATED_BY", UUID(0L,0L)))
        )
    )
    database.addMissingColumns(*tables.toTypedArray())
*/
    structuralMigrations.runOn(database)
    migrations.runOn(database)
    database
}

fun Application.installUsers(environment: Environment, database: Database) {
    environment.injectUsers(database)
}


