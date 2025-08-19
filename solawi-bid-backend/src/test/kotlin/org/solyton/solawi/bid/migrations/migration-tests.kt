package org.solyton.solawi.bid.migrations

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.Migration
import org.solyton.solawi.bid.application.data.db.migrations.Config
import org.solyton.solawi.bid.application.data.db.migrations.migrate

class MigrationTests {
    @Migration@Test
    fun runMigrationsOnH2() = runBlocking { migrate(Config.H2) }
}
