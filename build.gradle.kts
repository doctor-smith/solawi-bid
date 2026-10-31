import io.gitlab.arturbosch.detekt.Detekt

allprojects {
    repositories {
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap") }
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        google()
        mavenLocal()
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
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.evoleq.fp.axioms) apply false
}

val gitDirectory = rootProject.file(".git")
val hooksDirectory = rootProject.file(".githooks")

if (gitDirectory.exists() && hooksDirectory.isDirectory) {
    val currentHooksPath = providers.exec {
        commandLine("git", "config", "--local", "--get", "core.hooksPath")
        isIgnoreExitValue = true
    }.standardOutput.asText.get().trim()

    if (currentHooksPath != ".githooks") {
        providers.exec {
            commandLine(
                "git",
                "config",
                "--local",
                "core.hooksPath",
                ".githooks"
            )
        }.result.get()

        logger.lifecycle("Git pre-commit hooks configured successfully.")
    }
}


tasks.register<Exec>("installGitHooks") {
    group = "setup"
    description = "Configure Git to use project hooks"

    commandLine(
        "git",
        "config",
        "core.hooksPath",
        ".githooks"
    )
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

val stagedOnly = providers.gradleProperty("stagedOnly")
    .map(String::toBoolean)
    .getOrElse(false)

if (stagedOnly) {

    val stagedFiles = providers.exec {
        commandLine(
            "git",
            "diff",
            "--cached",
            "--name-only",
            "--diff-filter=ACMR",
            "-z"
        )
    }.standardOutput.asText.get()
        .split('\u0000')
        .filter { it.endsWith(".kt") || it.endsWith(".kts") }
        .map { rootProject.file(it).canonicalFile }
        .filter { it.isFile }
        .toSet()

    allprojects {
        plugins.withId("io.gitlab.arturbosch.detekt") {

            tasks.withType<Detekt>().configureEach {

                // Ursprüngliche Quellen des jeweiligen Detekt-Tasks
                val originalSources = source

                // Nur gestagte Dateien, die zum Task gehören
                val relevantFiles = originalSources.asFileTree
                    .matching {
                        include("**/*.kt", "**/*.kts")
                    }
                    .files
                    .filter { it.canonicalFile in stagedFiles }

                setSource(files(relevantFiles))

                onlyIf {
                    relevantFiles.isNotEmpty()
                }
            }
        }
    }
}

tasks.register("analyseDependencies") {
    group = "architect"
    description = "Analyse all dependencies and generate reports"

    // Sammelt alle relevanten Tasks aus allen Projekten
    val analyserTasks = rootProject.allprojects.flatMap { project ->
        project.tasks.matching {
            it.name.endsWith("DependencyAnalyser") || it.name.endsWith("HasCyclicDependencies")
        }
    }

    // Macht diese Tasks zu Abhängigkeiten für den Wrapper-Task
    dependsOn(analyserTasks)

    doLast {
        println("✅ All dependency analysis tasks were triggered.")
        println("Wrapper task depends on ${analyserTasks.size} tasks.")
    }
}
