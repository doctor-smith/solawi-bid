package org.evoleq.architecture.dependency.task.computation

import org.evoleq.architecture.dependency.task.data.Dependency
import org.evoleq.architecture.dependency.task.data.IO
import org.evoleq.architecture.dependency.task.data.Node
import org.evoleq.architecture.dependency.task.data.Nodes
import org.evoleq.architecture.dependency.task.data.UsingObj

/**
 * Compute dependencies between the application package and modules
 */
fun computeAppToModuleDependencies(
    root: String,
    domain: String,
    appModule: String,
    modulePath: String,
    modules: Set<String>
): List<Dependency> {
    val dependencies = mutableListOf<Dependency>()

    val appImports = getAllKtFilesInPackage("$root/$appModule")

    modules.forEach { module ->
        val kotlinFiles = getAllKtFilesInPackage("$root/$modulePath/$module".cleanPath('/', true))
        val moduleUsesApp = kotlinFiles.map {
            UsingObj(
                it,
                readImportsFromFile(it)
                    .filter { import -> import.startsWith("$domain.$appModule") }.toSet()
            )
        }.filter { it.imports.isNotEmpty()}

        if(moduleUsesApp.isNotEmpty()) {
            dependencies.add(
                0, Dependency(
                    "$modulePath.$module".asPackage().cleanPath('.'),appModule, moduleUsesApp
                )
            )
        }

        val appUsesModule = appImports.map {
            UsingObj(
                it,
                readImportsFromFile(it)
                    .filter { import -> import.startsWith("$domain.$modulePath.$module".asPackage().cleanPath('.')) }.toSet()
            )
        }.filter { it.imports.isNotEmpty()}
        if(appUsesModule.isNotEmpty()) {
            dependencies.add(
                0, Dependency(
                    appModule,"$modulePath.$module".asPackage().cleanPath('.'),  appUsesModule
                )
            )
        }
    }
    return dependencies.filter{it.module.isNotEmpty()}
}

/**
 * Check if modules depend on the application package
 */
fun List<Dependency>.hasCyclicAppToModuleDependencies(appModule: String): Boolean {
    val sourceIsApp = filter { it.module ==  appModule}
    val sourceIsModule = filter { it.module != appModule }
    return sourceIsApp.any { (module, _ , _) -> sourceIsModule.any { it.dependsOn == module } }
}

/**
 * Compute dependencies between modules
 */
fun computeModuleDependencies(
    root: String,
    domain: String,
    modulePath: String,
    modules: Set<String>
): List<Dependency> {
    val  moduleDependencies = mutableListOf<Dependency>()
    modules.forEach { sourceModule ->
        val kotlinFiles = getAllKtFilesInPackage("$root/$modulePath/$sourceModule".asPath().cleanPath('/', true))
        modules.forEach { targetModule ->
            if(targetModule != sourceModule) {
                val sourceUsesTarget = kotlinFiles.map {
                    UsingObj(
                        it ,
                        readImportsFromFile(it)
                            .filter { import -> import.startsWith("$domain.$modulePath.$targetModule".asPackage().cleanPath('.')) }.toSet()
                    )
                }.filter { it.imports.isNotEmpty()}

                if(sourceUsesTarget.isNotEmpty()) {
                    moduleDependencies.add(
                        0, Dependency(
                            "$modulePath.$sourceModule".asPackage().cleanPath('.'),"$modulePath.$targetModule".asPackage().cleanPath('.'), sourceUsesTarget
                        )
                    )
                }

            }}
    }
    return moduleDependencies.filter{it.module.isNotEmpty() && it.dependsOn.isNotEmpty()}
}

/**
 * Check if there are cyclic dependencies between the modules
 */
fun List<Dependency>.hasCyclicModuleDependencies(): Boolean = toNodes().hasCycles()

fun List<Dependency>.toNodes(): Nodes = Nodes(
    nodes = groupBy { it.module }.entries.map { Node(
        source = it.key,
        targets = it.value.map { dependency -> dependency.dependsOn }
    )}
)

fun List<Dependency>.toIO(): List<IO> = groupBy { it.module }.map { IO(
    it.key,
    inputs = this@toIO.filter { dependency ->  dependency.dependsOn == it.key }
        .map { dependency -> dependency.module}
        .toSet(),
    it.value.map { it.dependsOn }.toSet()
) }.sortedWith(
    compareByDescending<IO>{
        - it.inputs.size
    }.thenBy {
        - it.outputs.size
    }/*.thenBy {
        it.module
    }*/
)

fun Nodes.hasCycles(): Boolean = reduce().nodes.isNotEmpty()

tailrec fun Nodes.reduce(): Nodes {
    if(nodes.isEmpty()) return this

    val sources = nodes.map { it.source }
    // Ends are nodes
    val ends = nodes.map {
        Node(it.source, it.targets.filter { target -> target  in sources })
    }.filter {
        it.targets.isEmpty()
    }.map { it.source }

    if (ends.isEmpty()) return this

    return Nodes(nodes.filter { it.source !in ends }.map { node ->
        Node(
            node.source,
            node.targets.filter { target -> target !in ends }
        )
    }).reduce()
}
