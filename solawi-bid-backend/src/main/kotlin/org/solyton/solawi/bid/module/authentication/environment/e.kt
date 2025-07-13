package org.solyton.solawi.bid.module.authentication.environment

interface JwtEnv {
    val jwt: JWT
}

data class JWT(
    val domain:String, // issuer
    val audience: String,
    val realm: String,
    val secret: String
)
