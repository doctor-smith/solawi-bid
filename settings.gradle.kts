rootProject.name = "solawi-bid"

include(":solawi-bid-frontend")
include(":solawi-bid-database")
include(":solawi-bid-api-data")
include(":solawi-bid-backend")

pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven ("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
include("e2e")
include("e2e")
