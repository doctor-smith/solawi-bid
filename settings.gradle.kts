rootProject.name = "solawi-bid"


include(":evoleq")
include(":solawi-bid-frontend")
include(":solawi-bid-database")
include(":solawi-bid-api-data")
include(":solawi-bid-backend")
include("e2e")

pluginManagement {
    repositories {
        google()
        mavenLocal()
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven ("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
