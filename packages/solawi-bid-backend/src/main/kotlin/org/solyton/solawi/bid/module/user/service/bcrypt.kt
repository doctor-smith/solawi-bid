package org.solyton.solawi.bid.module.user.service

import org.mindrot.jbcrypt.BCrypt


fun credentialsAreOK(plainPassword: String, hashedPassword: String) = BCrypt.checkpw(plainPassword, hashedPassword)

fun hashPassword(password: String): String {
    // Generate a salt and hash the password
    return BCrypt.hashpw(password, BCrypt.gensalt())
}
