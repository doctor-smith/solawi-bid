package org.solyton.solawi.bid.module.user.service.bcrypt

import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.Api


class GenHash {
    @Api
    @Test
    fun genHashTest() {

        println(hashPassword(""))
    }
}
