package org.solyton.solawi.bid.module.permission.data.api

import kotlinx.serialization.Serializable
import org.solyton.solawi.bid.module.permission.data.RightId
import org.solyton.solawi.bid.module.permission.data.RightName
import org.solyton.solawi.bid.module.values.Description

@Serializable
data class CreateRights(
    val rights: List<CreateRight>
)

@Serializable
data class CreateRight(
    val name: RightName,
    val description: Description
)

@Serializable
data class UpdateRight(
    val rightId: RightId,
    val name: RightName,
    val description: Description
)

@Serializable
data class DeleteRight(
    val rightId: RightId
)
