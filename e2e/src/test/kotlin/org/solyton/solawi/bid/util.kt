
import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.TimeoutError
import com.microsoft.playwright.assertions.LocatorAssertions
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import com.microsoft.playwright.options.WaitForSelectorState
import org.solyton.solawi.bid.TestUserContext
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun Page.getByDataId(testId: String): Locator {
    return locator("[data-id='$testId']")
}

fun Page.getByPartialDataId(testId: String): Locator {
    return locator("[data-id*='$testId']")
}

fun Page.put(testId: String, value: String) = getByDataId(testId).fill(value)

fun Page.clickOn(testId: String) = getByDataId(testId).apply {
    assertThat(this).isVisible()
    click()
}
/**
 * Many pages render a full-page `Loading()` overlay (`div.loading-page`) while their data is
 * still being fetched asynchronously. Interacting with elements underneath it too early (e.g.
 * right after a navigation) leads to flaky `TimeoutError`s, since the overlay briefly covers the
 * whole page. Call this after navigating/clicking and before the next interaction to wait until
 * the overlay is gone, the same way a real user would wait for the page to finish loading.
 */
fun Page.waitForLoadingToFinish() {
    val loadingSpinner = locator("div.loading-page")
    assertThat(loadingSpinner).not().isVisible(LocatorAssertions.IsVisibleOptions())
}

fun Page.shot(path: String): ByteArray = screenshot(Page.ScreenshotOptions().setPath(Paths.get(path.let{
    when{
        it.endsWith(".png") -> path
        else -> "$path.png"
    }
})))

fun Page.inputText(selector: String, value: String): Page = apply{
    locator(selector).fill(value)
}

/**
 * Navigating straight to one of these pages can briefly flash one or more unrelated
 * error dialogs on first mount (e.g. a "coroutine scope left the composition" error, or
 * the organization page's per-row application-context lookups transiently failing right
 * after a context was just created), which cover the whole page and can even stack on
 * top of each other. If any show up, dismiss all of them so the page below becomes
 * interactable again, just like a real user clicking them away one by one would.
 */
fun Page.dismissStrayErrorDialogIfPresent(dismiss: Boolean = true) {
    if (!dismiss) return
    val errorOk = getByDataId("null.modal.submit-button")
    while (true) {
        try {
            errorOk.first().waitFor(
                Locator.WaitForOptions().setTimeout(3000.0).setState(WaitForSelectorState.VISIBLE)
            )
            errorOk.first().click()
        } catch (e: TimeoutError) {
            // No (more) stray error dialogs - nothing left to dismiss.
            return
        }
    }
}

/**
 * Clicks [dataId] if (and only if) it is currently visible, within a short timeout.
 * Used to drive an application through whichever lifecycle steps it still needs
 * (available -> registered -> trialing -> active), without needing to know its
 * current state up front: steps that don't apply (already done, or not reached yet)
 * are simply skipped.
 */
fun Page.clickIfPresent(dataId: String, timeoutMs: Double = 3000.0) {
    val locator = getByDataId(dataId).first()
    try {
        locator.waitFor(Locator.WaitForOptions().setTimeout(timeoutMs).setState(WaitForSelectorState.VISIBLE))
    } catch (e: TimeoutError) {
        return
    }
    locator.click()
    waitForLoadingToFinish()
}

/**
 * Navigates to the organization-scoped page of a booked application by going through
 * its "organizations with <application>" list page and clicking the plain-text link
 * for [organizationName], rather than guessing the organization's id.
 */
fun Page.navigateToOrganizationApplicationPage(listSegment: String, organizationName: String, hasToNavigate: Boolean = false) {
    if (hasToNavigate) navigate("${TestConfig.BASE_URL}/app/management/organizations/with/$listSegment")
    dismissStrayErrorDialogIfPresent()
    waitForLoadingToFinish()

    // This list page renders each organization as a plain clickable `<span>` (the
    // shared `Link` composable, which does client-side routing via `onClick`, not a
    // real `<a>` tag) with the organization's name as its text (no `data-id`), so
    // locate it by text instead.
    locator("span", Page.LocatorOptions().setHasText(organizationName)).first().click()
    waitForLoadingToFinish()
    dismissStrayErrorDialogIfPresent()
}

