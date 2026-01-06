rootProject.name = "solawi-bid"


include(":evoleq")
include(":solawi-bid-frontend")
include(":solawi-bid-database")
include(":solawi-bid-api-data")
include(":solawi-bid-backend")
include("e2e")

pluginManagement {
    resolutionStrategy {
        eachPlugin {
                if (requested.id.id == "org.evoleq.fp-axioms-plugin") {
                // ...leite Gradle auf das JitPack-Artefakt des Untermoduls um
                // Format: com.github.<USER>.<REPO>:<SUBMODULE>:<VERSION>
                useModule("com.github.evoleq.fp-axioms:fp-axioms-plugin:${requested.version}")
            }
        }
    }

    repositories {
        google()
        mavenLocal()
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven ("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
        maven { url = uri("https://jitpack.io") }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
