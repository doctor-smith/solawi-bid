package org.evoleq.api.documentation

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.JavaExec
import org.gradle.kotlin.dsl.*
import java.io.File

class ApiDocPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create<ApiDocExtension>("apiDoc")
        extension.outputFile.convention(project.layout.buildDirectory.file("docs/api-guide.md"))

        project.tasks.register<JavaExec>("generateApiDoc") {
            group = "documentation"
            dependsOn("classes")

            val javaExtension = project.extensions.getByType<JavaPluginExtension>()
            val mainSourceSet = javaExtension.sourceSets.getByName("main")

            // WICHTIG: App-Klassen + App-Abhängigkeiten
            classpath(mainSourceSet.runtimeClasspath)

            // WICHTIG: Die Klassen dieses Generators selbst hinzufügen
            // Wir suchen den Pfad, in dem UniversalDocGenerator kompiliert wurde
            val buildSrcClasses = File(project.rootDir, "buildSrc/build/classes/kotlin/main")
            classpath(buildSrcClasses)

            // Falls die package-Struktur org.evoleq.api.documentation ist:
            mainClass.set("org.evoleq.api.documentation.UniversalDocGenerator")

            doFirst {
                args(
                    extension.apiPath.get(),
                    extension.outputFile.get().asFile.absolutePath
                )
                // Debug-Ausgabe, falls es fehlschlägt
                println("Start Generator with API-Path: ${extension.apiPath.get()}")
            }
        }
    }
}