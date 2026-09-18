package org.solyton.solawi.bid.module.application.action

import org.evoleq.math.DropInput
import org.evoleq.math.Reader
import org.evoleq.optics.storage.Action
import org.evoleq.optics.storage.suffixed
import org.solyton.solawi.bid.module.application.data.ApplicationId
import org.solyton.solawi.bid.module.application.data.FixApplicationRelatedContexts
import org.solyton.solawi.bid.module.application.data.FixModuleRelatedContexts
import org.solyton.solawi.bid.module.application.data.ModuleId
import org.solyton.solawi.bid.module.application.data.management.ApplicationManagement

const val FIX_APPLICATION_RELATED_CONTEXTS = "FIX_APPLICATION_RELATED_CONTEXTS"


fun fixApplicationRelatedContexts(
    applicationId: ApplicationId,
    nameSuffix: String? = null
) : Action<ApplicationManagement, FixApplicationRelatedContexts, Unit> = Action(
    name = FIX_APPLICATION_RELATED_CONTEXTS.suffixed(nameSuffix),
    reader = Reader { FixApplicationRelatedContexts(applicationId) },
    endPoint = FixApplicationRelatedContexts::class,
    writer = DropInput()
)


const val FIX_MODULE_RELATED_CONTEXTS = "FIX_MODULE_RELATED_CONTEXTS"

fun fixModuleRelatedContexts(
    moduleId: ModuleId,
    nameSuffix: String? = null
) : Action<ApplicationManagement, FixModuleRelatedContexts, Unit> = Action(
    name = FIX_MODULE_RELATED_CONTEXTS.suffixed(nameSuffix),
    reader = Reader { FixModuleRelatedContexts(moduleId) },
    endPoint = FixModuleRelatedContexts::class,
    writer = DropInput()
)
