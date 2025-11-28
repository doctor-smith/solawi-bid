package org.solyton.solawi.bid.module.testFramework

/**
 * Assumes that the db url parameter is exposed/h2-compatible, i.e. it has the structure
 * ```
 * url = name:of:database;params
 * ```
 * Example:
 * ```
 * url = jdbc:h2:mem:application_module_test;DB_CLOSE_DELAY=-1
 * ```
 */
fun String.appendDbNameSuffix(dbNameSuffix: String): String {
    val indexOfSemiColon = this.indexOf(";")
    val name = this.substring(0, indexOfSemiColon)
    val rest = this.substring(indexOfSemiColon )
    return "$name-$dbNameSuffix;$rest"
}
