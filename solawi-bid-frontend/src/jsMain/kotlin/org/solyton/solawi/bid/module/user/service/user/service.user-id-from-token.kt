package org.solyton.solawi.bid.module.user.service.user

import org.evoleq.math.Reader
import org.solyton.solawi.bid.module.user.data.user.User
import org.solyton.solawi.bid.module.user.service.getSubjectFromJwt

fun User.userIdFromToken(): String? = getSubjectFromJwt(accessToken)

val userIdFromToken: Reader<User, String?> = Reader { user -> user.userIdFromToken() }
