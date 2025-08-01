plugins {
    alias(libs.plugins.mpp)
    alias(libs.plugins.compose)
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
    mavenLocal()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
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
        browser()
        binaries.executable()
    }
    jvm(){ }
    sourceSets {
        val commonMain by getting {
            kotlin.srcDir("/src/commonMain/kotlin")

            dependencies {
                implementation(kotlin("stdlib"))
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
        /*
        val jvmMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8") // Specific for JVM
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(libs.kotlin.test.junit)
            }
        }

         */
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
    config = files("$rootDir/detekt/detekt.yml")
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
