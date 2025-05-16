package org.solyton.solawi.bid.module.authentication.service


import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.solyton.solawi.bid.application.environment.JWT
import java.util.*


private const val ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 60 * 24 // 15 minutes

// Generate access token
fun generateAccessToken(userId: String, jwt: JWT): String {
    return Jwts.builder()
        .setSubject(userId)
        .setIssuer(jwt.domain)
        .setAudience(jwt.audience)
        .setExpiration(Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
        .signWith(Keys.hmacShaKeyFor(jwt.secret.toByteArray()), SignatureAlgorithm.HS256)
        .compact()
}

fun generateRefreshToken():UUID = UUID.randomUUID()
