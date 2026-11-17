package org.solyton.solawi.bid.module.user.service.password

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.Unit
import org.solyton.solawi.bid.module.user.exception.UserManagementException

class PasswordPolicyTests {
    private val username = "member@example.org"

    @Unit @Test
    fun `accepts a long passphrase with spaces`() {
        assertDoesNotThrow {
            PasswordPolicy.validate("Birken tanzen nachts am Fluss", username)
        }
    }

    @Unit @Test
    fun `rejects passwords shorter than fifteen characters`() {
        assertRejected("too-short")
    }

    @Unit @Test
    fun `rejects recognizable dates even inside a longer password`() {
        assertRejected("Reise-am-15.11.1980")
        assertRejected("Geburtstag-19801115")
    }

    @Unit @Test
    fun `rejects username components`() {
        assertRejected("member-hat-ein-passwort")
        assertRejected("member@example.org-ist-geheim")
    }

    @Unit @Test
    fun `rejects trivial numeric and repeated passwords`() {
        assertRejected("12345678901234567890")
        assertRejected("aaaaaaaaaaaaaaaaaaaa")
    }

    private fun assertRejected(password: String) {
        assertThrows(UserManagementException.PasswordPolicyViolation::class.java) {
            PasswordPolicy.validate(password, username)
        }
    }
}
