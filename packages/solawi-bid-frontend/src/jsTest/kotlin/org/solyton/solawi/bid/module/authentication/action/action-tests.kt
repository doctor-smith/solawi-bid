package org.solyton.solawi.bid.module.authentication.action

import org.jetbrains.compose.web.testutils.ComposeWebExperimentalTestsApi
import org.jetbrains.compose.web.testutils.runTest
import org.solyton.solawi.bid.application.serialization.installSerializers
import org.solyton.solawi.bid.application.ui.page.login.action.logoutAction
import org.solyton.solawi.bid.test.storage.TestStorage
import kotlin.test.Test

class AuthorizationActionTests {

    // todo:test improve
    @OptIn(ComposeWebExperimentalTestsApi::class)
    @Test
    fun logoutActionTest() = runTest{
        val name = "name"
        installSerializers()

        val logout = logoutAction

        composition {
            val storage = TestStorage()


        }
    }
}