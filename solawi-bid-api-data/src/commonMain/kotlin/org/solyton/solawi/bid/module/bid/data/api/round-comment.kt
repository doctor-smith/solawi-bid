package org.solyton.solawi.bid.module.bid.data.api

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

typealias ApiRoundComments = RoundComments
typealias ApiRoundComment = RoundComment


@Serializable
data class RoundComments(
    val all: List<RoundComment>
)

@Serializable
data class RoundComment(
    val id: String,
    val comment: String,
    val createAt: LocalDate,
    val createdBy: String
)
