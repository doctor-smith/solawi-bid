package org.solyton.solawi.bid.module.authentication.exception

sealed class AuthenticationException(override val message: String): Exception(message) {
    data object InvalidOrExpiredToken: AuthenticationException("Invalid or expired refresh token")
}