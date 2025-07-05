package org.evoleq.architecture.dependency

import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer

open class DependencyAnalyserConfig(var name: String) {
    //var name: String
    var domain: String = ""
    var sourceSet: String = ""
    var appModule: String = "application"
    var modulePath: String = "module"
    var modules: Set<String> = setOf()
    var targetFile: String = "dependency-analysis"

}

open class DependencyAnalyserExtension(
    val dependencyAnalyser:  NamedDomainObjectContainer<DependencyAnalyserConfig>
) {
    fun analyse(name: String, action: Action<DependencyAnalyserConfig>) {
        val config = dependencyAnalyser.maybeCreate(name)
        action.execute(config)
    }
}