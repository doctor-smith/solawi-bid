package org.solyton.solawi.bid.module.authentication.action

import io.ktor.server.request.*
import io.ktor.util.*
import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.result.Result
import org.evoleq.math.x
import org.evoleq.util.Action
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.joda.time.DateTime
import org.solyton.solawi.bid.application.environment.JWT
import org.solyton.solawi.bid.module.authentication.data.api.IsLoggedIn
import org.solyton.solawi.bid.module.authentication.data.api.LoggedInAs
import org.solyton.solawi.bid.module.authentication.service.generateAccessToken
import org.solyton.solawi.bid.module.authentication.service.isUuid
import org.solyton.solawi.bid.module.db.schema.TokenEntity
import org.solyton.solawi.bid.module.db.schema.TokensTable
import java.util.UUID

@KtorDsl
@Suppress("FunctionName")
fun  IsLoggedIn(jwt: JWT): Action<Result<LoggedInAs>> = Action { base -> //{
        val refreshToken = base.call.receive<IsLoggedIn>().refreshToken
        resultTransaction(base.database) {
            when{
                !refreshToken.isUuid() -> NotLoggedIn()
                else -> {
                    val token = TokenEntity.find { TokensTable.refreshToken eq UUID.fromString(refreshToken) }.firstOrNull()
                    if(token != null) {
                        val isExpired = token.expiresAt < DateTime.now()
                        if(isExpired) {
                            TokensTable.deleteWhere { TokensTable.refreshToken eq UUID.fromString(refreshToken) }
                            NotLoggedIn()
                        } else {
                            LoggedInAs(
                                token.user.username,
                                generateAccessToken(token.user.id.toString(), jwt),
                                refreshToken
                            )
                        }
                    } else {
                        NotLoggedIn()
                    }
                }

            }
        } x base
}

@Suppress("FunctionName")
fun NotLoggedIn(): LoggedInAs = LoggedInAs("","","")