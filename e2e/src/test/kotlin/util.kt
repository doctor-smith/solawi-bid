
import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.LocatorAssertions
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import java.nio.file.Paths

fun Page.getByDataId(testId: String): Locator {
    return locator("[data-id='$testId']")
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
