package org.evoleq.architecture.dependency

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class DependencyAnalyserTask : DefaultTask() {
    @Input
    var nameOf: String = ""

    @Input
    var domain: String = ""

    @Input
    var modulePath: String = ""

    @Input
    var appModule: String = ""

    @Input
    var modules: Set<String> = setOf()

    @Input
    var sourceSet: String = ""

    @Input
    var targetFile: String = ""

    @Input
    var reportType: String = "md"

    @TaskAction
    fun generateGraph() {
        println(
            """Generating graph of intermediate dependencies 
                | domain = $domain
                | sourceSet = $sourceSet
                | modules = ${modules.joinToString(", ") { it.toString() }}
            """.trimMargin()
        )

        // Compute all dependencies between application, module1, ... , moduleN
        // both directions
        // First we look at the deps between app <-> mod
        val root = "${project.rootDir.absolutePath}/${project.path.drop(   1)}/src/$sourceSet/kotlin/${domain.replace(".", "/")}"

        val dependencies = mutableListOf<Dependency>()

        val appImports = getAllKtFilesInPackage("$root/$appModule")

        modules.forEach { module ->
            val kotlinFiles = getAllKtFilesInPackage("$root/$modulePath/$module")
            val moduleUsesApp = kotlinFiles.map {
                UsingObj(
                    it ,
                    readImportsFromFile(it)
                        .filter { import -> import.startsWith("$domain.$appModule") }.toSet()
                )
            }.filter { it.imports.isNotEmpty()}

            if(moduleUsesApp.isNotEmpty()) {
                dependencies.add(
                    0, Dependency(
                         "$modulePath.$module",appModule, moduleUsesApp
                    )
                )
            }

            val appUsesModule = appImports.map {
                UsingObj(
                    it,
                    readImportsFromFile(it)
                        .filter { import -> import.startsWith("$domain.$modulePath.$module") }.toSet()
                )
            }.filter { it.imports.isNotEmpty()}
            if(appUsesModule.isNotEmpty()) {
                dependencies.add(
                    0, Dependency(
                        appModule,"$modulePath.$module",  appUsesModule
                    )
                )
            }
        }
        val nodeModules = """ subgraph Critical Modules
            |    ${dependencies.filter { it.dependsOn == appModule }.joinToString("\n") { 
                it.module
            }}
            |end
        """.trimMargin()

        val arrows = dependencies.joinToString("\n") {
                dependency -> """ 
                    |    ${dependency.module} --> ${dependency.dependsOn} 
                """.trimMargin()
            }

        val criticalArrows = dependencies.filter { it.dependsOn == appModule }
        val renderedCriticalArrows = criticalArrows.joinToString("\n") { "${it.module} --> ${it.dependsOn}" }

        val criticalImportsByModuleAndFile = criticalArrows.joinToString("\n\n") {
            """
                |
                |### Module: ${it.module}
                |
                |Imports:
                |${it.uses.joinToString("\n") { obj -> 
                    """
                        |* File: ${obj.file.name}
                        |
                        |  ${obj.imports.joinToString("\n") { import ->
                            "  * $import"
                    }}
                    """.trimMargin()  
            }}
            """.trimMargin()
        }

        val report = File(root, "$targetFile.$reportType")
        report.writeText("""
            |# Dependency Analysis
            |
            |## Graph
            |```mermaid
            |graph TD
            |$nodeModules
            |$arrows
            |```
            |
            |## Critical Imports:
            |```mermaid
            |graph TD
            |$renderedCriticalArrows
            |```
            |
            |$criticalImportsByModuleAndFile
            |
        """.trimMargin()

        )
    }
}

fun readImportsFromFile(filePath: String): List<String> {
    val file = File(filePath)
    return readImportsFromFile(file)
}
fun readImportsFromFile(file: File): List<String> {

    return file.readLines()
        .filter { it.trim().startsWith("import ") }
        .map { it.replace("import ", "") }
}

fun getAllKtFilesInPackage(packagePath: String): List<File> {
    val dir = File(packagePath)
    if (!dir.exists() || !dir.isDirectory) {
        error("Invalid package path: $packagePath")
    }

    return dir.walkTopDown()
        .filter { it.isFile && it.extension == "kt" }
        .toList()
}

data class Dependency(
    val module: String,
    val dependsOn: String,
    val uses: List<UsingObj>
)

data class UsingObj(
    val file: File,
    val imports: Set<String>
)