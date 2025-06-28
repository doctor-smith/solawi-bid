plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("com.microsoft.playwright:playwright:1.51.0")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
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
