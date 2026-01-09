package org.solyton.solawi.bid.module.user.service.user

import org.jetbrains.exposed.sql.Transaction
import java.util.*

/**
 * Deletes a user from the system and removes all associated records, connections, and references.
 * This includes associations with organizations, role contexts, applications/modules, and user profiles.
 *
 * @param userId The unique identifier of the user to be deleted.
 * @return The unique identifier of the deleted user.
 */
@Suppress("UNUSED_PARAMETER")
fun Transaction.deleteUser(userId: UUID): UUID {
    // Recognize that the user is connected to several other entities
    // 1. Organizations -> as member
    // 2. Role Contexts
    // 3. Applications / Modules
    // 4. User Profiles
    TODO("needs concept to handle deletion w.r.t. dsgvo and laws forcing to keep data for legal reasons" )
}

/**
 * Deactivates a user in the system based on their unique identifier.
 *
 * @param uuid The unique identifier of the user to be deactivated.
 */
@Suppress("UNUSED_PARAMETER")
fun Transaction.deactivateUser(uuid: UUID): UUID {
    // 1. Deactivate all apps and modules (PAUSE it!)
    // 2. Set user.active to false
    TODO()
}
