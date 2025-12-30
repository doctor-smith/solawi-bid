package org.solyton.solawi.bid.module.user.action.permission

import org.evoleq.math.Reader
import org.evoleq.math.contraMap
import org.evoleq.optics.lens.FirstBy
import org.evoleq.optics.lens.times
import org.evoleq.optics.storage.Action
import org.solyton.solawi.bid.module.permission.data.api.PutUserRoleContext
import org.solyton.solawi.bid.module.permission.data.api.UserContext
import org.solyton.solawi.bid.module.permissions.data.contexts
import org.solyton.solawi.bid.module.permissions.data.transform.toDomainType
import org.solyton.solawi.bid.module.user.data.Application
import org.solyton.solawi.bid.module.user.data.managed.permissions
import org.solyton.solawi.bid.module.user.data.managedUsers

/**
 * Updates the role context for a specific user in the application by assigning new role IDs to the user
 * in the specified context.
 *
 * @param userId The ID of the user whose roles are being updated.
 * @param contextId The ID of the context where roles are being assigned.
 * @param userRoleIds A list of role IDs to be assigned to the user for the given context.
 * @param nameSuffix An optional suffix to append to the action name for customization. Default is an empty string.
 * @return An action that updates the user's role context and represents the operation as a transformation
 *         from a base `Application` to a `UserContext` through a `PutUserRoleContext`.
 */
fun putUsersRoleContext(
    userId: String,
    contextId: String,
    userRoleIds: List<String>,
    nameSuffix: String = ""
): Action<Application, PutUserRoleContext, UserContext> = Action<Application, PutUserRoleContext, UserContext>(
    name = "PutUsersRoleContext$nameSuffix",
    reader = Reader { _: Application -> PutUserRoleContext(userId, contextId, userRoleIds) },
    endPoint = PutUserRoleContext::class,
    writer = (managedUsers *
        FirstBy { it.id == userId } *
        permissions *
        contexts *
        FirstBy { ctx -> ctx.contextId == contextId }
    ).set contraMap {
        userContext: UserContext -> userContext.context.toDomainType()
    },
)
