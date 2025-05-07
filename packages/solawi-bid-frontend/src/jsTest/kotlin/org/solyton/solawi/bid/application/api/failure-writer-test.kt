package org.solyton.solawi.bid.application.api

import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.Return
import org.evoleq.ktorx.result.apply
import org.evoleq.ktorx.result.on
import org.evoleq.math.dispatch
import org.evoleq.math.write
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.testutils.ComposeWebExperimentalTestsApi
import org.jetbrains.compose.web.testutils.runTest
import org.solyton.solawi.bid.application.data.failure.Failure
import org.solyton.solawi.bid.application.data.modals
import org.solyton.solawi.bid.test.storage.TestStorage
import kotlin.test.Test
import kotlin.test.assertEquals

class FailureWriterTest {
    @OptIn(ComposeWebExperimentalTestsApi::class)
    @Test fun failureWriterTest() = runTest {


        composition {
            val storage = TestStorage()

            storage.failureWriter().write(Failure("failure")) on Unit

            val modalList = (storage * modals).read()
            assertEquals(1, modalList.size)

            storage.failureWriter().dispatch(Failure("failure"))

            val modalList1 = (storage * modals).read()
            assertEquals(2, modalList1.size)



            Result.Return((storage.failureWriter()).dispatch()).apply() on Result.Success(Failure("f"))
            assertEquals(3, (storage * modals).read().size)
        }
    }

}