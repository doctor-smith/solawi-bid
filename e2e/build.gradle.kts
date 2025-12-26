plugins {
    kotlin("jvm")
    alias(libs.plugins.evoleq.exposedx.migrations)
}

repositories {
    google()
    mavenCentral()
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
