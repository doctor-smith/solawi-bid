repositories {
    mavenLocal()
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap") }
    google()
}


plugins{
    alias(libs.plugins.jvm) apply false
    alias(libs.plugins.serialization) apply false
    alias(libs.plugins.mpp) apply false
    alias(libs.plugins.ktor) apply false
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.shadow) apply false
    id("org.evoleq.exposedx.migration") apply false
}
/*
configurations.all {
    exclude(group = "org.gradle.api.plugins", module = "MavenPlugin")
}

 */





