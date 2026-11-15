package org.solyton.solawi.bid.module.user.service.password

import org.solyton.solawi.bid.module.user.data.api.PasswordRequirements
import org.solyton.solawi.bid.module.user.exception.UserManagementException
import java.util.Locale

object PasswordPolicy {
    private val datePattern = Regex(
        "(?<!\\d)(?:\\d{1,2}[./-]\\d{1,2}[./-](?:\\d{2}|\\d{4})|" +
            "(?:\\d{2}|\\d{4})[./-]\\d{1,2}[./-]\\d{1,2}|\\d{8})(?!\\d)"
    )

    private val blockedPasswords = setOf(
        "passwordpassword",
        "passwortpasswort",
        "123456789012345",
        "qwertyuiopasdfgh",
        "qwertzuiopasdfgh",
        "letmeinletmein",
        "adminadminadmin"
    )

    fun validate(password: String, username: String) {
        val normalizedPassword = password.lowercase(Locale.ROOT)
        val normalizedUsername = username.trim().lowercase(Locale.ROOT)
        val usernameLocalPart = normalizedUsername.substringBefore('@')
        val codePointLength = password.codePointCount(0, password.length)

        val violatesPolicy = codePointLength < PasswordRequirements.MINIMUM_LENGTH ||
            normalizedPassword in blockedPasswords ||
            password.all(Char::isDigit) ||
            password.toSet().size == 1 ||
            datePattern.containsMatchIn(password) ||
            normalizedPassword.contains(normalizedUsername) ||
            (usernameLocalPart.length >= 4 && normalizedPassword.contains(usernameLocalPart))

        if (violatesPolicy) throw UserManagementException.PasswordPolicyViolation
    }
}
