package org.solyton.solawi.bid.module.application.schema

import org.jetbrains.exposed.sql.SqlExpressionBuilder.neq
import org.solyton.solawi.bid.module.auditable.AuditableUUIDTable

typealias ApplicationDependenciesTable = ApplicationDependencies

/**
 * Global product-catalog dependency (dependent application -> required application).
 * This table does not grant user, organization or tenant permissions.
 */
object ApplicationDependencies : AuditableUUIDTable("application_dependencies") {
    val applicationId = reference("application_id", ApplicationsTable.id)
    val requiredApplicationId = reference("required_application_id", ApplicationsTable.id)

    init {
        uniqueIndex(applicationId, requiredApplicationId)
        check("chk_application_dependency_not_self") {
            applicationId neq requiredApplicationId
        }
    }
}
