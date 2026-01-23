package org.solyton.solawi.bid.module.application.repository

import org.jetbrains.exposed.sql.Transaction
import org.solyton.solawi.bid.module.application.schema.OrganizationBundleEntity
import org.solyton.solawi.bid.module.application.schema.OrganizationBundlesTable
import java.util.*


fun Transaction.readUserBundlesOfOrganization(organizationId: UUID): List<OrganizationBundleEntity> = OrganizationBundleEntity.find {
    OrganizationBundlesTable.organizationId eq organizationId
}.toList()

fun Transaction.readOrganizationBundleSubscriptions(bundleId: UUID): List<OrganizationBundleEntity> = OrganizationBundleEntity.find {
    OrganizationBundlesTable.bundleId eq bundleId
}.toList()

