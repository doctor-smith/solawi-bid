import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension

plugins {
    alias(libs.plugins.mpp)
    alias(libs.plugins.compose)
    alias(libs.plugins.serialization)
    id("org.evoleq.math.cat.gradle.optics")
    alias(libs.plugins.detekt)
    alias(libs.plugins.kover)
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven {
        url = uri("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")
    }
    maven {
        url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
    }
    google()
}

group = libs.versions.solytonGroup
version = libs.versions.solawi

kotlin {
    js(IR) {
        browser()
        binaries.executable()
    }
    sourceSets {
        val jsMain by getting {
            kotlin.srcDir("src/jsMain/kotlin")
            resources.srcDir("src/main/resources")

            dependencies {
                // kotlin coroutines
                implementation(libs.kotlinx.coroutines.core)


                // ktor client
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.js)
                implementation(libs.ktor.http)
                implementation(libs.ktor.http.cio)

                // own dependencies
                api(project(":solawi-bid-api-data"))

                // Serialization
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.ktor.client.serialization)

                // dotenv
                implementation(npm("dotenv", "16.0.1"))

                // compose
                implementation(compose.html.core)
                implementation(compose.runtime)

                // qr todo "Add dependency to lib, if possible"
                implementation(npm("qrcode", "1.5.1"))

            }
        }

        val jsTest by getting {
            kotlin.srcDir("src/jsTest/kotlin")

            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                implementation(kotlin("test-js"))
                implementation(compose.html.testUtils)
                implementation(libs.ktor.client.mock) // Ktor Client Mock)
            }
        }

        val commonMain by getting {
            kotlin.srcDir("src/commonMain/kotlin")
            dependencies {
                // kotlin coroutines
                implementation(libs.kotlinx.coroutines.core)

                // datetime
                implementation(libs.kotlinx.datetime)

                // ktor client
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.js)
                implementation(libs.ktor.http)
                implementation(libs.ktor.http.cio)

                // own dependencies
                api(project(":solawi-bid-api-data"))

                // Serialization
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.ktor.client.serialization)


            }

            val commonTest by getting {
                kotlin.srcDir("src/commonTest/kotlin")
                dependencies {
                    implementation(libs.kotlinx.coroutines.core)
                    implementation(kotlin("test"))
                }
            }
        }
    }


}
optics{
    sourceSet = "jsMain"
    defaultPackage = "org.solyton.solawi.bid.data"
}
tasks.withType<Test>() {
    reports {
        junitXml.required = true
        html.required = true
    }
}
tasks.register<Test>("commonTest") {
    useJUnitPlatform()  // or your specific test platform
    testClassesDirs =  files("src/commonTest/kotlin")
 //   classpath = sourceSets["commonTest"].runtimeClasspath
}

compose {
     //kotlinCompilerPlugin.set("androidx.compose.compiler:compiler:$composeCompiler")
}
// a temporary workaround for a bug in jsRun invocation - see https://youtrack.jetbrains.com/issue/KT-48273
afterEvaluate {
    rootProject.extensions.configure<NodeJsRootExtension> {
        nodeVersion = "20.0.0"//""16.0.0"
        versions.webpack.version = "5.75.0"
        versions.webpackDevServer.version = "4.10.0"//"4.0.0"
        versions.webpackCli.version = "5.1.0"//""4.10.0"
    }
}

detekt {
    toolVersion = libs.versions.detekt.get()
    config = files("$rootDir/detekt/detekt.yml")
    buildUponDefaultConfig = true
    allRules = false
}
