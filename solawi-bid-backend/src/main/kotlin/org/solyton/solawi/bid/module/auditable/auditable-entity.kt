package org.solyton.solawi.bid.module.auditable

import org.joda.time.DateTime


/**
 * Implement in exposed entities
 */
interface AuditableEntity<Id> {
    var createdAt: DateTime
    var createdBy: Id
    var modifiedAt: DateTime?
    var modifiedBy: Id?
}
