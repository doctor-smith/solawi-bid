package org.solyton.solawi.bid.application.service

import org.solyton.solawi.bid.module.user.data.user.User

fun User.seemsToBeLoggerIn(): Boolean = accessToken != ""  && refreshToken!= ""

fun User.seemsNotToBeLoggerIn(): Boolean = !seemsToBeLoggerIn()
