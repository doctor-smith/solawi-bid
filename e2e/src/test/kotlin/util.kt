import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page

fun Page.getByDataId(testId: String): Locator {
    return locator("[data-id='$testId']")
}

fun Page.put(testId: String, value: String) = getByDataId(testId).fill(value)

fun Page.submit(testId: String) = getByDataId(testId).click()
