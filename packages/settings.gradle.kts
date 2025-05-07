rootProject.name = "solawi-bid"

include(":solawi-bid-frontend")
include(":solawi-bid-database")
include(":solawi-bid-api-data")
include(":solawi-bid-backend")

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven ("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
    }
}