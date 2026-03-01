

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.or
import org.solyton.solawi.bid.module.shares.data.internal.ShareStatus

/**
 * Allows comparing a database column to a specific `ShareStatus` value.
 * For example: `ShareStatusTable.name eq ShareStatus.Subscribed`
 */
infix fun Column<String>.eq(status: ShareStatus): Op<Boolean> =
    this.eq(status.value) or this.eq(status.toString())
