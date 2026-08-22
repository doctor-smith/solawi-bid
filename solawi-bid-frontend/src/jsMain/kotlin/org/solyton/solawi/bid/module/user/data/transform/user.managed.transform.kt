package org.solyton.solawi.bid.module.user.data.transform

import org.solyton.solawi.bid.module.permissions.data.Permissions
import org.solyton.solawi.bid.module.user.data.api.ApiUser
import org.solyton.solawi.bid.module.user.data.api.ApiUserStatus
import org.solyton.solawi.bid.module.user.data.managed.ManagedUser
import org.solyton.solawi.bid.module.user.data.status.UserStatus

fun ApiUser.toNewManagedType(): ManagedUser = ManagedUser(
    id,
    username,
    "",
    status = status.toDomainType(),
    permissions = Permissions()
)

fun ApiUserStatus.toDomainType(): UserStatus = when(this){
    ApiUserStatus.REGISTERED -> UserStatus.REGISTERED
    ApiUserStatus.DISABLED -> UserStatus.DISABLED
    ApiUserStatus.ACTIVE -> UserStatus.ACTIVE
    ApiUserStatus.INVITED -> UserStatus.INVITED
    ApiUserStatus.PENDING -> UserStatus.PENDING
}

fun UserStatus.toApiType(): ApiUserStatus = when(this) {
    UserStatus.REGISTERED -> ApiUserStatus.REGISTERED
    UserStatus.DISABLED -> ApiUserStatus.DISABLED
    UserStatus.ACTIVE -> ApiUserStatus.ACTIVE
    UserStatus.INVITED -> ApiUserStatus.INVITED
    UserStatus.PENDING -> ApiUserStatus.PENDING
}
