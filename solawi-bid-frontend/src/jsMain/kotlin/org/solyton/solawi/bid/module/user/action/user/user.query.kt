package org.solyton.solawi.bid.module.user.action.user

import org.evoleq.iql.data.Query
import org.evoleq.math.Reader
import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.solyton.solawi.bid.module.permissions.data.Permissions
import org.solyton.solawi.bid.module.user.data.Application
import org.solyton.solawi.bid.module.user.data.api.UserQuery
import org.solyton.solawi.bid.module.user.data.api.Users
import org.solyton.solawi.bid.module.user.data.managed.ManagedUser
import org.solyton.solawi.bid.module.user.data.managedUsers
import org.solyton.solawi.bid.module.user.data.transform.toDomainType

const val USER_QUERY = "USER_QUERY"

fun userQuery(
    query: Query,
    nameSuffix: String = ""
) = Action<Application, UserQuery, Users>(
    name = "$USER_QUERY$nameSuffix",
    reader = Reader { _: Application -> UserQuery(query) },
    endPoint = UserQuery::class,
    writer = managedUsers.set contraMap {
        users: Users -> users.all.map { ManagedUser(
        it.id,
        it.username,
        "",
        it.status.toDomainType(),
        Permissions())
        }
    }
)
