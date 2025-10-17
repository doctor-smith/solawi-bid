package org.solyton.solawi.bid.module.bid.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.auditable.AuditableEntity
import org.solyton.solawi.bid.module.auditable.AuditableUUIDTable
import java.util.UUID

typealias RoundCommentsTable = RoundComments
typealias RoundCommentEntity = RoundComment

object RoundComments : AuditableUUIDTable("round_comments") {
    val roundId = reference("round_id", RoundsTable)
    val comment = text("comment")
}

class RoundComment(id: EntityID<UUID>) : UUIDEntity(id), AuditableEntity<UUID> {
    companion object : UUIDEntityClass<RoundComment>(RoundCommentsTable)

    var round by RoundEntity referencedOn RoundComments.roundId
    var comment by RoundCommentsTable.comment

    override var createdAt: DateTime by RoundCommentsTable.createdAt
    override var createdBy: UUID by RoundCommentsTable.createdBy
    override var modifiedAt: DateTime? by RoundCommentsTable.modifiedAt
    override var modifiedBy: UUID? by RoundCommentsTable.modifiedBy
}
