package org.solyton.solawi.bid

object TestConfig {
    // Defaults to the port used by the regular development environment and CI
    // (docker-compose.yml / docker-compose.e2e.yml), so existing setups keep working
    // unchanged. The dedicated local E2E environment (docker-compose.e2e.local.yml) uses
    // distinct ports and sets E2E_BASE_URL itself (see script/e2e-local.sh).
    val BASE_URL: String = System.getenv("E2E_BASE_URL") ?: "http://localhost:8080"
}
