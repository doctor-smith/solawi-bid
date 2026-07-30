import java.util.*


plugins {
    kotlin("jvm")
    alias(libs.plugins.evoleq.exposedx.migrations)
    alias(libs.plugins.ksp)
    alias(libs.plugins.evoleq.fp.axioms)
}

repositories {
    google()
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    testImplementation(project(":solawi-bid-backend"))
    testImplementation(libs.microsoft.playwright)
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.junit.jupiter.v581)
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
    listOf("TEST_USER", "TEST_USER_PASSWORD").forEach { key ->
        val fromEnv = System.getenv(key)
        val fromProps = localProps.getProperty(key)
        (fromEnv ?: fromProps)?.let { environment(key, it) }
    }
}
