package org.solyton.solawi.bid.state

import com.microsoft.playwright.Browser
import org.evoleq.math.state.KlState
import org.evoleq.math.state.State
import org.solyton.solawi.bid.AdminTestUser
import org.solyton.solawi.bid.StdTestUser
import org.solyton.solawi.bid.TestUser
import org.solyton.solawi.bid.TestUserContext
import org.solyton.solawi.bid.TestWorkflow
import org.solyton.solawi.bid.on
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CancellationException

/**
 * Owns the Playwright sessions for one scenario.
 * User switches open new contexts; previously opened sessions are closed at scenario end.
 */
class E2eEnvironment(private val browser: Browser) : AutoCloseable {
    private val sessions = mutableListOf<TestUserContext>()

    fun open(user: TestUser): TestUserContext {
        val session = when (user) {
            is AdminTestUser -> user on browser
            is StdTestUser -> user on browser
        }
        sessions += session
        return session
    }

    override fun close() {
        var firstFailure: Throwable? = null
        sessions.asReversed().forEach { session ->
            try {
                session.close()
            } catch (failure: Throwable) {
                if (firstFailure == null) firstFailure = failure
                else firstFailure?.addSuppressed(failure)
            }
        }
        firstFailure?.let { throw it }
    }
}

/**
 * State is the composition mechanism, not a claim that Playwright is pure.
 * The shared report is an effectful sink so it survives a failed computation.
 */
data class E2eState(
    val userContext: TestUserContext,
    val environment: E2eEnvironment,
    val report: E2eReport
)

typealias E2eAction<T> = State<E2eState, T>
typealias E2eStep<I, O> = KlState<E2eState, I, O>

private enum class EntryType { STEP, CHECK }

private data class ReportEntry(
    val kind: EntryType,
    val title: String,
    val passed: Boolean,
    val durationMillis: Long,
    val exceptionType: String? = null
)

/** Business-readable report. Never include credentials, HTTP bodies or raw exception messages. */
class E2eReport(private val scenario: String) {
    private val entries = mutableListOf<ReportEntry>()

    fun step(title: String, durationMillis: Long, failure: Throwable? = null) {
        entries += ReportEntry(EntryType.STEP, title, failure == null, durationMillis, failure?.javaClass?.simpleName)
    }

    fun verification(title: String, durationMillis: Long, failure: Throwable? = null) {
        entries += ReportEntry(EntryType.CHECK, title, failure == null, durationMillis, failure?.javaClass?.simpleName)
    }

    fun failedChecks(): Int = entries.count { it.kind == EntryType.CHECK && !it.passed }

    fun writeTo(path: Path) {
        path.parent?.let { Files.createDirectories(it) }
        fun escape(s: String) = s.replace("|", "\\|").replace("\n", " ").replace("\r", " ")
        val content = buildString {
            appendLine("# " + escape(scenario))
            appendLine()
            appendLine("| Kind | Business step or expectation | Result | Duration |")
            appendLine("| --- | --- | --- | ---: |")
            entries.forEach { entry ->
                val result = if (entry.passed) "PASS" else "FAIL (" + escape(entry.exceptionType ?: "error") + ")"
                appendLine("| " + entry.kind.name.lowercase() + " | " + escape(entry.title) + " | " + result + " | " + entry.durationMillis + " ms |")
            }
            appendLine()
            appendLine("**Checks failed:** " + failedChecks())
            appendLine()
            appendLine("Technical failure details and stack traces remain in the JUnit report.")
        }
        Files.writeString(path, content)
    }
}

/** Adapt a workflow constructor; input and execution context are supplied later. */
fun <I, O> workflow(
    workflow: (TestUserContext) -> TestWorkflow<I, O>
): E2eStep<I, O> = { input ->
    State { state -> workflow(state.userContext).runWith(input) to state }
}

/** Reporting decorates an arrow without changing its input or output type. */
infix fun <I, O> E2eStep<I, O>.reportedAs(title: String): E2eStep<I, O> = { input ->
    State { state ->
        val start = System.nanoTime()
        try {
            val result = this@reportedAs(input)(state)
            state.report.step(title, (System.nanoTime() - start) / 1_000_000)
            result
        } catch (failure: Throwable) {
            state.report.step(title, (System.nanoTime() - start) / 1_000_000, failure)
            throw failure
        }
    }
}

/** Focus a workflow on part of the input and explicitly retain the surrounding data. */
fun <D, I, O, R> E2eStep<I, O>.retaining(
    input: (D) -> I,
    combine: (D, O) -> R
): E2eStep<D, R> = { data ->
    State { state ->
        val (result, next) = this@retaining(input(data))(state)
        combine(data, result) to next
    }
}

/** Change browser context while passing the complete input through unchanged. */
@Suppress("FunctionName")
fun <T> SwitchUser(user: (T) -> TestUser): E2eStep<T, T> = { data ->
    State { state ->
        data to state.copy(userContext = state.environment.open(user(data)))
    }
}

data class E2eExpectation<T>(val title: String, val evaluate: E2eState.(T) -> Unit)

fun <T> expect(title: String, evaluate: E2eState.(T) -> Unit): E2eExpectation<T> =
    E2eExpectation(title, evaluate)

/**
 * Applicative-style accumulation of INDEPENDENT expectations.
 * No browser actions are parallelized. All checks run sequentially on the
 * same page, then we report their combined result.
 *
 * This is deliberately NOT a new Applicative typeclass; the independent
 * validation phase can be generalized after validating this small spike.
 */
fun <T> verifyAll(vararg expectations: E2eExpectation<T>): E2eStep<T, T> = { data ->
    State { state ->
        val failures = mutableListOf<String>()
        expectations.forEach { expectation ->
            val start = System.nanoTime()
            try {
                expectation.evaluate(state, data)
                state.report.verification(expectation.title, (System.nanoTime() - start) / 1_000_000)
            } catch (failure: Throwable) {
                if (failure is CancellationException || failure is VirtualMachineError || failure is ThreadDeath) {
                    throw failure
                }
                state.report.verification(expectation.title, (System.nanoTime() - start) / 1_000_000, failure)
                failures += expectation.title
            }
        }
        if (failures.isNotEmpty()) {
            throw AssertionError(failures.size.toString() + " independent expectation(s) failed: " + failures.joinToString())
        }
        data to state
    }
}
