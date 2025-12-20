allprojects {
    repositories {
        google()
        mavenLocal()
        mavenCentral()
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap") }
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

plugins{
    alias(libs.plugins.android) apply false
    alias(libs.plugins.jvm) apply false
    alias(libs.plugins.serialization) apply false
    alias(libs.plugins.mpp) apply false
    alias(libs.plugins.ktor) apply false
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.shadow) apply false
    alias(libs.plugins.evoleq.exposedx.migrations) apply false
    alias(libs.plugins.evoleq.api.doc) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.kover) apply false
}

tasks.register("detektAll") {
    group = "verification"
    description = "Runs all detekt tasks in all projects"

    val detektTasks = listOf("detekt", "detektMain", "detektTest",
        "detektJs" , "detektJsMain", "detektJsTest",
        "detektJvm" , "detektJvmMain", "detektJvmTest",
    )

    dependsOn(
        rootProject.allprojects.flatMap { project ->
            project.tasks.matching {
                if(it.name in detektTasks){ println("${project.name}:${it.name}")}
                it.name in detektTasks
            }
        }
    )
}
