package org.solyton.solawi.bid.module.authentication.data.api

import kotlinx.serialization.Serializable

typealias ApiIdentifier = Identifier
typealias ApiLogin = Login
typealias ApiLogout = Logout
typealias ApiLoggedIn = LoggedIn
typealias ApiRefreshToken = RefreshToken
typealias ApiAccessToken = AccessToken


@Serializable
data class Identifier(
    val value: String
)

@Serializable
data class Login(
    val username: String,
    val password: String
)

@Serializable
data class Logout(
    val refreshToken: String
)

@Serializable
data class LoggedIn(
    val sessionId: String,
    val accessToken: String,
    val refreshToken: String
)

@Serializable
data class RefreshToken(
    val username: String,
    val refreshToken: String
)

@Serializable
data class AccessToken(
    val accessToken: String
)

@Serializable
data class IsLoggedIn(
    val refreshToken: String
)
@Serializable
data class LoggedInAs(
    val username: String,
    val accessToken: String,
    val refreshToken: String,
)


/*
@Serializable
data class Session(
    val sessionId: String,
    val userId: String,
    val startTime: DateTime,
    val endTime: DateTime?,
    val isActive: Boolean,
    val ipAddress: String?,
    val userAgent: String?,
    val createdAt: DateTime,
    val updatedAt: DateTime
)

 */