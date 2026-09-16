package org.solyton.solawi.bid.module.application.action

import org.evoleq.math.Reader
import org.evoleq.math.contraMap
import org.evoleq.optics.lens.DeepSearch
import org.evoleq.optics.lens.times
import org.evoleq.optics.storage.Action
import org.evoleq.optics.storage.suffixed
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.module.application.data.ApplicationId
import org.solyton.solawi.bid.module.application.data.UpdateStandardApplicationContext
import org.solyton.solawi.bid.module.application.data.management.ApplicationManagement
import org.solyton.solawi.bid.module.application.data.management.availablePermissions
import org.solyton.solawi.bid.module.permission.data.api.ApiContext
import org.solyton.solawi.bid.module.permission.data.api.ContextIdValue
import org.solyton.solawi.bid.module.permissions.data.Context
import org.solyton.solawi.bid.module.permissions.data.Role
import org.solyton.solawi.bid.module.permissions.data.contexts
import org.solyton.solawi.bid.module.permissions.data.transform.toApiType
import org.solyton.solawi.bid.module.permissions.data.transform.toDomainType
import org.solyton.solawi.bid.module.permissions.data.roles as rolesLens

const val UPDATE_STANDARD_APPLICATION_CONTEXT = "UPDATE_STANDARD_APPLICATION_CONTEXT"

fun updateStandardApplicationContext(
    applicationId: ApplicationId,
    defaultContextId: ContextIdValue,
    roles: List<Role>,
    nameSuffix: String?
) : Action<ApplicationManagement, UpdateStandardApplicationContext, ApiContext> = Action(
    name = UPDATE_STANDARD_APPLICATION_CONTEXT.suffixed(nameSuffix),
    reader = Reader { UpdateStandardApplicationContext(applicationId, roles.map { it.toApiType() }) },
    endPoint = UpdateStandardApplicationContext::class,
    writer = availablePermissions *
            contexts *
            DeepSearch{
                context: Context -> context.contextId == defaultContextId.value
            } *
            rolesLens.set
            contraMap {
                context: ApiContext ->context.roles.map{role -> role.toDomainType()}
            }
)
