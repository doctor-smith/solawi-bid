package org.solyton.solawi.bid.module.shares.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.neq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or
import org.solyton.solawi.bid.module.shares.data.internal.ChangeReason
import org.solyton.solawi.bid.module.shares.data.internal.ChangedBy
import java.util.*

object ShareSubscriptionStatusHistory : UUIDTable("share_subscription_status_history") {
    val shareSubscriptionId = reference("share_subscription_id", ShareSubscriptionsTable)
    val rollingOverFromSubscriptionId = optReference(
        "rolling_over_from_subscription_id",
        ShareSubscriptionsTable
    ).default(null)
    val fromStatusId = optReference("from_status_id", ShareStatusTable)
    val toStatusId = reference("to_status_id", ShareStatusTable)
    val reason = enumeration<ChangeReason>("reason")
    val changedBy = enumeration<ChangedBy>("changed_by")
    val humanModifierId = uuid("modified").nullable()
    val comment = text("comment").nullable()

    init {
        // CHECK Constraint: fromStatusId only null, if reason = INITIAL_CREATION
        val isInitialOrRollover = (reason eq ChangeReason.INITIAL_CREATION) or (reason eq ChangeReason.ROLLOVER)
        val isNotInitialOrRollover = (reason neq ChangeReason.INITIAL_CREATION) and (reason neq ChangeReason.ROLLOVER)
        check("chk_from_status_initial_activation_and_rollover") {
            (fromStatusId.isNull() and isInitialOrRollover) or
            (fromStatusId.isNotNull() and isNotInitialOrRollover)
        }
        // check rollover and former subscription ids
        check("chk_rollover") {
            (reason eq ChangeReason.ROLLOVER and (rollingOverFromSubscriptionId.isNotNull())) or
            (reason neq ChangeReason.ROLLOVER and (rollingOverFromSubscriptionId.isNull()))
        }

        check("chk_human_modification_requires_modifier_id_and_comment") {
            (changedBy eq ChangedBy.SYSTEM) or (humanModifierId.isNotNull() and comment.isNotNull())
        }
        check("chk_system_modification_rules_out_human_modifiers") {
            (changedBy neq ChangedBy.SYSTEM) or humanModifierId.isNull()
        }
    }
}

class ShareSubscriptionStatusHistoryEntry(id: EntityID<UUID>): UUIDEntity(id) {
    companion object : UUIDEntityClass<ShareSubscriptionStatusHistoryEntry>(ShareSubscriptionStatusHistory)

    var shareSubscription by ShareSubscriptionEntity referencedOn ShareSubscriptionStatusHistory.shareSubscriptionId
    var rollingOverFromShareSubscription by ShareSubscriptionEntity optionalReferencedOn ShareSubscriptionStatusHistory.rollingOverFromSubscriptionId
    var fromStatus by ShareStatusEntity optionalReferencedOn ShareSubscriptionStatusHistory.fromStatusId
    var toStatus by ShareStatusEntity referencedOn ShareSubscriptionStatusHistory.toStatusId

    var reason by ShareSubscriptionStatusHistory.reason
    var changedBy by ShareSubscriptionStatusHistory.changedBy
    var humanModifierId by ShareSubscriptionStatusHistory.humanModifierId
    var comment by ShareSubscriptionStatusHistory.comment
}
