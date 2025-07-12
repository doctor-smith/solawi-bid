package org.evoleq.architecture.dependency.task

import org.evoleq.architecture.dependency.task.computation.computeAppToModuleDependencies
import org.evoleq.architecture.dependency.task.computation.computeModuleDependencies
import org.evoleq.architecture.dependency.task.computation.hasCyclicAppToModuleDependencies
import org.evoleq.architecture.dependency.task.computation.hasCyclicModuleDependencies
import org.evoleq.architecture.dependency.task.computation.toIO
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
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

    @Input
    var checkAppModalDependencies: Boolean = true

    @Input
    var checkModalDependencies: Boolean = true

    @TaskAction
    fun generateGraph() {

        // Compute all dependencies between application, module1, ... , moduleN
        // both directions
        // First we look at the deps between app <-> mod
        val root = "${project.rootDir.absolutePath}/${project.path.drop(   1)}/src/$sourceSet/kotlin/${domain.replace(".", "/")}"
        val appToModuleDependencies = when{
            checkAppModalDependencies -> computeAppToModuleDependencies(
                root,
                domain,
                appModule,
                modulePath,
                modules
            )
            else -> listOf()
        }

        val criticalModules = appToModuleDependencies.filter { it.dependsOn == appModule }
        val nodeModules = """ subgraph Critical Modules
            |    ${criticalModules.joinToString("\n") { 
                it.module
            }}
            |end
        """.trimMargin()

        val arrows = appToModuleDependencies.joinToString("\n") {
                dependency -> """ 
                    |    ${dependency.module} --> ${dependency.dependsOn} 
                """.trimMargin()
            }

        val criticalArrows = appToModuleDependencies.filter { it.dependsOn == appModule }
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

        val moduleDependencies = when{
            checkModalDependencies -> computeModuleDependencies(
                root,
                domain,
                modulePath,
                modules
            )
            else -> listOf()
        }// .sortedBy { dependency -> dependency. }
        val order = moduleDependencies.toIO().withIndex().associateBy { it.value.module }
        val moduleArrows = moduleDependencies.sortedBy{ order[it.module]!!.index }
            .joinToString("\n") {
                dependency -> """ 
                    |    ${dependency.module} --> ${dependency.dependsOn} 
                """.trimMargin()
        }
        val moduleImportsByModuleAndFile = moduleDependencies.groupBy  { it.module }.entries.joinToString("\n\n") {
            (module, dependencies) -> """
                |
                |### module: $module
                |
                |  ${dependencies.joinToString("\n") { dependency -> """
                |
                |  * dependency: ${dependency.dependsOn}
                |  
                |  ${dependency.uses.joinToString("\n") { obj -> obj.imports.joinToString("\n\n") { import -> 
                    "    * $import"
                }}}
                """.trimMargin()
                        
                }}
            """.trimMargin()

        }

        val report = File(root, "$targetFile.$reportType")

        val appModalDependencies = """
            |## Dependency Graph App <-> Modules
            |Background: App is allowed to depend on modules, but not the other way round
            |
            |```mermaid
            |graph TD
            |${if(criticalModules.isNotEmpty()){nodeModules}else{""}}
            |$arrows
            |```
            |${
            if (criticalModules.isNotEmpty()) {"""
            |### Critical Dependencies:
            |```mermaid
            |graph TD
            |$renderedCriticalArrows
            |```
            |
            |<details>
            |$criticalImportsByModuleAndFile
            |</details>
            |"""
            }else{ "" }}
        """.trimMargin()

        val modalDependencies = """
            |## Dependencies between modules
            |In principle, all dependencies are allowed. But there should be no cycles in the graph 
            |```mermaid
            |graph TD
            |$moduleArrows
            |```
            |<details>
            |$moduleImportsByModuleAndFile
            |</details>
        """.trimMargin()

        report.writeText("""
            |# Dependency Analysis
            |
            |$appModalDependencies
            |
            |$modalDependencies
            |
        """.trimMargin()

        )
    }
}


abstract class DetectCyclicDependenciesTask : DefaultTask() {
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

    @Input
    var checkAppModalDependencies: Boolean = true

    @Input
    var checkModalDependencies: Boolean = true

    @TaskAction
    fun hasCyclicDependencies() {
        // Compute all dependencies between application, module1, ... , moduleN
        // both directions
        // First we look at the deps between app <-> mod
        val root =
            "/${project.rootDir.absolutePath}/${project.path.drop(1)}/src/$sourceSet/kotlin/${domain.replace(".", "/")}"
        val appToModuleDependencies = when{
            checkAppModalDependencies -> computeAppToModuleDependencies(
                root,
                domain,
                appModule,
                modulePath,
                modules
            )
            else -> listOf()
        }

        val moduleDependencies = when{
            checkModalDependencies -> computeModuleDependencies(
                root,
                domain,
                modulePath,
                modules
            )
            else -> listOf()
        }

        val hasCyclicAppToModuleDependencies = appToModuleDependencies.hasCyclicAppToModuleDependencies(appModule)
        val hasCyclicModuleDependencies = moduleDependencies.hasCyclicModuleDependencies()

        when{
            hasCyclicAppToModuleDependencies && hasCyclicModuleDependencies -> throw GradleException("There are cyclic module dependencies and some modules depend on $appModule")
            hasCyclicAppToModuleDependencies -> throw GradleException("Some module depends on $appModule")
            hasCyclicModuleDependencies -> throw GradleException("There are cyclic module dependencies")
        }
    }
}
