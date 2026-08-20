package org.evoleq.exposedx.iql

import kotlinx.serialization.json.*
import org.evoleq.iql.data.EntityType
import org.evoleq.iql.data.FieldInfo
import org.evoleq.iql.data.FieldType
import org.evoleq.iql.data.RelationInfo
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

typealias ValueTranslator = (JsonElement) -> Any

/**
 * Runtime registry.
 *
 * EntityRegistry contains serializable IQL metadata.
 * Registry additionally binds that metadata to actual
 * Exposed Tables/Columns and backend-specific field definitions.
 */
@Suppress("TooManyFunctions")
class Registry {

    private val entities =
        mutableMapOf<String, EntityType>()

    private val tables =
        mutableMapOf<String, Table>()

    private val columns =
        mutableMapOf<String, MutableMap<String, Column<*>>>()

    private val fieldDefinitions =
        mutableMapOf<FieldType, FieldDefinition>()

    init {
        registerDefaultFieldDefinitions()
    }

    // -------------------------------------------------------------------------
    // Entities
    // -------------------------------------------------------------------------

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
            ?: error(
                "No Exposed table registered for entity: $name"
            )

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    // Relations
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    // Field definitions / value translators
    // -------------------------------------------------------------------------

    fun registerFieldDefinition(
        definition: FieldDefinition
    ) {
        fieldDefinitions[definition.type] = definition
    }

    fun lookup(
        type: FieldType
    ): FieldDefinition =
        fieldDefinitions[type]
            ?: error(
                "No field definition registered for $type"
            )

    fun translator(
        type: FieldType
    ): ValueTranslator =
        lookup(type)::translate

    private fun registerDefaultFieldDefinitions() {

        registerFieldDefinition(
            PrimitiveFieldDefinition(
                type = FieldType.STRING,
                translateValue = {
                    it.jsonPrimitive.content
                }
            )
        )

        registerFieldDefinition(
            PrimitiveFieldDefinition(
                type = FieldType.INTEGER,
                translateValue = {
                    it.jsonPrimitive.int
                }
            )
        )

        registerFieldDefinition(
            PrimitiveFieldDefinition(
                type = FieldType.LONG,
                translateValue = {
                    it.jsonPrimitive.long
                }
            )
        )

        registerFieldDefinition(
            PrimitiveFieldDefinition(
                type = FieldType.DOUBLE,
                translateValue = {
                    it.jsonPrimitive.double
                }
            )
        )

        registerFieldDefinition(
            PrimitiveFieldDefinition(
                type = FieldType.BOOLEAN,
                translateValue = {
                    it.jsonPrimitive.boolean
                }
            )
        )
    }
}
