package org.solyton.solawi.bid.module.db.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.jodatime.datetime
import java.util.*

typealias TokenEntity = Token
typealias TokensTable = Tokens

object Tokens : UUIDTable("tokens") {
    val userId = reference("user_id", Users)
    val refreshToken = uuid("refresh_token")
    val expiresAt = datetime("expires_at")
    /*
    val tokenValue = varchar("token_value", 255).uniqueIndex() // The actual token value
    val createdAt = datetime("created_at").default(DateTime.now()) // Created time in UTC
    val expiresAt = datetime("expires_at") // Expiration time in UTC
    val isRevoked = bool("is_revoked").default(false) // Flag to indicate if the token has been revoked
    val userAgent = varchar("user_agent", 255).nullable() // Optional user agent
    val ipAddress = varchar("ip_address", 45).nullable() //

     */
}

class Token(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<Token>(Tokens)

    var user by User referencedOn Tokens.userId
    var refreshToken by Tokens.refreshToken
    var expiresAt by Tokens.expiresAt
    /*
    var tokenValue by Tokens.tokenValue
    var createdAt by Tokens.createdAt
    var isRevoked by Tokens.isRevoked
    var useAgent by Tokens.userAgent
    var ipAccess by Tokens.ipAddress

     */
}
