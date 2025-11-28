package org.solyton.solawi.bid.application.data.transform

import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.env.Environment
import org.solyton.solawi.bid.application.data.transform.user.userIso
import kotlin.test.Test

class UserIsoTest {
    @Test fun userIsoGetTest() {
        val application = Application(
            Environment()
        )

        //val userModule =
        userIso.get(application)
    }

    @Test fun userIsoSetTest() {
        val application = Application(
            Environment()
        )

        // val result = userIso.set

        //val userModule =
        userIso.get(application)
    }
}
