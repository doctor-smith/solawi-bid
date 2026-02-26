package org.solyton.solawi.bid.module.user.data.transform

import org.solyton.solawi.bid.module.permissions.data.Permissions
import org.solyton.solawi.bid.module.user.data.api.ApiUser
import org.solyton.solawi.bid.module.user.data.managed.ManagedUser

fun ApiUser.toNewManagedType(): ManagedUser = ManagedUser(id, username, "", permissions = Permissions() )
