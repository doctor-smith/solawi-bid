import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import java.util.*


plugins {
    kotlin("jvm")
    alias(libs.plugins.evoleq.exposedx.migrations)
    alias(libs.plugins.ksp)
    alias(libs.plugins.evoleq.fp.axioms)
}

repositories {
    mavenCentral()
    google()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    // Test stuff
    testImplementation(libs.microsoft.playwright)
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.junit.jupiter.v581)

    // Backend code
    testImplementation(project(":solawi-bid-backend"))

    // Serialization
    testImplementation(libs.kotlinx.serialization.json)
    testImplementation(libs.ktor.client.serialization)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.register<JavaExec>("playwright") {
    workingDir = file("$projectDir")
    classpath(sourceSets["test"].runtimeClasspath)
    mainClass.set("com.microsoft.playwright.CLI")
}

tasks.test {
    useJUnitPlatform()
    reports {
        junitXml.required.set(true)
        html.required.set(true)
        junitXml.outputLocation.set(file(layout.buildDirectory.dir("test-results/test")))
    }
}

tasks.register<JavaExec>("installPlaywright") {
    group = "playwright"
    description = "Installiert die Playwright-Browser"
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("com.microsoft.playwright.CLI")
    args = listOf("install", "--with-deps")
}

tasks.withType<Test>().configureEach {
    onlyIf {
        val requestedTasks = gradle.startParameter.taskNames
        // Disable tests if `build` is requested and this test task wasn't explicitly requested
        !("build" in requestedTasks && name !in requestedTasks)
    }
}


val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

tasks.withType<Test>().configureEach {
    testLogging {
        events()
        exceptionFormat = TestExceptionFormat.FULL
        showExceptions = true
        showCauses = true
        showStackTraces = true
    }

    val ansiReset = "\u001B[0m"
    val ansiGreen = "\u001B[32m"
    val ansiRed = "\u001B[31m"
    val ansiYellow = "\u001B[33m"
    val ansiCyan = "\u001B[36m"
    val ansiBold = "\u001B[1m"
    val ansiDim = "\u001B[2m"

    val failedTestDetails = mutableListOf<Pair<String, String>>()
    var suiteStartTime = 0L

    addTestListener(object : TestListener {
        override fun beforeSuite(suite: TestDescriptor) {
            if (suite.parent == null) {
                suiteStartTime = System.currentTimeMillis()
                failedTestDetails.clear()
                logger.lifecycle("\n${ansiBold}${ansiCyan}► Executing test suite: :${project.name}:${name}${ansiReset}\n")
            }
        }

        override fun afterSuite(suite: TestDescriptor, result: TestResult) {
            if (suite.parent == null) {
                val totalDuration = (System.currentTimeMillis() - suiteStartTime) / 1000.0
                val passedStr = "${ansiGreen}${result.successfulTestCount} PASSED${ansiReset}"
                val failedStr = if (result.failedTestCount > 0) "${ansiRed}${result.failedTestCount} FAILED${ansiReset}" else "0 FAILED"
                val skippedStr = if (result.skippedTestCount > 0) "${ansiYellow}${result.skippedTestCount} SKIPPED${ansiReset}" else "0 SKIPPED"

                logger.lifecycle("")
                logger.lifecycle("${ansiBold}================================================================================${ansiReset}")
                logger.lifecycle("${ansiBold} TEST RESULTS SUMMARY: :${project.name}:${name}${ansiReset}")
                logger.lifecycle("--------------------------------------------------------------------------------")
                logger.lifecycle(" Total Tests : ${result.testCount}")
                logger.lifecycle(" Results     : $passedStr | $failedStr | $skippedStr")
                logger.lifecycle(" Total Time  : ${String.format(Locale.US, "%.2f", totalDuration)}s")

                if (failedTestDetails.isNotEmpty()) {
                    logger.lifecycle("--------------------------------------------------------------------------------")
                    logger.lifecycle("${ansiRed}${ansiBold} FAILED TESTS (${failedTestDetails.size}):${ansiReset}")
                    failedTestDetails.forEach { (testName, reason) ->
                        logger.lifecycle("   ${ansiRed}✘${ansiReset} ${ansiBold}$testName${ansiReset}")
                        logger.lifecycle("     ${ansiRed}Reason:${ansiReset} $reason")
                    }
                }
                logger.lifecycle("${ansiBold}================================================================================${ansiReset}\n")
            }
        }

        override fun beforeTest(testDescriptor: TestDescriptor) {
        }

        override fun afterTest(testDescriptor: TestDescriptor, result: TestResult) {
            val durationMs = result.endTime - result.startTime
            val durationStr = if (durationMs >= 1000) {
                String.format(Locale.US, "%.2fs", durationMs / 1000.0)
            } else {
                "${durationMs}ms"
            }

            val statusSymbol = when (result.resultType) {
                TestResult.ResultType.SUCCESS -> "${ansiGreen}✔ PASSED${ansiReset}"
                TestResult.ResultType.FAILURE -> "${ansiRed}✘ FAILED${ansiReset}"
                TestResult.ResultType.SKIPPED -> "${ansiYellow}⊘ SKIPPED${ansiReset}"
                else -> result.resultType.name
            }

            val className = testDescriptor.className?.substringAfterLast('.') ?: testDescriptor.className ?: ""
            val testName = testDescriptor.displayName ?: testDescriptor.name

            logger.lifecycle("  $statusSymbol  ${ansiDim}[${durationStr}]${ansiReset} ${ansiBold}$className${ansiReset} > $testName")

            if (result.resultType == TestResult.ResultType.FAILURE) {
                val fullTestName = "${testDescriptor.className} > ${testDescriptor.name}"
                val cause = result.exception?.message?.lines()?.firstOrNull() ?: "Unknown error"
                failedTestDetails.add(fullTestName to cause)
                logger.lifecycle("     ${ansiRed}└─ Reason: $cause${ansiReset}")
            }
        }
    })

    // Only set if not already provided by the shell / CI
    // E2E_RESET_DB opts into a full local database reset before the first test,
    // see DbReset.kt/DbResetExtension.kt; script/e2e-local.sh sets it automatically.
    listOf(
        "TEST_USER",
        "TEST_USER_PASSWORD",
        "E2E_BASE_URL",
        "E2E_RESET_DB",
        "PARALLEL_TESTS",
        "NUMBER_OF_WORKERS"
    ).forEach { key ->
        val fromEnv = System.getenv(key)
        val fromProps = localProps.getProperty(key)
        (fromEnv ?: fromProps)?.let { environment(key, it) }
    }

// Parallel test execution
    val parallelTests = (
            System.getenv("PARALLEL_TESTS")
                ?: localProps.getProperty("PARALLEL_TESTS")
            )?.toBoolean() ?: false

    val numberOfWorkers = (
            System.getenv("NUMBER_OF_WORKERS")
                ?: localProps.getProperty("NUMBER_OF_WORKERS")
            )?.toIntOrNull() ?: 1

    maxParallelForks = if (parallelTests) {
        numberOfWorkers.coerceAtLeast(1)
    } else {
        1
    }

    doFirst {
        logger.lifecycle("========================================")
        logger.lifecycle("Test task: $name")
        logger.lifecycle("PARALLEL_TESTS: $parallelTests")
        logger.lifecycle("NUMBER_OF_WORKERS requested: $numberOfWorkers")
        logger.lifecycle("Gradle maxParallelForks: $maxParallelForks")
        logger.lifecycle("========================================")
    }
}

