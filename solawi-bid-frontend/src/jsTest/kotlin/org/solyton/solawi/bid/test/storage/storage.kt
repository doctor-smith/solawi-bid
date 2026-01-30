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
import org.solyton.solawi.bid.module.permissions.data.Permissions
import org.solyton.solawi.bid.module.user.data.user.User
import org.solyton.solawi.bid.module.user.data.user.username
import kotlin.test.Test
import kotlin.test.assertEquals

@Markup
@Composable
fun TestStorage(): Storage<Application> {
    // val environment = getEnv()
    var application by remember {
        mutableStateOf<Application>(
            Application(
                environment = Environment(true,"DEV"),
                userData = User("", "", "", "",null,Permissions())
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

@Markup
@Composable
fun <Data> TestStorage(data: Data): Storage<Data>  {
    var storedData by remember {
        mutableStateOf<Data>(data)
    }

    return Storage<Data>(
        read = { storedData },
        write = {
                newApplication -> storedData = newApplication
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


    @OptIn(ComposeWebExperimentalTestsApi::class)
    @Test fun testGenericStorageTest() = runTest {
        composition {

            val storage = TestStorage("Johannes")

            assertEquals("Johannes", storage.read())

            val name = "Alfred"
            storage.write(name)

            val result = storage.read()

            assertEquals(name, result)
        }
    }
}
