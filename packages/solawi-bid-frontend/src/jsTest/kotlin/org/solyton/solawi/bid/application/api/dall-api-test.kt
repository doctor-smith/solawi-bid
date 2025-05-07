package org.solyton.solawi.bid.application.api

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import org.evoleq.ktorx.result.Result
import org.evoleq.math.Writer
import org.evoleq.math.state.runOn
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.testutils.ComposeWebExperimentalTestsApi
import org.jetbrains.compose.web.testutils.runTest
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.env.Environment
import org.solyton.solawi.bid.application.data.modals
import org.solyton.solawi.bid.test.storage.TestStorage
import kotlin.test.Test
import kotlin.test.assertTrue

// todo:improve
class CallApiTest {

    @OptIn(ComposeWebExperimentalTestsApi::class)
    @Test fun dispatchTest() = runTest {
        var s : Int = 0

        composition {
            var x by remember { mutableStateOf(0) }
            val storage = TestStorage()
            val writer = Writer<Application, Environment> { env -> { app: Application -> app.copy(environment = env) } }
            val failure = Result.Failure.Message("failure message")



            launch {

                val result = Dispatch(writer)(failure) runOn storage
                val size = (storage * modals).read().size
                s = size
                x = 1
                assertTrue { result.first == failure }
                assertTrue { s == 1 }

            }
        }}
}