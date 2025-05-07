package org.solyton.solawi.bid.application.pipeline

import io.ktor.server.application.*
import kotlinx.coroutines.runBlocking
import org.evoleq.exposedx.migrations.Migration
import org.evoleq.exposedx.migrations.runOn
import org.jetbrains.exposed.sql.Database
import org.solyton.solawi.bid.application.environment.Environment

fun Application.installDatabase(environment: Environment, migrations: ArrayList<Database.()-> Migration> = arrayListOf()): Database = runBlocking {
    val database = environment.connectToDatabase()
    migrations.runOn(database)
    database
}

fun Application.installUsers(environment: Environment, database: Database) {
    environment.injectUsers(database)
}

