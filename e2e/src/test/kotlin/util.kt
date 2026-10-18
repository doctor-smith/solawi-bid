import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.WaitForSelectorState

fun Page.getByDataId(testId: String): Locator {
    return locator("[data-id='$testId']")
}

fun Page.put(testId: String, value: String) = getByDataId(testId).fill(value)

fun Page.clickOn(testId: String) = getByDataId(testId).click()

/**
 * Many pages render a full-page `Loading()` overlay (`div.loading-page`) while their data is
 * still being fetched asynchronously. Interacting with elements underneath it too early (e.g.
 * right after a navigation) leads to flaky `TimeoutError`s, since the overlay briefly covers the
 * whole page. Call this after navigating/clicking and before the next interaction to wait until
 * the overlay is gone, the same way a real user would wait for the page to finish loading.
 */
fun Page.waitForLoadingToFinish() {
    locator("div.loading-page").waitFor(
        Locator.WaitForOptions().setState(WaitForSelectorState.DETACHED)
    )
}
