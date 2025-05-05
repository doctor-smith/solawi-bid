package org.solyton.solawi.bid.test.storage

import androidx.compose.runtime.*
import org.evoleq.compose.Markup
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.testutils.ComposeWebExperimentalTestsApi
import org.jetbrains.compose.web.testutils.runTest
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.env.Environment
import org.solyton.solawi.bid.application.data.userData
import org.solyton.solawi.bid.module.user.data.User
import org.solyton.solawi.bid.module.user.data.username
import kotlin.test.Test
import kotlin.test.assertEquals

@Markup
@Composable
fun TestStorage(): Storage<Application> {
    var pulse by remember { mutableStateOf<Int>(0) }
    // val environment = getEnv()
    var application by remember {
        mutableStateOf<Application>(
            Application(
                environment = Environment(true,"DEV"),
                userData = User("", "", "", "",)
            )
        )
    }

    return Storage<Application>(
        read = { application },
        write = {
                newApplication -> application = newApplication
        }
    )
}

class TestStorageTest {
    @OptIn(ComposeWebExperimentalTestsApi::class)
    @Test fun testStorageTest() = runTest {
        composition {

            val storage = TestStorage()

            val name = "Alfred"
            (storage * userData * username).write(name)

            val result = (storage * userData * username).read()

            assertEquals(name, result)
        }
    }
}