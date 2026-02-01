package org.solyton.solawi.bid.module.shares.action.api

import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.Contextual
import org.evoleq.ktorx.DbAction
import org.evoleq.ktorx.KlAction
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.math.MathDsl
import org.evoleq.math.x
import org.evoleq.uuid.toUuidOrNull
import org.jetbrains.exposed.sql.Transaction
import org.solyton.solawi.bid.module.shares.data.api.ImportShareSubscriptions
import org.solyton.solawi.bid.module.shares.data.api.ShareSubscriptions
import org.solyton.solawi.bid.module.shares.data.toApiType
import org.solyton.solawi.bid.module.shares.data.toInternalType
import org.solyton.solawi.bid.module.shares.service.ShareToImport
import org.solyton.solawi.bid.module.shares.service.importShareSubscriptions
import org.solyton.solawi.bid.module.user.schema.UserEntity
import org.solyton.solawi.bid.module.user.schema.UserProfileEntity
import org.solyton.solawi.bid.module.user.schema.UserProfilesTable
import org.solyton.solawi.bid.module.user.schema.UsersTable
import java.util.*

/**
 * Imports share subscription data into the system. This method processes a list of share subscriptions
 * for a given provider and fiscal year and associates them with user profiles based on usernames.
 * It handles situations where existing data may need to be overridden and ensures data consistency.
 *
 * The operations performed include:
 * - Fetching users with usernames specified in the subscription data.
 * - Retrieving associated user profiles for these users.
 * - Mapping and transforming the provided share subscriptions into a format suitable for internal processing.
 * - Executing the import operation, which updates or creates share subscription records as required.
 *
 * It operates in a transaction to ensure atomicity and consistency of database operations, rolling back changes in case of failure.
 *
 * @return A `KlAction` which provides the result of the import operation. The result encapsulates either a
 *         successful import represented by `ShareSubscriptions` or a failure with relevant messages or exceptions.
 */
@MathDsl
@Suppress("FunctionName", "MapGetWithNotNullAssertionOperator", "UnsafeCallOnNullableType")
fun ImportShareSubscriptions() = KlAction<Result<Contextual<ImportShareSubscriptions>>, Result<ShareSubscriptions>> { result ->
    DbAction { database -> result bindSuspend { contextual -> resultTransaction(database) {
        val userId = contextual.userId
        val data = contextual.data

        val userProfileIds = userProfilesByUsernames(data.shareSubscriptions.map { it.username }
            .toSet()).mapValues { it.value.id.value }

        importShareSubscriptions(
            data.override,
            UUID.fromString(data.providerId),
            UUID.fromString(data.fiscalYearId),
            data.shareSubscriptions.map { subscription ->
                ShareToImport(
                    UUID.fromString(subscription.shareOfferId),
                    userProfileIds[subscription.username]!!,
                    subscription.distributionPointId.toUuidOrNull(),
                    subscription.numberOfShares,
                    subscription.pricePerShare,
                    subscription.ahcAuthorized,
                    subscription.status.toInternalType(),
                    subscription.coSubscribers
                )
            },
            userId
        ).toApiType()
    } }  x database
} }

/**
 * Retrieves user profile entities mapped by usernames from the database.
 *
 * @param usernames a set of usernames for which user profiles need to be retrieved.
 * @return a map of usernames to their corresponding user profile entities.
 */
fun Transaction.userProfilesByUsernames(usernames: Set<String>): Map<String, UserProfileEntity>  {

    val users = UserEntity.find {
        UsersTable.username inList usernames
    }
    return UserProfileEntity.find {
        UserProfilesTable.userId inList users.map { it.id }
    }.associateBy(
        { it.user.username },
        { it }
    )
}
