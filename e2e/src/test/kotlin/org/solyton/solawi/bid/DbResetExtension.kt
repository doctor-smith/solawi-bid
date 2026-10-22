import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext

/**
 * Auto-registered JUnit 5 extension (see `src/test/resources/META-INF/services`
 * and `junit-platform.properties`) that runs before every test class and asks
 * [DbReset] to reset the local E2E database. It actually only does so once per
 * whole test run, and only when explicitly opted into - see [DbReset] for
 * details.
 */
class DbResetExtension : BeforeAllCallback {
    override fun beforeAll(context: ExtensionContext) {
        DbReset.resetOnce()
    }
}
