package org.solyton.solawi.bid.module.shares.repository

import org.jetbrains.exposed.sql.Transaction
import org.solyton.solawi.bid.module.shares.schema.ShareSubscriptionStatusHistory
import org.solyton.solawi.bid.module.shares.schema.ShareSubscriptionStatusHistoryEntry
import java.util.*

/**
 * Reads the history of share subscription status changes for the specified share subscription IDs.
 *
 * @param shareSubscriptionIds A list of UUIDs representing the share subscriptions
 * for which status change histories are to be fetched.
 * @return A list of ShareSubscriptionStatusHistoryEntry objects representing the
 * history entries for the specified share subscriptions.
 */
fun Transaction.readShareSubscriptionStatusHistories(shareSubscriptionIds: List<UUID>): List<ShareSubscriptionStatusHistoryEntry> =
    ShareSubscriptionStatusHistoryEntry.find { ShareSubscriptionStatusHistory.shareSubscriptionId inList shareSubscriptionIds
}.toList()
