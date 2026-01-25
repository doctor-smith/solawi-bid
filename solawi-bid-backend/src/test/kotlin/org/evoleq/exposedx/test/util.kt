package org.evoleq.exposedx.test

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

interface TestDbConfig {
    val url: String
    val driver: String
    val user: String
    val password: String
}

object Config {
    object H2 : TestDbConfig {
        override val url: String = "jdbc:h2:mem:test"
        override val driver: String = "org.h2.Driver"
        override val user: String = "root"
        override val password: String = ""
    }
    object H2NoClose : TestDbConfig  {
        override val url: String = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"
        override val driver: String = "org.h2.Driver"
        override val user: String = "root"
        override val password: String = ""
    }

    val H2NoCloseUniqueDB : (String) -> TestDbConfig = {dbName: String -> object: TestDbConfig {
        override val url: String = "jdbc:h2:mem:test-$dbName;DB_CLOSE_DELAY=-1"
        override val driver: String = "org.h2.Driver"
        override val user: String = "root"
        override val password: String = ""
    }}
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

    }
    transaction{
        exec("SET REFERENTIAL_INTEGRITY FALSE")
        SchemaUtils.drop(*tables)
        exec("SET REFERENTIAL_INTEGRITY TRUE")
    }
}

fun runSimpleH2Test(databaseId: String,vararg tables: Table, block: Transaction.()->Unit) {
    val config = Config.H2NoCloseUniqueDB(databaseId)
    Database.connect(
        url = config.url,
        driver = config.driver,
        user = config.user,
        password = config.password
    )

    transaction {
        addLogger(StdOutSqlLogger)
        SchemaUtils.create(*tables)
        exec("SET REFERENTIAL_INTEGRITY FALSE")
        block()
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
