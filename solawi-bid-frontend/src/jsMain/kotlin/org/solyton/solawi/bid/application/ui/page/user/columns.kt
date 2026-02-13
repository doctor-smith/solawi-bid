package org.solyton.solawi.bid.application.ui.page.user

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class MapDelegate<K, V>(
    private val map: Map<K, V>,
    private val key: K,
) : ReadOnlyProperty<Any?, V> {

    override fun getValue(thisRef: Any?, property: KProperty<*>): V {
        return map[key]!!
    }
}

fun <K, V> map(map: Map<K, V>, key: K) = MapDelegate(map, key)

data class ApplicationTables(val tables: Map<String, TableConfigurations>) : Map<String, TableConfigurations> by tables

data class TableConfigurations(
    val visible: Boolean,
    val tables: Map<String, TableConfiguration>
): Map<String, TableConfiguration> by tables

data class TableConfiguration(
    val visible: Boolean,
    val visibleColumns: List<String>,
)

val applicationTables by lazy {  ApplicationTables(mapOf(
    "shareManagement" to TableConfigurations(true, mapOf())
)) }

val shareManagementTables by lazy { applicationTables["shareManagement"]!!.tables }
