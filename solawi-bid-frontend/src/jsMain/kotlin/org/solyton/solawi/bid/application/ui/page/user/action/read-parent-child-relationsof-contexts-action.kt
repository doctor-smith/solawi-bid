package org.solyton.solawi.bid.application.ui.page.user.action

import org.evoleq.math.Reader
import org.evoleq.optics.storage.Action
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.module.permission.data.api.ParentChildRelationsOfContext
import org.solyton.solawi.bid.module.permission.data.api.ReadParentChildRelationsOfContexts

const val READ_PARENT_CHILD_RELATIONS_OF_CONTEXT = "ReadParentChildRelationsOfContexts"

fun readParentChildRelationsOfContextsAction(nameSuffix: String = ""): Action<
    Application,
    ReadParentChildRelationsOfContexts,
    ParentChildRelationsOfContext
> = Action(
    name = "$READ_PARENT_CHILD_RELATIONS_OF_CONTEXT$nameSuffix",
    reader = Reader { app: Application -> ReadParentChildRelationsOfContexts(
            app.userData.permissions.contexts.map { context -> context.contextId }
    ) },
    endPoint = ReadParentChildRelationsOfContexts::class,
    writer = {_: ParentChildRelationsOfContext -> {a: Application -> a}}
)
