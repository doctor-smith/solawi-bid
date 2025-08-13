package org.evoleq.exposedx.migrations

sealed class MigrationException(override val message : String) : Exception(message) {
    data class NoSuchColumn(val columnName: String): MigrationException("No such column $columnName")
    data class NoSuchColumnDef(val columnName: String): MigrationException("No such column def $columnName")
}
