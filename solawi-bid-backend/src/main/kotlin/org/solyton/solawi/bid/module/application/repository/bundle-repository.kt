package org.solyton.solawi.bid.module.application.repository

import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.application.data.domain.BundleDefinition
import org.solyton.solawi.bid.module.application.exception.ApplicationException
import org.solyton.solawi.bid.module.application.schema.ApplicationEntity
import org.solyton.solawi.bid.module.application.schema.ApplicationsTable
import org.solyton.solawi.bid.module.application.schema.BundleDefinitionEntity
import org.solyton.solawi.bid.module.application.schema.BundleDefinitionsTable
import org.solyton.solawi.bid.module.application.schema.BundleEntity
import org.solyton.solawi.bid.module.application.schema.BundlesTable
import org.solyton.solawi.bid.module.application.schema.ModuleEntity
import org.solyton.solawi.bid.module.application.schema.ModulesTable
import java.util.*

/**
 * Create [org.solyton.solawi.bid.module.application.schema.Bundle] together with [org.solyton.solawi.bid.module.application.schema.BundleDefinitions]
 */
fun Transaction.createBundle(
    name: String,
    description: String,
    bundleDefinition: BundleDefinition,
    creator: UUID
): BundleEntity {
    // validation
    if(readBundleByName(name) != null) throw ApplicationException.DuplicateBundleName(name)

    // apps and modules exist
    validateBundleDefinition(bundleDefinition)

    // create the bundle
    val bundle = BundleEntity.new {
        createdBy = creator
        this.name = name
        this.description = description
    }

    // create corresponding entries in bundle_definitions
    addBundleDefinition(bundleDefinition, bundle.id.value, creator)

    return bundle
}


fun Transaction.readBundle(bundleId: UUID): BundleEntity? = BundleEntity.findById(bundleId)

fun Transaction.readBundleByName(name: String): BundleEntity? = BundleEntity.find {
    BundlesTable.name eq name
}.firstOrNull()

fun Transaction.updateBundle(
    bundleId: UUID,
    name: String,
    description: String,
    bundleDefinition: BundleDefinition,
    modifier: UUID
): BundleEntity {
    // validate
    val bundle = readBundle(bundleId)
    if(bundle == null) throw ApplicationException.NoSuchBundle(bundleId.toString())

    validateBundleDefinition(bundleDefinition)

    var changed = false

    // update bundle
    if(name != bundle.name) {
        bundle.name = name
        changed = true
    }
    if(description != bundle.description) {
        bundle.description = description
        changed = true
    }

    changed =  updateBundleDefinition(bundle.id.value, bundleDefinition, modifier) || changed

    if(changed) {
        bundle.modifiedBy = modifier
        bundle.modifiedAt = DateTime.now()
    }

    return bundle
}

fun Transaction.deleteBundle(bundleId: UUID): UUID {
    val userBundles = readUserBundleSubscriptions(bundleId)
    val organizationBundles = readOrganizationBundleSubscriptions(bundleId)

    if(userBundles.isNotEmpty() || organizationBundles.isNotEmpty()) throw ApplicationException.CannotDeleteBundle(
        bundleId.toString(),
        "Active subscriptions: " +
                "users: ${userBundles.joinToString { it.user.toString() }}" +
                "organizations: ${organizationBundles.joinToString { it.organization.toString() }}"
    )

    removeBundleDefinitionOfBundle(bundleId)
    BundlesTable.deleteWhere { BundlesTable.id eq bundleId }
    return bundleId
}


internal fun addBundleDefinition(
    bundleDefinition: BundleDefinition,
    bundleId: UUID,
    creator: UUID
) {
    bundleDefinition.entries.forEach { (appId, modules) ->
        modules.forEach { modId ->
            BundleDefinitionsTable.insert {
                it[createdBy] = creator
                it[BundleDefinitionsTable.bundleId] = bundleId
                it[applicationId] = appId
                it[moduleId] = modId
            }
        }
    }
}

internal fun removeBundleDefinitionOfBundle(bundleId: UUID) {
    BundleDefinitionsTable.deleteWhere { BundleDefinitionsTable.bundleId eq bundleId }
}

internal fun removeBundleDefinitionOfBundle(bundleId: UUID, bundleDefinition: Map<UUID, List<UUID>>) {
    bundleDefinition.entries.forEach { (applicationId, moduleIds) ->
        BundleDefinitionsTable.deleteWhere {
            (BundleDefinitionsTable.bundleId eq bundleId) and
            (BundleDefinitionsTable.applicationId eq applicationId) and
            (BundleDefinitionsTable.moduleId inList moduleIds)
        }
    }
}

internal fun Transaction.updateBundleDefinition(bundleId: UUID, bundleDefinition: BundleDefinition, modifier: UUID): Boolean {
    val flatBundleDefinition = bundleDefinition.entries.map { (key, value) -> value.map { key to it } }.flatten()
    val existingBundleDefinition = BundleDefinitionEntity.find {
        BundleDefinitionsTable.bundleId eq bundleId
    }.map {
        it.application.value to it.module.value
    }

    val toRemove = existingBundleDefinition.filterNot { it in flatBundleDefinition }.groupBy (
        keySelector = {it.first},
        valueTransform = { it.second }
    )
    val toAdd = flatBundleDefinition.filterNot { it in existingBundleDefinition }.groupBy (
        keySelector = {it.first},
        valueTransform = { it.second }
    )

    removeBundleDefinitionOfBundle(bundleId, toRemove)
    addBundleDefinition(bundleDefinition, bundleId, modifier )

    return toRemove.isNotEmpty() || toAdd.isNotEmpty()
}


internal fun validateBundleDefinition(bundleDefinition: BundleDefinition) {
    // apps and modules exist
    val existingApps = ApplicationEntity.find { ApplicationsTable.id inList bundleDefinition.keys }.map { it.id.value }
    val nonExistingApps = bundleDefinition.keys.filterNot { it in existingApps }

    val moduleIds = bundleDefinition.values.flatten().distinct()
    val existingMods = ModuleEntity.find { ModulesTable.id inList moduleIds }.map { it.id.value }
    val nonExistingMods = moduleIds.filterNot { it in existingMods }
    if (nonExistingApps.isNotEmpty() || nonExistingMods.isNotEmpty()) throw ApplicationException.NoSuchAppsOrModules(
        nonExistingApps.map { it.toString() },
        nonExistingMods.map { it.toString() }
    )
}
