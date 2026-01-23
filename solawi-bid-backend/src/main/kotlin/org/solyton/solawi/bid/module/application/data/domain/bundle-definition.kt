package org.solyton.solawi.bid.module.application.data.domain

import java.util.*

/**
 * Maps application ids to sets of module ids
 */
data class BundleDefinition(
    val map: HashMap<UUID, Set<UUID>>
) : Map<UUID, Set<UUID>> by map
