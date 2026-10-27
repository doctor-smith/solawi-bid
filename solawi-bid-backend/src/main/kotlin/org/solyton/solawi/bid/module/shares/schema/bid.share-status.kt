package org.solyton.solawi.bid.module.shares.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.solyton.solawi.bid.module.shares.data.internal.ShareStatus
import org.solyton.solawi.bid.module.shares.data.internal.ShareStatus.Companion.from
import java.util.*

object ShareStatusTable : UUIDTable("share_status") {
    val name = varchar( "name", length = 50).uniqueIndex()

    val description = text("description")
}

class ShareStatusEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ShareStatusEntity>(ShareStatusTable)

    var name by ShareStatusTable.name
    var description by ShareStatusTable.description

    /**
     * Compares this ShareStatusEntity with internal [ShareStatus] for equality.
     *
     * Background: It has been a common mistake to compare internal status and status
     * entity directly in the code, which led to wrong comparison results. The solution
     * was to add this == operator
     *
     * @param other the object to compare with this instance.
     * @return `true` if the other object is a ShareStatus and corresponds to
     * the status represented by this entity's name; otherwise, delegates to
     * the superclass's equality check.
     */
    fun isEqualTo(other: ShareStatus?): Boolean = when(other) {
        is ShareStatus -> from(this.name) == other
        else -> false
    }

    override fun toString(): String {
        return this.name
    }
}

/**
 * Compares this instance of [ShareStatus] with a [ShareStatusEntity] for equality.
 *
 * Background: It has been a common mistake to compare internal status and status
 * entity directly in the code, which led to wrong comparison results. The solution
 * was to add this == operator
 *
 * @param other the [ShareStatusEntity] to compare with this instance.
 * @return `true` if the given [ShareStatusEntity] represents the same status as this [ShareStatus];
 * `false` otherwise.
 */
fun ShareStatus.isEqualTo(other: ShareStatusEntity?): Boolean = when(other){
    is ShareStatusEntity -> from(other.name) == this
    else -> false
}

