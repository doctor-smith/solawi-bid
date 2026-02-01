package org.solyton.solawi.bid.application.data.transform.shares

import org.evoleq.optics.lens.Lens
import org.evoleq.optics.storage.ActionDispatcher
import org.evoleq.optics.storage.times
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.service.useI18nTransform
import org.solyton.solawi.bid.module.shares.data.management.ShareManagement

/**
 * A lazily initialized `Lens` providing functional access and transformations between
 * the `Application` structure and its nested `ShareManagement` element. The `Lens` is
 * particularly tailored to ensure the `actions` property in the `ShareManagement` object
 * is dynamically composed with the `actions` from the `Application` structure, achieving
 * immutability while enabling composable updates.
 *
 * This `Lens` allows:
 * - Retrieval of the `ShareManagement` part from an `Application` instance with a customized
 *   `actions` attribute.
 * - Updating the `ShareManagement` part back into the `Application` structure in a functional way.
 *
 * The `get` function extracts the `ShareManagement` object while merging its `actions`
 * with the `Application.actions` through a compositional operation. The `set` function
 * allows for seamless updates to the `ShareManagement` within the `Application`.
 */
val shareManagementIso: Lens<Application, ShareManagement> by lazy {
    Lens(
        get = { whole -> preShareManagementIso.get(whole).copy(actions = whole.actions * preShareManagementIso) },
        set = preShareManagementIso.set
    )
}

/**
 * Provides a lazily initialized `Lens` that focuses on the `ShareManagement` component within an `Application`.
 *
 * This lens enables functional access and transformation of the `ShareManagement` properties within the
 * `Application` data structure. It encapsulates the immutability of the application while allowing partial
 * updates and retrieval.
 *
 * The `get` operation extracts a `ShareManagement` object from an `Application`, applying specific
 * transformations to the `environment` property for localization through the `useI18nTransform` function.
 *
 * The `set` operation constructs a new `Application` with updated `ShareManagement` properties, ensuring
 * immutability. It replaces the relevant fields of the `Application` with the corresponding values from the
 * updated `ShareManagement` object.
 *
 * The `ShareManagement` instance represents configurations and data necessary for sharing functionalities,
 * such as context, modals, i18n configurations, and lists of share-related entities (types, offers, and subscriptions).
 */
private val preShareManagementIso: Lens<Application, ShareManagement> by lazy {
    Lens(
        get = { whole -> ShareManagement(
            context = whole.context,
            actions = ActionDispatcher { },
            deviceData = whole.deviceData,
            modals = whole.modals,
            i18n = whole.i18N,
            environment = whole.environment.useI18nTransform(),
            shareTypes = whole.shareTypes,
            shareOffers = whole.shareOffers,
            shareSubscriptions = whole.shareSubscriptions
        ) },
        set = { part -> { whole -> whole.copy(
            context = part.context,
            modals = part.modals,
            i18N = part.i18n,
            shareTypes = part.shareTypes,
            shareOffers = part.shareOffers,
            shareSubscriptions = part.shareSubscriptions
        ) } }
    )
}
