package org.evoleq.exposedx.iql

import org.evoleq.iql.data.FieldType
import org.jetbrains.exposed.sql.*
import kotlin.reflect.KClass

class FieldTypeRegistry {

    private val mappings =
        mutableMapOf<KClass<out IColumnType>, FieldType>()

    fun register(
        columnType: KClass<out IColumnType>,
        fieldType: FieldType
    ) {
        require(columnType !in mappings) {
            "Field type mapping for '${columnType.simpleName}' is already registered"
        }

        mappings[columnType] = fieldType
    }

    fun override(
        columnType: KClass<out IColumnType>,
        fieldType: FieldType
    ) {
        mappings[columnType] = fieldType
    }

    fun resolve(
        columnType: IColumnType
    ): FieldType {

        mappings[columnType::class]?.let {
            return it
        }

        return mappings.entries
            .firstOrNull { (type, _) ->
                type.isInstance(columnType)
            }
            ?.value
            ?: error(
                "Unsupported Exposed column type " +
                        "'${columnType::class.simpleName}' " +
                        "(SQL type '${columnType.sqlType()}')"
            )
    }
}


class FieldTypeResolver(
    private val registry: FieldTypeRegistry
) {

    fun resolve(column: Column<*>): FieldType =
        resolve(column.columnType)

    private fun resolve(
        columnType: IColumnType
    ): FieldType =
        when (columnType) {

            is EntityIDColumnType<*> ->
                resolve(columnType.idColumn)

            else ->
                registry.resolve(columnType)
        }
}


fun defaultFieldTypeRegistry() =
    FieldTypeRegistry().apply {

        register(UUIDColumnType::class, FieldType.UUID)

        register(TextColumnType::class, FieldType.STRING)
        register(VarCharColumnType::class, FieldType.STRING)
        register(StringColumnType::class, FieldType.STRING)

        register(IntegerColumnType::class, FieldType.INTEGER)
        register(LongColumnType::class, FieldType.LONG)

        register(DoubleColumnType::class, FieldType.DOUBLE)
        register(FloatColumnType::class, FieldType.DOUBLE)
        register(DecimalColumnType::class, FieldType.DOUBLE)

        register(BooleanColumnType::class, FieldType.BOOLEAN)

        // depending on your Exposed version:
        // register(DateColumnType::class, FieldType.DATE)
        // register(DateTimeColumnType::class, FieldType.DATETIME)
    }
