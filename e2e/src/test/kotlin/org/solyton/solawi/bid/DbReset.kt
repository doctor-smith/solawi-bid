package org.solyton.solawi.bid

import org.solyton.solawi.bid.DbReset.RESET_FLAG_ENV
import java.io.File

/**
 * Full database reset for the dedicated local E2E environment (see
 * [script/e2e-local.sh](../../../../../../script/e2e-local.sh) and
 * `docker-compose.e2e.local.yml`).
 *
 * Most tests should just rely on migrations and create their own uniquely
 * named fixtures instead (see e.g. `UserManagementTest`), since a full reset
 * recreates the database and backend containers from scratch and is
 * therefore comparatively slow. This is meant for the rare case where a
 * truly clean slate is wanted (e.g. once before a whole local test run).
 *
 * It is opt-in via the [RESET_FLAG_ENV] environment variable, so it never
 * runs in CI (which already starts from a fresh `docker-compose.e2e.yml` on
 * every run) or during a plain `./gradlew :e2e:test` against the regular
 * development environment. `script/e2e-local.sh test`/`run` set that flag
 * automatically; see [DbResetExtension] for how/when this actually gets
 * triggered.
 */
object DbReset {

    private const val RESET_FLAG_ENV = "E2E_RESET_DB"
    private const val SCRIPT_RELATIVE_PATH = "script/e2e-local.sh"

    @Volatile
    private var alreadyReset = false

    /**
     * Resets the local E2E database by running `./script/e2e-local.sh reset-db`,
     * but only once per JVM/test run, and only if [RESET_FLAG_ENV] is set to
     * "true". Safe to call from every test class; later calls are no-ops.
     */
    @Synchronized
    fun resetOnce() {
        if (alreadyReset) return
        if (!System.getenv(RESET_FLAG_ENV).toBoolean()) return

        val projectRoot = findProjectRoot()
        val script = File(projectRoot, SCRIPT_RELATIVE_PATH)
        check(script.exists()) {
            "$RESET_FLAG_ENV is set, but could not find $SCRIPT_RELATIVE_PATH under $projectRoot"
        }

        println("==> [DbReset] $RESET_FLAG_ENV=true, resetting the local E2E database via $SCRIPT_RELATIVE_PATH reset-db")
        val process = ProcessBuilder(script.absolutePath, "reset-db")
            .directory(projectRoot)
            .redirectErrorStream(true)
            .start()
        process.inputStream.bufferedReader().forEachLine(::println)
        val exitCode = process.waitFor()
        check(exitCode == 0) { "$SCRIPT_RELATIVE_PATH reset-db failed with exit code $exitCode" }

        alreadyReset = true
        println("==> [DbReset] Database reset complete")
    }

    private fun findProjectRoot(): File {
        var dir = File(System.getProperty("user.dir")).absoluteFile
        while (!File(dir, SCRIPT_RELATIVE_PATH).exists()) {
            dir = dir.parentFile
                ?: error("Could not locate the project root (containing $SCRIPT_RELATIVE_PATH) above ${System.getProperty("user.dir")}")
        }
        return dir
    }
}
