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
    testLogging {
        events("passed", "skipped", "failed")
    }
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
