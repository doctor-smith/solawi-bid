package org.solyton.solawi.bid.module.authentication.action

import io.ktor.util.*
import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.math.MathDsl
import org.evoleq.math.x
import org.evoleq.util.DbAction
import org.evoleq.util.KlAction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.solyton.solawi.bid.application.environment.JWT
import org.solyton.solawi.bid.module.authentication.data.api.*
import org.solyton.solawi.bid.module.authentication.exception.AuthenticationException
import org.solyton.solawi.bid.module.authentication.service.generateAccessToken
import org.solyton.solawi.bid.module.authentication.service.generateRefreshToken
import org.solyton.solawi.bid.module.authentication.service.isUuid
import org.solyton.solawi.bid.module.db.schema.Token
import org.solyton.solawi.bid.module.db.schema.Tokens
import org.solyton.solawi.bid.module.db.schema.User
import org.solyton.solawi.bid.module.db.schema.Users
import org.solyton.solawi.bid.module.user.exception.UserManagementException
import org.solyton.solawi.bid.module.user.service.credentialsAreOK
import java.util.*


/**
 * secret, etc, is to be provided by the environment
 */
@MathDsl
@Suppress("FunctionName")
fun Login(jwt: JWT) = KlAction<Result<Login>, Result<LoggedIn>> {
    result -> DbAction {
        database -> result bindSuspend { data ->   resultTransaction(database) {
            login(data, jwt)
        } }  x database
    }
}
@MathDsl
@Suppress("FunctionName")
fun Refresh(jwt: JWT) = KlAction<Result<RefreshToken>, Result<AccessToken>> {
    result -> DbAction {
        database -> result bindSuspend   { data ->resultTransaction(database) {
            val refreshToken = data.refreshToken
            if(!validateRefreshToken(refreshToken))
                throw AuthenticationException.InvalidOrExpiredToken
            val user = User.find{ Users.username eq data.username }.firstOrNull()
                ?: throw UserManagementException.UserDoesNotExist.Username(data.username)

            val newAccessToken = generateAccessToken(user.id.value.toString(), jwt)
                AccessToken(newAccessToken)
        } } x database
    }
}

fun Transaction.login(login: Login, jwt: JWT): LoggedIn {
    val user = User.find{ Users.username eq login.username }.firstOrNull()
        ?: throw UserManagementException.UserDoesNotExist.Username(login.username)

    if(!credentialsAreOK(login.password, user.password))
        throw UserManagementException.WrongCredentials

    val accessToken = generateAccessToken(user.id.value.toString(), jwt)
    val refreshToken = generateAndStoreRefreshToken(user)
    val session = "" // TODO(generate and store session)

    return LoggedIn(session, accessToken, refreshToken)
}


// Generate refresh token and save to the database
fun Transaction.generateAndStoreRefreshToken(user: User): String {
    val refreshToken = generateRefreshToken()
    return transaction {
        Token.new {
            this.user = user
            this.refreshToken = refreshToken
            expiresAt = DateTime.now().plusDays(7)
        }
        refreshToken.toString()
     }
}

// Validate refresh token
fun Transaction.validateRefreshToken(refreshToken: String): Boolean {
    return transaction {

        Token.find { Tokens.refreshToken eq UUID.fromString(refreshToken) }
            .singleOrNull()
            ?.let {
                DateTime.now() < it.expiresAt // Ensure token is not expired
            } ?: false

    }
}

@MathDsl
@Suppress("FunctionName")
fun LogoutUser() = KlAction<Result<Logout>, Result<Unit>> {
    result -> DbAction {
        database -> result bindSuspend { data ->   resultTransaction(database) {
            revokeRefreshToken(data.refreshToken)
        } }  x database
    }
}

// Revoke a refresh token
fun revokeRefreshToken(refreshToken: String) {
    if(refreshToken.isUuid()) Tokens.deleteWhere { Tokens.refreshToken eq UUID.fromString(refreshToken) }
}
