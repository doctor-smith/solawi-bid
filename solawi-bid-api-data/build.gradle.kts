plugins {
    alias(libs.plugins.mpp)
    alias(libs.plugins.serialization)
     `maven-publish`
    alias(libs.plugins.detekt)
  //  alias(libs.plugins.kover)
    id("org.evoleq.architecture.dependency")
}



group = libs.versions.solytonGroup
version = libs.versions.solawi.get()
val kotlinVersion = libs.versions.kotlin

repositories {
    mavenCentral()
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
            kotlin.srcDir("/src/commonMain/kotlin")

            dependencies {
                implementation(libs.benasher.uuid)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.json)

                // datetime
                implementation(libs.kotlinx.datetime)

                implementation(project(":evoleq"))
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
        "src/jvmMain/kotlin",
        "src/jvmTest/kotlin",

    )
}

tasks.named<io.gitlab.arturbosch.detekt.DetektCreateBaselineTask>("detektBaseline") {
    baseline.set(file("detekt/detekt-baseline.xml"))
}
tasks.named<io.gitlab.arturbosch.detekt.Detekt>("detekt") {
    baseline.set(file("detekt/detekt-baseline.xml"))
}


tasks.named<io.gitlab.arturbosch.detekt.DetektCreateBaselineTask>("detektBaselineJvmMain") {
    baseline.set(file("detekt/detekt-baseline-jvm-main.xml"))
}
tasks.named<io.gitlab.arturbosch.detekt.Detekt>("detektJvmMain") {
    baseline.set(file("detekt/detekt-baseline-jvm-main.xml"))
}

/*
tasks.named<io.gitlab.arturbosch.detekt.DetektCreateBaselineTask>("detektBaselineMetadataCommonMain") {
    baseline.set(file("detekt/detekt-baseline-common-main.xml"))
}
tasks.named<io.gitlab.arturbosch.detekt.Detekt>("detektMetadataCommonMain") {
    baseline.set(file("detekt/detekt-baseline-common-main.xml"))
}
tasks.named<io.gitlab.arturbosch.detekt.DetektCreateBaselineTask>("detektBaselineCommonTest") {
    baseline.set(file("detekt/detekt-baseline-common-test.xml"))
}
tasks.named<io.gitlab.arturbosch.detekt.Detekt>("detektCommonTest") {
    baseline.set(file("detekt/detekt-baseline-common-test.xml"))
}
*/
tasks.named<io.gitlab.arturbosch.detekt.DetektCreateBaselineTask>("detektBaselineJvmTest") {
    baseline.set(file("detekt/detekt-baseline-jvm-test.xml"))
}
tasks.named<io.gitlab.arturbosch.detekt.Detekt>("detektJvmTest") {
    baseline.set(file("detekt/detekt-baseline-jvm-test.xml"))
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
    analyse("apiCommonMain") {
        domain = "org.solyton.solawi.bid"
        sourceSet = "commonMain"
        modules = setOf(
            "application",
            "authentication",
            "bid",
            "permission",
            "user",
        )
        checkCyclesBeforeBuild = true
    }
}

