package org.solyton.solawi.bid.module.shares.data

import org.solyton.solawi.bid.module.shares.data.management.actions
import org.solyton.solawi.bid.module.shares.data.management.modals
import kotlin.getValue

/**
 * Lazily initializes the share-related actions. The variable is intended to handle or provide
 * operations or functionalities related to shares within the system.
 */
val shareManagementActions by lazy { actions }

/**
 * Provides an instance of `modals` lazily initialized on first access.
 *
 * The `shareModals` variable is designed to encapsulate the modal components related to share
 * transactions, including operations like subscriptions, offers, and types within the domain
 * of share management. This ensures that modal-related configurations or access points are
 * initialized only when needed, enhancing performance and memory utilization.
 */
val shareManagementModals by lazy { modals }
