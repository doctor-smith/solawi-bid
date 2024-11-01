import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension

plugins {
    alias(libs.plugins.mpp)
    alias(libs.plugins.compose)
    alias(libs.plugins.serialization)
    id("org.evoleq.math.cat.gradle.optics")
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
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
                //"io.ktor:ktor-client-js:$ktorClientJs")

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
            }
        }

        val jsTest by getting {
            kotlin.srcDir("src/jsTest/kotlin")

            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                implementation(kotlin("test-js"))
                implementation(compose.html.testUtils)

            }
        }
    }


}
optics{
    sourceSet = "jsMain"
    defaultPackage = "org.solyton.solawi.bid.data"
}

compose {
     //kotlinCompilerPlugin.set("androidx.compose.compiler:compiler:$composeCompiler")
}
// a temporary workaround for a bug in jsRun invocation - see https://youtrack.jetbrains.com/issue/KT-48273
afterEvaluate {
    rootProject.extensions.configure<NodeJsRootExtension> {
        nodeVersion = "16.0.0"
        versions.webpackDevServer.version = "4.0.0"
        versions.webpackCli.version = "4.10.0"
    }
}