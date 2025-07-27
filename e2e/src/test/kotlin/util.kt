import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page

fun Page.getByDataId(testId: String): Locator {
    return locator("[data-id='$testId']")
}