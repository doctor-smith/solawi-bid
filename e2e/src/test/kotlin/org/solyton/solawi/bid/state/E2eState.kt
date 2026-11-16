package org.solyton.solawi.bid.state

import com.microsoft.playwright.Browser
import org.evoleq.math.state.State
import org.solyton.solawi.bid.AdminTestUser
import org.solyton.solawi.bid.StdTestUser
import org.solyton.solawi.bid.TestUser
import org.solyton.solawi.bid.TestUserContext
import org.solyton.solawi.bid.TestWorkflow
import org.solyton.solawi.bid.on
import org.solyton.solawi.bid.withFailureDiagnostics
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

/** Reuse an existing OOP PageObject workflow without modifying that workflow. */
fun <I, O> workflowStep(
    title: String,
    input: I,
    workflow: (TestUserContext) -> TestWorkflow<I, O>,
    diagnosticsPath: String? = null
): E2eAction<O> = State { state: E2eState ->
    val start = System.nanoTime()
    try {
        val result = if (diagnosticsPath == null) {
            workflow(state.userContext).runWith(input)
        } else {
            state.userContext.withFailureDiagnostics(diagnosticsPath) {
                workflow(state.userContext).runWith(input)
            }
        }
        state.report.step(title, (System.nanoTime() - start) / 1_000_000)
        result to state
    } catch (failure: Throwable) {
        state.report.step(title, (System.nanoTime() - start) / 1_000_000, failure)
        throw failure
    }
}

/** Switch to a new isolated Playwright user context, with the environment owning cleanup. */
fun switchUser(user: TestUser): E2eAction<Unit> = State { state: E2eState ->
    Unit to state.copy(userContext = state.environment.open(user))
}

data class E2eExpectation(val title: String, val evaluate: E2eState.() -> Unit)

fun expect(title: String, evaluate: E2eState.() -> Unit): E2eExpectation =
    E2eExpectation(title, evaluate)

/**
 * Applicative-style accumulation of INDEPENDENT expectations.
 * No browser actions are parallelized. All checks run sequentially on the
 * same page, then we report their combined result.
 *
 * This is deliberately NOT a new Applicative typeclass; the independent
 * validation phase can be generalized after validating this small spike.
 */
fun verifyAll(vararg expectations: E2eExpectation): E2eAction<Unit> = State { state: E2eState ->
    val failures = mutableListOf<String>()
    expectations.forEach { expectation ->
        val start = System.nanoTime()
        try {
            expectation.evaluate(state)
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
    Unit to state
}
