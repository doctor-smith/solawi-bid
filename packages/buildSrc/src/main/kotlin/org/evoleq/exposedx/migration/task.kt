package org.evoleq.exposedx.migration

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class GenerateMigrationTask : DefaultTask() {
    // Define task inputs that will be configured by the plugin
    @Input
    var domain: String = ""

    @Input
    var module: String = ""

    @Input
    var migrations: String = ""

    @Input
    var sourceSet: String = ""

    @TaskAction
    fun generate() {
        logger.lifecycle("Starting migration generation for: $domain:$module")
        println("Generating migration with the following configuration:")
        println("Domain: $domain")
        println("Module: $module")
        println("Migrations: $migrations")
        println("SourceSet: $sourceSet")
        // Add task logic here that uses these properties


        //doLast {
           // group = "migration"
            val id = System.currentTimeMillis()
            val packageName = if(module != "application"){
                "$domain.module.$module.$migrations"
            }else{
                "$domain.$module.$migrations"
            }

            val migrationText = generateMigration(
                id,
                packageName.replace("/",".")
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
                        packageName.replace("/","."),
                        module
                    )
                )
            }
            //println(file.parentFile.buildMigrationList())




    }

}