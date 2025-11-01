plugins {
    alias(libs.plugins.mpp)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.serialization)
    `maven-publish`
    alias(libs.plugins.detekt)
    alias(libs.plugins.kover)
    id("org.evoleq.architecture.dependency")
}



group = libs.versions.solytonGroup
version = libs.versions.solawi.get()
val kotlinVersion = libs.versions.kotlin

repositories {
    google()
    mavenLocal()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven ("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
}

configurations.all {
    exclude(group = "org.gradle.api.plugins", module = "MavenPlugin")
}

kotlin {
    jvmToolchain(17)
}
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}


kotlin{
    js(IR) {
        browser{
            testTask {
                useKarma {
                    useChromeHeadlessNoSandbox()
                }
            }
        }
        binaries.executable()
    }
    jvm(){ }
    sourceSets {
        val commonMain by getting {
           // kotlin.srcDir("src/commonMain/kotlin")

            dependencies {
                // implementation(kotlin("stdlib-common"))
                // implementation("org.jetbrains.kotlin:kotlin-stdlib-common:${kotlinVersion}")
                implementation(libs.benasher.uuid)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.json)

                // datetime
                implementation(libs.kotlinx.datetime)

                implementation(compose.runtime)
                implementation(compose.foundation)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test")) // Adds kotlin.test for multiplatform
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8") // Specific for JVM
                implementation(libs.exposed.joda.time)
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(libs.kotlin.test.junit)
            }
        }

        val jsMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-html-js:0.8.0") // Example for JS
                // ktor client
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.js)
                implementation(libs.ktor.http)
                implementation(libs.ktor.http.cio)

                // compose
                implementation(compose.html.core)
                implementation(compose.runtime)
            }
        }
        val jsTest by getting {
            dependencies {
                implementation(libs.kotlin.test.js) // Adds kotlin.test for multiplatform
            }
        }

    }
}
tasks.withType<Test> {
    reports {
        junitXml.required = true
        html.required = true
    }
}

tasks.withType<Test>().configureEach {
    // Wenn CI-Property gesetzt ist, ignoriere Failures
    ignoreFailures = project.findProperty("ignoreFailuresInTests")?.toString()?.toBoolean() ?: false
}

publishing {
    publications {
        withType<MavenPublication>()
    }
    repositories {
        mavenLocal()
    }
}

detekt {
    toolVersion = libs.versions.detekt.get()
    config.from(files("$rootDir/detekt/detekt.yml"))
    buildUponDefaultConfig = true
    allRules = false

    source.from(
        "src/commonMain/kotlin",
        "src/commonTest/kotlin",
        "src/jsMain/kotlin",
        "src/jsTest/kotlin",

        )
}

tasks.named<io.gitlab.arturbosch.detekt.DetektCreateBaselineTask>("detektBaseline") {
    baseline.set(file("detekt/detekt-baseline.xml"))
}
tasks.named<io.gitlab.arturbosch.detekt.Detekt>("detekt") {
    baseline.set(file("detekt/detekt-baseline.xml"))
}




tasks.named<io.gitlab.arturbosch.detekt.DetektCreateBaselineTask>("detektBaselineJsMain") {
    baseline.set(file("detekt/detekt-baseline-js-main.xml"))
}
tasks.named<io.gitlab.arturbosch.detekt.Detekt>("detektJsMain") {
    baseline.set(file("detekt/detekt-baseline-js-main.xml"))
}

tasks.named<io.gitlab.arturbosch.detekt.DetektCreateBaselineTask>("detektBaselineJsTest") {
    baseline.set(file("detekt/detekt-baseline-js-test.xml"))
}
tasks.named<io.gitlab.arturbosch.detekt.Detekt>("detektJsTest") {
    baseline.set(file("detekt/detekt-baseline-js-test.xml"))
}

dependencyAnalyser {
    analyse("evoleqCommonMain") {
        domain = "org.evoleq"
        sourceSet = "commonMain"
        modulePath = ""
        modules = setOf(
            "configuration",
            "csv",
            "ktorx/date",
            "ktorx/api",
            "ktorx/result",
            "math",
            "permission",
            "serializationx",
            "test",
            "uuid",
            "value"
        )
        checkCyclesBeforeBuild = true
        checkAppModalDependencies = false
    }
    analyse("evoleqJsMain") {
        domain = "org.evoleq"
        sourceSet = "jsMain"
        modulePath = ""
        modules = setOf(
            "compose",
            "device",
            "ktorx/client",
            "language",
            "optics",
            "parser"
        )
        checkCyclesBeforeBuild = true
        checkAppModalDependencies = false
    }
    analyse("evoleqComposeWeb") {
        domain = "org.evoleq.compose"
        sourceSet = "jsMain"
        modulePath = ""
        modules = setOf(
            "attribute",
            "card",
            "date",
            "dnd",
            "error",
            "i18n",
            "label",
            "layout",
            "link",
            "modal",
            "routing",
            "storage",
            "structure",
            "style",
        )
        checkCyclesBeforeBuild = true
        checkAppModalDependencies = false
    }
    analyse("evoleqOptics") {
        domain = "org.evoleq.optics"
        sourceSet = "jsMain"
        modulePath = ""
        modules = setOf(
            "iso",
            "lens",
            "prism",
            "sg",
            "storage",
            "transform"
        )
        checkCyclesBeforeBuild = true
        checkAppModalDependencies = false
    }
}

