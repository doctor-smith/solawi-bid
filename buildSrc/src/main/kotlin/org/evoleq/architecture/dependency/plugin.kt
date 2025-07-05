package org.evoleq.architecture.dependency

import org.gradle.api.Plugin
import org.gradle.api.Project

class DependencyPlugin : Plugin<Project> {



    override fun apply(project: Project) {


        // Create a container for MigrationConfig objects
        val configs = project.container(DependencyAnalyserConfig::class.java)

        // Register the extension with the container
        val extension = project.extensions.create(
            "analyserConfigs",
            DependencyAnalyserExtension::class.java,
            configs
        )

        project.afterEvaluate {
            extension.dependencyAnalyser.forEach { config ->
                project.tasks.register(
                    "${config.name}DependencyAnalyser",
                    DependencyAnalyserTask::class.java
                ) {  ->
                    group = "architect"
                    domain = config.domain
                    sourceSet = config.sourceSet
                    modules = config.modules
                    modulePath = config.modulePath
                    appModule = config.appModule
                    targetFile = config.targetFile
                    reportType = "md"
                    nameOf = config.name

                }
            }
        }
    }

}