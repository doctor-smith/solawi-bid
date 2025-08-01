plugins {
    kotlin("jvm")
    id("org.evoleq.exposedx.migration")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(project(":solawi-bid-backend"))
    testImplementation("com.microsoft.playwright:playwright:1.51.0")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.register<JavaExec>("playwright") {
    classpath(sourceSets["test"].runtimeClasspath)
    mainClass.set("com.microsoft.playwright.CLI")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
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
