package org.solyton.solawi.bid.module.shares.action

import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.solyton.solawi.bid.module.shares.data.toDomainType
import org.solyton.solawi.bid.module.shares.data.api.ApiShareSubscriptions
import org.solyton.solawi.bid.module.shares.data.api.ImportShareSubscription
import org.solyton.solawi.bid.module.shares.data.api.ImportShareSubscriptions
import org.solyton.solawi.bid.module.shares.data.management.ShareManagement
import org.solyton.solawi.bid.module.shares.data.management.shareSubscriptions


/**
 * Imports share subscriptions into the system, optionally overriding existing data.
 *
 * @param override Determines whether to override existing share subscriptions with the same identifiers. Defaults to false.
 * @param providerId The unique identifier of the provider executing the subscription import.
 * @param fiscalYearId The identifier of the fiscal year to which the subscriptions belong.
 * @param shareSubscriptionsToImport A list of `ImportShareSubscription` objects containing the share subscriptions to be imported.
 * @param nameSuffix An optional suffix appended to the action's name for identification purposes. Defaults to an empty string.
 * @return An `Action` instance responsible for handling the import of share subscriptions, defining the input, endpoint, and writer needed for the operation.
 */
fun importShareSubscriptions(
    override: Boolean = false,
    providerId: String,
    fiscalYearId: String,
    shareSubscriptionsToImport: List<ImportShareSubscription>,
    nameSuffix: String = ""
): Action<ShareManagement, ImportShareSubscriptions, ApiShareSubscriptions> = Action(
    name = "ImportShareSubscriptions$nameSuffix",
    reader = { _: ShareManagement ->
        ImportShareSubscriptions(
            override = override,
            providerId = providerId,
            fiscalYearId = fiscalYearId,
            shareSubscriptions = shareSubscriptionsToImport
        )
    },
    endPoint = ImportShareSubscriptions::class,
    writer = shareSubscriptions.set contraMap { s: ApiShareSubscriptions -> s.toDomainType()}
)
