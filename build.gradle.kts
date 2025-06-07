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

/*
tasks.register("detektBaselineAll") {
    group = "verification"
    description = "Runs all detekt tasks in all projects"

    val detektTasks = listOf("detektBaseline", "detektBaselineMain", "detektBaselineTest",
        "detektBaselineJs" , "detektBaselineJsMain", "detektBaselineJsTest",
        "detektBaselineJvm" , "detektBaselineJvmMain", "detektJvmTest",
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

 */
tasks.register("addNewlineToFiles") {
    group = "custom"
    description = "Adds a newline at the end of each file in a target directory if not present."

    // Customize this path as needed
    val targetDir = project.rootDir // or replace with any directory

    doLast {
        if (!targetDir.exists() || !targetDir.isDirectory) {
            println("Directory does not exist: ${targetDir.absolutePath}")
            return@doLast
        }

        targetDir.walkTopDown()
            .filter { it.isFile }
            .forEach { file ->
                val content = file.readText()
                if (!content.endsWith("\n")) {
                    file.appendText("\n")
                    println("Added newline to: ${file.absolutePath}")
                }
            }
    }
}
