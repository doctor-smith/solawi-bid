package org.solyton.solawi.bid.module.bid.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.auditable.AuditableEntity
import org.solyton.solawi.bid.module.auditable.AuditableUUIDTable
import org.solyton.solawi.bid.module.user.schema.UserEntity
import org.solyton.solawi.bid.module.user.schema.UsersTable
import java.util.UUID

typealias CoSubscribersTable = CoSubscribers
typealias CoSubscriberEntity = CoSubscriber

object CoSubscribers : AuditableUUIDTable("co_subscribers") {
    val shareSubscriptionId = reference("share_subscription_id", ShareSubscriptionsTable)
    val userId =reference("user_id", UsersTable)

    init {
        uniqueIndex(shareSubscriptionId, userId)
    }
}

class CoSubscriber(id: EntityID<UUID>): UUIDEntity(id), AuditableEntity<UUID> {
    companion object:  UUIDEntityClass<CoSubscriber>(CoSubscribers)

    var shareSubscription by ShareSubscriptionEntity referencedOn CoSubscribers.shareSubscriptionId
    var user by UserEntity referencedOn CoSubscribers.userId

    override var createdAt: DateTime by CoSubscribers.createdAt
    override var createdBy: UUID by CoSubscribers.createdBy
    override var modifiedAt: DateTime? by CoSubscribers.modifiedAt
    override var modifiedBy: UUID? by CoSubscribers.modifiedBy
}
