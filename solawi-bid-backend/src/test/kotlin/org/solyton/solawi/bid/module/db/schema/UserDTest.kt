package org.solyton.solawi.bid.module.db.schema


import org.evoleq.exposedx.test.runSimpleH2Test
import org.evoleq.uuid.UUID_ZERO
import org.junit.Test
import org.solyton.solawi.bid.Schema
import org.solyton.solawi.bid.module.user.schema.User
import org.solyton.solawi.bid.module.user.schema.Users
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


class UserDTest {
    @Schema@Test
    fun readAndWriteUser() = runSimpleH2Test(Users){
        User.new {
            username = "name"
            password = "pw"
            createdBy = UUID_ZERO
        }

        val  user = User.find {
            Users.username eq "name"
        }.firstOrNull()

        assertNotNull(user)

        val newUsername = "newName"

        User.findByIdAndUpdate(user.id.value) {
            it.username = newUsername
        }

        val  user1 = User.find {
            Users.id eq user.id
        }.first()

        assertEquals(newUsername, user1.username)

    }
}
