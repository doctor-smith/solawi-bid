package org.solyton.solawi.bid.module.auditable.iql

import org.evoleq.exposedx.iql.EntityTypeConfiguration
import org.solyton.solawi.bid.module.auditable.AuditableTable

fun <Id> EntityTypeConfiguration.auditableFields(
    table: AuditableTable<Id>) where Id: Comparable<Id> =
    with(table) {
        field(createdAt)
        field(createdBy)
        field(modifiedAt)
        field(modifiedBy)
    }
