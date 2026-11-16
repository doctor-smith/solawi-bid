package org.solyton.solawi.bid.state

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Page
import kotlinx.coroutines.runBlocking
import org.evoleq.math.state.State
import org.evoleq.math.state.runOn
import org.evoleq.math.state.times
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.solyton.solawi.bid.StdTestUser
import org.solyton.solawi.bid.TestUserContext
import org.solyton.solawi.bid.TestWorkflow
import org.solyton.solawi.bid.module.values.Password
import org.solyton.solawi.bid.module.values.Username
import java.lang.reflect.Proxy
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CancellationException

/** Exercise the real State algebra and adapters without launching Playwright. */
class E2eCompositionTest {
    @TempDir
    lateinit var directory: Path

    @Test
    fun `composition supplies input and context only at execution and preserves surrounding data`() = runBlocking {
        val state = initial()
        val calls = mutableListOf<String>()
        val arrow = workflow { userContext ->
            calls += "constructed"
            object : TestWorkflow<Int, Int> {
                override val context = userContext
                override fun TestUserContext.runWith(input: Int): Int {
                    assertSame(state.userContext, this)
                    calls += "executed"
                    return input + 1
                }
            }
        } reportedAs "Increment"
        val data = "retain me" to 41
        val retained: E2eStep<Pair<String, Int>, Pair<Pair<String, Int>, Int>> =
            arrow.retaining({ it.second }, { original, result -> original to result })
        val scenario = State<E2eState, Pair<String, Int>>(data) * retained
        assertTrue(calls.isEmpty())

        val (result, next) = scenario runOn state
        assertSame(data, result.first)
        assertEquals(42, result.second)
        assertSame(state, next)
        assertEquals(listOf("constructed", "executed"), calls)
        state.report.writeTo(directory.resolve("success.md"))
        assertTrue(Files.readString(directory.resolve("success.md")).contains("Increment | PASS"))
    }

    @Test
    fun `retaining and reporting propagate the updated state`() = runBlocking {
        val state = initial()
        val changed = state.copy(report = E2eReport("Next report"))
        val arrow: E2eStep<Int, Int> = { input -> State { input + 1 to changed } }
        val retained: E2eStep<String, Pair<String, Int>> =
            (arrow reportedAs "Change state").retaining(String::length, ::Pair)
        val (result, next) = retained("abc")(state)
        assertEquals("abc" to 4, result)
        assertSame(changed, next)
    }

    @Test
    fun `switch changes the session and preserves payload identity and cleanup ownership`() = runBlocking {
        var closed = 0
        val page = stub<Page>()
        val context = stub<BrowserContext> { method ->
            when (method) {
                "newPage" -> page
                "close" -> { closed++; null }
                else -> error("Unexpected browser-context call: $method")
            }
        }
        val browser = stub<Browser> { method ->
            check(method == "newContext")
            context
        }
        E2eEnvironment(browser).use { environment ->
            val state = initial().copy(environment = environment)
            val user = StdTestUser(Username("member"), Password("unused"))
            val payload = user to "important data"
            val (result, next) = SwitchUser<Pair<StdTestUser, String>> { it.first }(payload)(state)
            assertSame(payload, result)
            assertSame(user, next.userContext.user)
            assertSame(context, next.userContext.context)
            assertSame(state.report, next.report)
            assertSame(environment, next.environment)
            assertEquals(0, closed)
        }
        assertEquals(1, closed)
    }

    @Test
    fun `all independent checks execute with the same payload and success passes it through`() = runBlocking {
        val state = initial()
        val payload = listOf("retained")
        val seen = mutableListOf<List<String>>()
        val passing = verifyAll(
            expect<List<String>>("First") { seen += it },
            expect<List<String>>("Second") { seen += it }
        )
        val (result, next) = passing(payload)(state)
        assertSame(payload, result)
        assertSame(state, next)
        assertEquals(2, seen.size)
        seen.forEach { assertSame(payload, it) }

        var lastEvaluated = false
        val failing = verifyAll(
            expect<List<String>>("Failure one") { throw AssertionError("secret") },
            expect<List<String>>("Failure two") { throw IllegalStateException("secret") },
            expect<List<String>>("Still evaluated") { lastEvaluated = true }
        )
        val failure = try {
            failing(payload)(state)
            fail<AssertionError>("Expected accumulated failure")
        } catch (failure: AssertionError) { failure }
        assertTrue(lastEvaluated)
        assertEquals(2, state.report.failedChecks())
        assertFalse(failure.message.orEmpty().contains("secret"))
    }

    @Test
    fun `failed prerequisite aborts dependent actions and remains in report`() = runBlocking {
        val state = initial()
        val failure = IllegalStateException("secret")
        var reached = false
        val broken: E2eStep<Int, Int> = { State { throw failure } }
        val following: E2eStep<Int, Int> = { input -> State { reached = true; input to it } }
        val scenario = State<E2eState, Int>(1) * (broken reportedAs "Prerequisite") * following
        try {
            scenario runOn state
            fail<Unit>("Expected prerequisite failure")
        } catch (caught: IllegalStateException) { assertSame(failure, caught) }
        assertFalse(reached)
        state.report.writeTo(directory.resolve("failure.md"))
        val report = Files.readString(directory.resolve("failure.md"))
        assertTrue(report.contains("Prerequisite | FAIL (IllegalStateException)"))
        assertFalse(report.contains("secret"))
    }

    @Test
    fun `cancellation aborts checks immediately`() = runBlocking {
        var reached = false
        val cancellation = CancellationException("cancel")
        val checks = verifyAll(
            expect<Int>("Cancelled") { throw cancellation },
            expect<Int>("Skipped") { reached = true }
        )
        try {
            checks(1)(initial())
            fail<Unit>("Expected cancellation")
        } catch (caught: CancellationException) { assertSame(cancellation, caught) }
        assertFalse(reached)
    }

    private fun initial(): E2eState {
        val user = StdTestUser(Username("initial"), Password("unused"))
        return E2eState(
            TestUserContext(user, stub<Page>(), stub<BrowserContext>()),
            E2eEnvironment(stub<Browser>()),
            E2eReport("Composition")
        )
    }

    private inline fun <reified T> stub(crossinline call: (String) -> Any? = { error("Unexpected call: $it") }): T =
        Proxy.newProxyInstance(T::class.java.classLoader, arrayOf(T::class.java)) { _, method, _ ->
            call(method.name)
        } as T
}
