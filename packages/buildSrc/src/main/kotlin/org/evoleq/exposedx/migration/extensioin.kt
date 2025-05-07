package org.evoleq.exposedx.migration


import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer

open class MigrationConfig(var name: String) {
    //var name: String
    var domain: String = ""
    var module: String = ""
    var migrations: String = ""
    var sourceSet: String = ""

}

open class MigrationPluginExtension(
    val migrations: NamedDomainObjectContainer<MigrationConfig>
) {
    fun migration(name: String, action: Action<MigrationConfig>) {
        val config = migrations.maybeCreate(name)
        action.execute(config)
    }
}