tasks.register<Test>("regressionTest"  ) {
    description = "Run all regression tests"
    group = "verification"
    useJUnitPlatform() {
        includeTags("regression")
    }
    reports {
        junitXml.required = true
        html.required = true

    }
//    finalizedBy(tasks.jacocoTestReport)
}

tasks.register<Test>("smokeTest") {
    description = "Run all smoke tests"
    group = "verification"
    useJUnitPlatform() {
        includeTags("smoke")
    }
    reports {
        junitXml.required = true
        html.required = true

    }
//    finalizedBy(tasks.jacocoTestReport)
}

tasks.register<Test>("userJourneyTest") {
    description = "Run all user-journey tests"
    group = "verification"
    useJUnitPlatform() {
        includeTags("user-journey")
    }
    reports {
        junitXml.required = true
        html.required = true

    }
//    finalizedBy(tasks.jacocoTestReport)
}

tasks.register<Test>("workflowTest") {
    description = "Run all workflow tests"
    group = "verification"
    useJUnitPlatform() {
        includeTags("workflow")
    }
    reports {
        junitXml.required = true
        html.required = true

    }
//    finalizedBy(tasks.jacocoTestReport)
}

tasks.register<Test>("securityTest") {
    description = "Run all security tests"
    group = "verification"
    useJUnitPlatform() {
        includeTags("security")
    }
    reports {
        junitXml.required = true
        html.required = true

    }
//    finalizedBy(tasks.jacocoTestReport)
}

tasks.register<Test>("performanceTest") {
    description = "Run all performance tests"
    group = "verification"
    useJUnitPlatform() {
        includeTags("performance")
    }
    reports {
        junitXml.required = true
        html.required = true

    }
//    finalizedBy(tasks.jacocoTestReport)
}


tasks.register<Test>("developmentTest") {
    description = "Run all tests under development"
    group = "verification"
    useJUnitPlatform() {
        includeTags("develop")
    }
    reports {
        junitXml.required = true
        html.required = true

    }
//    finalizedBy(tasks.jacocoTestReport)
}
