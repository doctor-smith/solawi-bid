package org.solyton.solawi.bid.application.data.transform.distribution

import org.evoleq.optics.lens.Lens
import org.evoleq.optics.storage.ActionDispatcher
import org.evoleq.optics.storage.times
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.service.useI18nTransform
import org.solyton.solawi.bid.module.distribution.data.management.DistributionManagement

/**
 * Provides a lazily initialized `Lens` for managing the `DistributionManagement` aspect
 * of an `Application`. This `Lens` enables access and transformation of the
 * `DistributionManagement` part of the `Application`, maintaining immutability.
 *
 * The `get` function retrieves the `DistributionManagement` object from the `Application`,
 * augmenting its `actions` property by composing it with the pre-defined `Lens` (`preDistributionManagementIso`).
 * The `set` function applies transformations to the `DistributionManagement` part within the `Application`.
 */
val distributionManagementIso: Lens<Application, DistributionManagement> by lazy {
    Lens(
        get = { whole -> preDistributionManagementIso.get(whole).copy(actions = whole.actions * preDistributionManagementIso) },
        set = preDistributionManagementIso.set
    )
}

/**
 * A lens that provides composable access and transformation functionalities for the
 * `DistributionManagement` instance within an `Application` object. This lens enables
 * the retrieval and immutably updated composition of `DistributionManagement` properties
 * with its associated components, including `context`, `modals`, `i18n`, and
 * `distributionPoints`.
 *
 * This lens:
 * 1. Retrieves the focused `DistributionManagement` from the larger `Application` object.
 * 2. Creates transformations for immutably updating an `Application` with a modified
 *    `DistributionManagement`.
 */
private val preDistributionManagementIso: Lens<Application, DistributionManagement> by lazy {
    Lens(
        get = { whole -> DistributionManagement(
            context = whole.context,
            actions = ActionDispatcher { },
            deviceData = whole.deviceData,
            modals = whole.modals,
            i18n = whole.i18N,
            environment = whole.environment.useI18nTransform(),
            distributionPoints = whole.distributionPoints
        ) },
        set = { part -> { whole -> whole.copy(
            context = part.context,
            modals = part.modals,
            i18N = part.i18n,
            distributionPoints = part.distributionPoints
        ) } }
    )
}
