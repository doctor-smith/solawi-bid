
plugins {
    application
    alias(libs.plugins.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.serialization)
    alias(libs.plugins.shadow)
    alias(libs.plugins.evoleq.exposedx.migrations)
    alias(libs.plugins.evoleq.architecture.dependency)
    alias(libs.plugins.evoleq.api.doc)
    alias(libs.plugins.detekt)
    alias(libs.plugins.kover)
    alias(libs.plugins.ksp)
    alias(libs.plugins.evoleq.fp.axioms)
}

group = libs.versions.solytonGroup
version = libs.versions.solawi.get()
val solawiBackendMainClassName = "org.solyton.solawi.bid.MainKt"
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

kotlin{
    jvmToolchain(17)
}

application {
    mainClass.set(solawiBackendMainClassName)

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
}

dependencies {
    // ktor
    implementation(libs.ktor.server.core.jvm)
    implementation(libs.ktor.server.netty.jvm)
    implementation(libs.ktor.server.default.headers)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.forwarded.header)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.java.jwt)
    implementation(libs.logback)
    testImplementation(libs.ktor.server.tests.jvm)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.junit.jupiter)

    // bcrypt
    implementation (libs.mindrot.jbcrypt)

    // jwt
    implementation (libs.jjwt.api) // JWT API
    implementation (libs.jjwt.impl) // JWT implementation
    implementation (libs.jjwt.jackson) // JWT Jackson support (for JSON processing)


    implementation(libs.cdimascio.dotenv.kotlin)

    // own dependencies
    api(project(":solawi-bid-api-data"))
    api(project(":evoleq"))

    // serialization
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.kotlinx.serialization.json)
    // datetime
    implementation(libs.kotlinx.datetime)

    // exposed
    implementation(libs.exposed.core)
    implementation(libs.exposed.crypt)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.joda.time)
    // mysql connector
    implementation(libs.mysql.connector.java)

    // h2
    implementation(libs.h2)

    // slf4j
    implementation (libs.slf4j.nop)

    // mail
    implementation("org.simplejavamail:simple-java-mail:8.0.0")
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.7.5")
}

tasks.register<Test>("dbFunctionalTest"  ) {
    group = "verification"
    useJUnitPlatform() {
        includeTags("dbFunctional")
    }
    reports {
        junitXml.required = true
        html.required = true

    }
//    finalizedBy(tasks.jacocoTestReport)
}

tasks.register<Test>("apiTest") {
    group = "verification"
    useJUnitPlatform() {
        includeTags("api")
    }
    reports {
        junitXml.required = true
        html.required = true
    }
}

tasks.register<Test>("unitTest") {
    group = "verification"
    useJUnitPlatform() {
        includeTags("unit")

    }
    reports {
        junitXml.required = true
        html.required = true
    }
//    finalizedBy(tasks.jacocoTestReport)
}
tasks.register<Test>("schemaTest") {
    group = "verification"
    useJUnitPlatform() {
        includeTags("schema")

    }
    reports {
        junitXml.required = true
        html.required = true
    }
//    finalizedBy(tasks.jacocoTestReport)
}
tasks.register<Test>("migrationTest") {
    group = "verification"
    useJUnitPlatform() {
        includeTags("migration")

    }
    reports {
        junitXml.required = true
        html.required = true
    }
//    finalizedBy(tasks.jacocoTestReport)
}

tasks.register<Test>("testFrameworkTest") {
    group = "verification"
    useJUnitPlatform() {
        includeTags("testFramework")

    }
    reports {
        junitXml.required = true
        html.required = true
    }
//    finalizedBy(tasks.jacocoTestReport)
}

tasks.withType<Test>().configureEach {
    // Wenn CI-Property gesetzt ist, ignoriere Failures
    ignoreFailures = project.findProperty("ignoreFailuresInTests")?.toString()?.toBoolean() ?: false
}

/*
tasks.jacocoTestReport {
    reports {

        xml.isEnabled = false // Disable XML report
        csv.isEnabled = false // Disable CSV report
        html.isEnabled = true  // Enable HTML report
    }
}


 */

// Konfiguration des Plugins
apiDoc {
    apiPath.set("org.solyton.solawi.bid.application.api.Solawi_apiKt.getSolawiApi")
    outputFile.set(layout.projectDirectory.file("API_DOCUMENTATION.md"))
}


migrations {
    migration("dbMain") {
        domain = "org.solyton.solawi.bid"
        module = "application"
        migrations = "data/db/migrations"
        sourceSet = "main"
    }

    migration("dbSchemaTest") {
        domain = "org.solyton.solawi.bid"
        module = "db/schema"
        migrations = "migrations"
        sourceSet = "test"
    }

    migration("bidApiTest") {
        domain = "org.solyton.solawi.bid"
        module = "bid/routing"
        migrations = "migrations"
        sourceSet = "test"
    }

    migration("authenticationApiTest") {
        domain = "org.solyton.solawi.bid"
        module = "authentication" // /routing
        migrations = "migrations"
        sourceSet = "test"
    }

    migration("applicationApiTest") {
        domain = "org.solyton.solawi.bid"
        module = "application"
        forceModule = true
        migrations = "migrations"
        sourceSet = "test"
    }

    migration("userManagementApiTest") {
        domain = "org.solyton.solawi.bid"
        module = "usermanagement"
        migrations = "migrations"
        sourceSet = "test"
    }
}

detekt {
    toolVersion = libs.versions.detekt.get()
    config.from( files("$rootDir/detekt/detekt.yml"))
    buildUponDefaultConfig = true
    allRules = false
}

tasks.named<io.gitlab.arturbosch.detekt.DetektCreateBaselineTask>("detektBaselineMain") {
    baseline.set(file("detekt/detekt-baseline-main.xml"))
}
tasks.named<io.gitlab.arturbosch.detekt.Detekt>("detektMain") {
    baseline.set(file("detekt/detekt-baseline-main.xml"))
}

tasks.named<io.gitlab.arturbosch.detekt.DetektCreateBaselineTask>("detektBaselineTest") {
    baseline.set(file("detekt/detekt-baseline-test.xml"))
}
tasks.named<io.gitlab.arturbosch.detekt.Detekt>("detektTest") {
    baseline.set(file("detekt/detekt-baseline-test.xml"))
}

dependencyAnalyser {
    analyse("backend") {
        domain = "org.solyton.solawi.bid"
        sourceSet = "main"
        modules = setOf(
            "application",
            "auditable",
            "authentication",
            "banking",
            "bid",
            "health",
            "permission",
            "user",
        )
        checkCyclesBeforeBuild = true
    }
}

