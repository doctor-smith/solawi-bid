package org.solyton.solawi.bid.module.bid.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import java.util.UUID

object ShareStatusTable : UUIDTable("share_status") {
    val name = varchar( "name", length = 50).uniqueIndex()

    val description = text("description")
}

class ShareStatusEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ShareStatusEntity>(ShareStatusTable)

    var name by ShareStatusTable.name
    var description by ShareStatusTable.description
}
