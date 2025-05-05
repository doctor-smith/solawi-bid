package org.evoleq.exposedx.test

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object Config {
    object H2 {
        const val url: String = "jdbc:h2:mem:test"
        const val driver: String = "org.h2.Driver"
        const val user: String = "root"
        const val password: String = ""
    }
}

fun runSimpleH2Test(vararg tables: Table, block: Transaction.()->Unit) {
    Database.connect(
        url = Config.H2.url,
        driver = Config.H2.driver,
        user = Config.H2.user,
        password = Config.H2.password
    )

    transaction {
        addLogger(StdOutSqlLogger)
        SchemaUtils.create(*tables)
        block()
        SchemaUtils.drop(*tables)
    }
}

fun runH2MigrationTest(block: Database.()->Unit) {
    transaction {
        Database.connect(
            url = Config.H2.url,
            driver = Config.H2.driver,
            user = Config.H2.user,
            password = Config.H2.password
        ).block()
    }
}

fun runH2Test(vararg tables: Table, block: (Database)->Unit) {
    val database = Database.connect(
        url = Config.H2.url,
        driver = Config.H2.driver,
        user = Config.H2.user,
        password = Config.H2.password
    )

    transaction {
        addLogger(StdOutSqlLogger)
        SchemaUtils.create(*tables)
    }

    block(database)

    transaction {
        addLogger(StdOutSqlLogger)
        SchemaUtils.drop(*tables)
    }
}