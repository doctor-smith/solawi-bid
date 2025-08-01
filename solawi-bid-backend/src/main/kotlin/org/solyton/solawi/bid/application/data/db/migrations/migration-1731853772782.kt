package org.solyton.solawi.bid.application.data.db.migrations

import org.evoleq.exposedx.migrations.Migration
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Transaction
import org.solyton.solawi.bid.module.user.schema.Sessions
import org.solyton.solawi.bid.module.user.schema.Tokens

/**
 * Autogenerated [Migration],
 * generated by the evoleq/exposedx migration gradle-plugin.
 * Alter with care!
 *
 * Generated at Sun Nov 17 15:29:32 CET 2024
 *
 * Description:
 * Add tables for authorization / authentication
 * - Tokens
 * - Sessions
 */
class Migration1731853772782(
    override val database: Database
) : Migration {

    /**
     * Id of the migration, do not change!
     */
    override val id: Long
        get() = 1731853772782

    /**
     * Upwards migration
     */
    override suspend fun Transaction.up() {
        SchemaUtils.create(
            Tokens,
            Sessions
        )
    }

    /**
     * Downwards migration (inverse to the upward migration).
     * These migrations are not taken into account by now!
     */
    override suspend fun Database.down() {
        TODO("Not yet implemented")
    }
}
