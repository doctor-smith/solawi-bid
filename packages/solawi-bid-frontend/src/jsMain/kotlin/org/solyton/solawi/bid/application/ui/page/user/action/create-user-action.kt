package org.solyton.solawi.bid.application.ui.page.user.action

import org.evoleq.compose.Markup
import org.evoleq.math.Reader
import org.evoleq.optics.storage.Action
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.module.user.data.api.CreateUser
import org.solyton.solawi.bid.module.user.data.api.User

@Markup
fun createUser(user: CreateUser) =
    Action<Application, CreateUser, User>(
        name = "CreateUser",
        reader = Reader { _: Application -> user },
        endPoint = CreateUser::class,
        // todo:dev improve
        writer = {_: User -> {app: Application -> app}}
    )