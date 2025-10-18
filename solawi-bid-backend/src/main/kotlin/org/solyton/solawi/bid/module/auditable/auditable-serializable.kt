package org.solyton.solawi.bid.module.auditable

import kotlinx.datetime.LocalDateTime


/**
 * Implement in serializable classes
 */
interface Auditable<Id> {
    val createdAt: LocalDateTime
    val createdBy: Id
    val modifiedAt: LocalDateTime?
    val modifiedBy: Id?
}
