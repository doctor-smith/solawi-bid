plugins {
    alias(libs.plugins.mpp)
    alias(libs.plugins.serialization)
     `maven-publish`
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
        browser()
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
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test.junit) // Adds kotlin.test for multiplatform
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
                implementation(libs.kotlin.test.junit) // Adds kotlin.test for multiplatform
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

