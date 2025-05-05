package org.solyton.solawi.bid.module.user

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.Api
import org.solyton.solawi.bid.module.user.service.hashPassword

class GenHash {
    //@Api@Test
    fun genHashTest() {

        println(hashPassword("place-strong-password"))
    }
}