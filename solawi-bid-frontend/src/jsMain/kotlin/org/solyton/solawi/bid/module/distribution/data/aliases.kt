package org.solyton.solawi.bid.module.distribution.data

import org.solyton.solawi.bid.module.distribution.data.management.actions
import org.solyton.solawi.bid.module.distribution.data.management.modals

/**
 * Lazy-initialized variable representing a set of actions related to managing distribution points.
 *
 * Retrieves and provides actions that are defined in the `actions` object from the distribution data management module.
 */
val distributionManagementActions by lazy { actions }

/**
 * Lazy-initialized variable representing a collection of modals related to distribution management.
 *
 * Retrieves and provides the modals that are defined in the `modals` object from the distribution data management module.
 */
val distributionManagementModals by lazy { modals }
