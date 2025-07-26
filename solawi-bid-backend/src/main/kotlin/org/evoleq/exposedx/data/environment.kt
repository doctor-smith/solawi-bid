package org.evoleq.exposedx.data

import org.jetbrains.exposed.sql.Database

interface DbEnv {
    fun connectToDatabase(): Database
}