/**
 * On any failure, dumps a full-page screenshot and the current page's HTML into
 * `build/reports/tests/screenshots/` for debugging, before rethrowing.
 */
fun <T> Page.withFailureDiagnostics(testCase: String? = null, block: () -> T): T {
    val consoleLog = StringBuilder()
    val networkLog = StringBuilder()
    onConsoleMessage { msg -> consoleLog.appendLine("[console:${msg.type()}] ${msg.text()}") }
    onPageError { error -> consoleLog.appendLine("[pageerror] $error") }
    onResponse { response ->
        if (response.status() >= 400) {
            try {
                val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))

                networkLog.appendLine("=== ${response.request().method()} ${response.url()} -> ${response.status()} ===")
                networkLog.appendLine("timestamp: $timestamp")
                val context = response.request().headerValue("CONTEXT")
                networkLog.appendLine("context-header: $context")
                val postData = response.request().postData()
                if (postData != null) networkLog.appendLine("request body: $postData")
                networkLog.appendLine("response body: ${response.text()}")
                // The page the test was on when this background request fired, so a failing
                // call can be matched back to the fixture step that triggered it.
                networkLog.appendLine("source-page-url: ${url()}")
                networkLog.appendLine()
            } catch (ignored: Exception) {
                // Response body may not be readable for every request type - ignore.
            }
        }
    }
    try {
        return block()
    } catch (e: Throwable) {
        try {
            val dir = Paths.get("build/reports/tests/screenshots${testCase?.let{"/$testCase"}}")
            Files.createDirectories(dir)
            screenshot(Page.ScreenshotOptions().setPath(dir.resolve("failure.png")).setFullPage(true))
            Files.writeString(dir.resolve("failure.html"), content())
            Files.writeString(dir.resolve("failure-console.log"), consoleLog.toString())
            Files.writeString(dir.resolve("failure-network.log"), networkLog.toString())
        } catch (diagnosticFailure: Exception) {
            // Best-effort diagnostics only - never hide the original failure.
        }
        throw e
    }
}

fun <T> TestUserContext.withFailureDiagnostics(testCase: String? = null, block: () -> T): T {
    val userLog = StringBuilder()
    val consoleLog = StringBuilder()
    val networkLog = StringBuilder()

    val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
    userLog.appendLine("Date: $timestamp")
    userLog.appendLine("username: ${user.username.value}")
    userLog.appendLine("password: ${user.password.value}")

    // create dir and write user log
    val dir = Paths.get("build/reports/tests/screenshots${testCase?.let{"/$testCase/${user.username.value}"}}")
    Files.createDirectories(dir)
    Files.writeString(dir.resolve("user.log"), userLog.toString())

    page.onConsoleMessage { msg -> consoleLog.appendLine("[console:${msg.type()}] ${msg.text()}") }
    page.onPageError { error -> consoleLog.appendLine("[pageerror] $error") }
    page.onResponse { response ->
        if (response.status() >= 400) {
            try {
                val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))

                networkLog.appendLine("=== ${response.request().method()} ${response.url()} -> ${response.status()} ===")
                networkLog.appendLine("timestamp: $timestamp")
                val context = response.request().headerValue("CONTEXT")
                networkLog.appendLine("context-header: $context")
                val postData = response.request().postData()
                if (postData != null) networkLog.appendLine("request body: $postData")
                networkLog.appendLine("response body: ${response.text()}")
                // The page the test was on when this background request fired, so a failing
                // call can be matched back to the fixture step that triggered it.
                networkLog.appendLine("source-page-url: ${page.url()}")
                networkLog.appendLine()
            } catch (ignored: Exception) {
                // Response body may not be readable for every request type - ignore.
            }
        }
    }
    try {
        return block()
    } catch (e: Throwable) {
        try {


            page.screenshot(Page.ScreenshotOptions().setPath(dir.resolve("failure.png")).setFullPage(true))
            Files.writeString(dir.resolve("failure.html"), page.content())
            Files.writeString(dir.resolve("failure-console.log"), consoleLog.toString())
            Files.writeString(dir.resolve("failure-network.log"), networkLog.toString())
        } catch (diagnosticFailure: Exception) {
            // Best-effort diagnostics only - never hide the original failure.
        }
        throw e
    }
}

