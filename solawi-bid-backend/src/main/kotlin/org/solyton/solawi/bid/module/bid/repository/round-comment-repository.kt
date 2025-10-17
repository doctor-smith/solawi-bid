package org.solyton.solawi.bid.module.bid.repository

import org.solyton.solawi.bid.module.bid.schema.RoundCommentEntity
import org.solyton.solawi.bid.module.bid.schema.RoundEntity
import java.util.UUID

fun RoundEntity.addComment(comment: String, creatorId: UUID): RoundEntity {
    val newComment = RoundCommentEntity.new {
        round = this@addComment
        this.comment = comment
        createdBy = creatorId
    }
    comments+newComment
    return this
}
