package org.evoleq.ktorx.client

import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.Return
import org.evoleq.ktorx.result.apply
import org.evoleq.ktorx.result.on
import org.evoleq.math.Writer
import org.evoleq.math.dispatch
import org.evoleq.math.state.KlState
import org.evoleq.math.state.State
import org.evoleq.math.state.runOn
import org.evoleq.math.x
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.testutils.ComposeWebExperimentalTestsApi
import org.jetbrains.compose.web.testutils.runTest
import kotlin.test.Test
import kotlin.test.assertEquals


class DispatchResultTest {
    data class Whole(val number: Int, val name: String, val error: String? = null)

    var whole = Whole(0,"")

    val storage = Storage<Whole> (
        read = {whole},
        write = {w -> whole = w}
    )

    @Test
    fun dispatchResult() {
        val name = "name"
        val result = Result.Success(name)
        val writer: Writer< Whole, String> = {n -> {w-> w.copy(name= n)} }

        val applicative = Result.Return((storage * writer).dispatch())
        // val applicative = Result.Return{n: String -> (storage * writer).dispatch(n)}
        val u = applicative.apply() on result

        assertEquals(Whole(0,name), whole)
    }

    @OptIn(ComposeWebExperimentalTestsApi::class)
    @Test
    fun dispatchResultWithPossibleFailure() = runTest{
        val name = "name"
        val result = Result.Success(name)
        val successWriter: Writer<Whole, String> = { n -> { w-> w.copy(name= n)} }
        val failureWriter: Writer<Whole, String> = { e -> { w-> w.copy(error = e)} }

        val success = {storage: Storage<Whole> -> Result.Return((storage * successWriter).dispatch())}
        val failure = {storage: Storage<Whole> -> Result.Return((storage * failureWriter).dispatch())}

        val DispatchState = KlState<Storage<Whole>,Result<String>, Result<Unit>> {
            r -> State { storage ->
                when(r) {
                    is Result.Success -> success(storage).apply() on r
                    is Result.Failure.Message -> failure(storage).apply() on Result.Return(r.value)
                    is Result.Failure.Exception -> failure(storage).apply() on Result.Return(r.value.message?: "")
                } x storage
            }
        }
        val u = DispatchState(result) runOn storage
        assertEquals(Whole(0,name), whole)

        val e = DispatchState(Result.Failure.Message("error")) runOn storage
        assertEquals(Whole(0,name, "error"), whole)
    }
}
