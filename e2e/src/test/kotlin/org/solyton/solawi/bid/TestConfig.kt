package org.solyton.solawi.bid

object TestConfig {
    /**
     * Defaults to the port used by the regular development environment and CI
     * (docker-compose.yml / docker-compose.e2e.yml), so existing setups keep working
     * unchanged. The dedicated local E2E environment (docker-compose.e2e.local.yml) uses
     *  distinct ports and sets E2E_BASE_URL itself (see script/e2e-local.sh).
     */
    val BASE_URL: String = System.getenv("E2E_BASE_URL") ?: "http://localhost:8080"

    /**
     * Indicates whether the application should run in a headless mode, typically used in environments
     * where a graphical user interface is not available or required.
     *
     * The value is determined by the "HEADLESS" environment variable. If the environment variable is
     * not set, the default value is `true`.
     *
     * @property HEADLESS Boolean flag to control headless mode configuration.
     */
    val HEADLESS: Boolean = System.getenv("HEADLESS")?.toBoolean()?: true

    /**
     * A constant representing the slowdown multiplier for animations or processes,
     * typically used for debugging or visualizing processes at a slower pace.
     *
     * The value is fetched from the environment variable "SLOW_MO". If the variable
     * is not set or cannot be converted to a Double, it defaults to 0.0.
     *
     * This variable allows dynamic adjustment of the slow-motion factor
     * without requiring code changes, making it convenient for testing purposes.
     */
    val SLOW_MO: Double = System.getenv("SLOW_MO")?.toDouble() ?: 0.0
}
