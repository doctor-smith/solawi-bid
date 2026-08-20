package org.evoleq.exposedx.iql

import org.evoleq.iql.data.EntityType
import org.evoleq.iql.data.FieldInfo
import org.evoleq.iql.data.RelationInfo
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table


/**
 * Runtime registry.
 *
 * EntityRegistry contains serializable IQL metadata.
 * Registry additionally binds that metadata to actual Exposed Tables/Columns.
 */
class Registry {

    private val entities = mutableMapOf<String, EntityType>()

    private val tables = mutableMapOf<String, Table>()

    private val columns =
        mutableMapOf<String, MutableMap<String, Column<*>>>()

    fun registerEntity(
        entity: EntityType,
        table: Table
    ) {
        require(entity.name !in entities) {
            "Entity already registered: ${entity.name}"
        }

        entities[entity.name] = entity
        tables[entity.name] = table

        columns[entity.name] =
            entity.fields.keys.associateWith { fieldName ->
                table.columns.firstOrNull {
                    it.name == fieldName
                } ?: error(
                    "Column '$fieldName' not found in table '${entity.table}'"
                )
            }.toMutableMap()
    }

    fun getEntity(name: String): EntityType? =
        entities[name]

    fun getEntityOrThrow(name: String): EntityType =
        getEntity(name)
            ?: error("Unknown entity: $name")

    fun getEntityTable(name: String): Table =
        tables[name]
            ?: error("No Exposed table registered for entity: $name")

    fun getField(
        entityName: String,
        fieldName: String
    ): FieldInfo =
        getEntityOrThrow(entityName)
            .fields[fieldName]
            ?: error(
                "Unknown field '$entityName.$fieldName'"
            )

    fun getColumn(
        entityName: String,
        fieldName: String
    ): Column<*> =
        columns[entityName]
            ?.get(fieldName)
            ?: error(
                "No Exposed column registered for '$entityName.$fieldName'"
            )

    fun getRelation(
        entityName: String,
        relationName: String
    ): RelationInfo =
        getEntityOrThrow(entityName)
            .relations[relationName]
            ?: error(
                "Unknown relation '$entityName.$relationName'"
            )

    fun validateField(
        entityName: String,
        fieldName: String
    ): Boolean =
        entities[entityName]
            ?.fields
            ?.containsKey(fieldName)
            ?: false

    fun validateRelation(
        entityName: String,
        relationName: String
    ): Boolean =
        entities[entityName]
            ?.relations
            ?.containsKey(relationName)
            ?: false
}
