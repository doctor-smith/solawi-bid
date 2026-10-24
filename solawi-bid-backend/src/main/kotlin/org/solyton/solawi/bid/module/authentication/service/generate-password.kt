package org.solyton.solawi.bid.module.authentication.service

import java.security.SecureRandom

fun generateSecurePassword(length: Int = 16): String {
    require(length >= 1) { "Password length must be at least 1" }

    val chars =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
        "abcdefghijklmnopqrstuvwxyz" +
        "0123456789" +
        "!@#$%^&*()-_=+"

    val secureRandom = SecureRandom()

    return buildString(length) {
        repeat(length) {
            append(chars[secureRandom.nextInt(chars.length)])
        }
    }
}
