package org.solyton.solawi.bid.migrations

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.Schema
import org.solyton.solawi.bid.module.db.migrations.Config
import org.solyton.solawi.bid.module.db.migrations.migrate

class MigrationTests {
    @Schema@Test
    fun runMigrationsOnH2() = runBlocking { migrate(Config.H2) }
}