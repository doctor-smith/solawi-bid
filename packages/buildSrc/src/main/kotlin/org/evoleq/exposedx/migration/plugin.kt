package org.evoleq.exposedx.migration

import org.gradle.api.Plugin
import org.gradle.api.Project

class MigrationPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // Create a container for MigrationConfig objects
        val migrationConfigs = project.container(MigrationConfig::class.java)

        // Register the extension with the container
        val extension = project.extensions.create(
            "migrations",
            MigrationPluginExtension::class.java,
            migrationConfigs
        )

        project.afterEvaluate {
            extension.migrations.forEach { config ->
                project.tasks.register(
                    //"${config.module}${config.sourceSet.uppercaseFirstChar()}GenerateMigration",
                    "${config.name}GenerateMigration",
                    GenerateMigrationTask::class.java
                ) {  ->
                    group = "migrations"
                    domain = config.domain
                    module = config.module
                    migrations = config.migrations
                    sourceSet = config.sourceSet
                }
            }
        }
    }
/*
        // val generateMigrationTask =
        project.tasks.register(
            "${extension.module}${extension.sourceSet.uppercaseFirstChar()}-generate-migration",
            GenerateMigrationTask::class.java
        ) {

            // Configure the task with values from the extension
            domain = extension.domain
            module = extension.module
            migrations = extension.migrations
            sourceSet = extension.sourceSet
            /*
            group = "migration"
            doLast {
                val id = System.currentTimeMillis()
                val packageName = if(module != "application"){
                    "$domain.module.$module.$migrations"
                }else{
                    "$domain.$module.$migrations"
                }

                val migrationText = generateMigration(
                    id,
                    packageName
                )

                val file = File(
                    "${project.rootDir.absolutePath}/${project.name}/src/$sourceSet/kotlin/${
                        domain.replace(
                            ".",
                            "/"
                        )
                    }${if(module != "application"){"/module"}else {""}}/$module/$migrations/migration-$id.kt"
                )
                println(file.absolutePath)

                file.writeText(migrationText)
                println(migrationText)

                val migrationsFolder = file.parentFile
                with(migrationsFolder) {
                    File(this, "migrations.kt").writeText(
                        buildMigrationList(
                            packageName,
                            module
                        )
                    )
                }
                //println(file.parentFile.buildMigrationList())
            }.finalizedBy(
                //"licenseFormat"
            )
            */
        }
    }

 */

}