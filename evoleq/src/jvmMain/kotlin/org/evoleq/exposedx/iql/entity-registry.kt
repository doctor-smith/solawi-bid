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
class Registry(
    private val fieldNameStrategy: FieldNameStrategy = FieldNameStrategy.EXACT,
    private val entityNameStrategy: EntityNameStrategy = EntityNameStrategy.EXACT,
    internal val nullSortOrder: NullSortOrder = NullSortOrder.FIRST,
) {

    private val entities =
        mutableMapOf<String, EntityType>()

    fun entities() = entities

    private val tables =
        mutableMapOf<String, Table>()

    private val mappingTables =
        mutableMapOf<String, Table>()

    private val columns =
        mutableMapOf<String, MutableMap<String, Column<*>>>()

    private val fieldDefinitions =
        mutableMapOf<FieldType, FieldDefinition>()
    private val pendingRelations =
        mutableListOf<PendingRelation>()

    init {
        registerDefaultFieldDefinitions()
    }

    // -------------------------------------------------------------------------
    // Entities
    // -------------------------------------------------------------------------

    private fun resolveColumnName(
        fieldName: String
    ): String =
        when (fieldNameStrategy) {
            FieldNameStrategy.EXACT ->
                fieldName

            FieldNameStrategy.SNAKE_CASE ->
                fieldName.toSnakeCase()
        }


    fun registerEntity(
        entity: EntityType,
        table: Table
    ) {
        val name = entityNameStrategy.apply(entity.name)

        require(name !in entities) {
            "Entity already registered: ${entity.name}"
        }

        val normalized = entity.copy(name = name)

        entities[name] = normalized
        tables[name] = table

        columns[name] =
            entity.fields.keys.associateWith { fieldName ->

                val columnName = resolveColumnName(fieldName)

                table.columns.firstOrNull {
                    it.name == columnName
                } ?: error(
                    "Column '$columnName' for field " +
                            "'${entity.name}.$fieldName' not found " +
                            "in table '${entity.table}'"
                )
            }.toMutableMap()
    }

    fun merge(other: Registry) {

        other.entities.filter{(name, _) -> !entities.containsKey(name)}.forEach { (name, entity) ->

            require(!entities.containsKey(name)) {
                "Entity '$name' is already registered"
            }

            registerEntity(
                entity,
                other.getEntityTable(name)
            )
        }

        other.mappingTables.forEach { (name, table) ->
            require(name !in mappingTables) {
                "Mapping table '$name' is already registered"
            }

            mappingTables[name] = table
        }
    }

    internal fun registerMappingTable(
        name: String,
        table: Table
    ) {
        if(mappingTables[name] != null) {
            assert(mappingTables[name] == table) { "Mapping table already registered: $name" }
            return
        }

        mappingTables[name] = table
    }

    fun registerMappingTable(table: Table) {
        registerMappingTable(table.tableName, table)
    }

    fun getEntity(name: String): EntityType? =
        entities[name]

    fun getEntityOrThrow(name: String): EntityType =
        getEntity(name)
            ?: error("Unknown entity: $name")

    fun put(entity: EntityType) {
        entities[entity.name] = entity
    }

    fun getEntityTable(name: String): Table =
        tables[name]
            ?: error(
                "No Exposed table registered for entity: $name"
            )

    fun getEntityByTable(table: Table): EntityType {
        return entities.values.firstOrNull {
            it.table == table.tableName
        } ?: error(
            "No entity registered for table '${table.tableName}'"
        )
    }

    fun getFieldType(
        table: Table,
        column: Column<*>
    ): FieldType {

        val entity =
            getEntityByTable(table)

        return entity.fields.values
            .firstOrNull { it.name == column.name }
            ?.type
            ?: error(
                "No field registered for column '${column.name}' " +
                        "on entity '${entity.name}'"
            )
    }

    fun getMappingTable(name: String): Table =
        mappingTables[name]
            ?: error("No Exposed mapping table registered: $name")

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

    internal fun registerPendingRelations(
        relations: Collection<PendingRelation>
    ) {
        pendingRelations += relations
    }
    internal fun resolvePendingRelations() {

        pendingRelations.forEach { pending ->

            pending.mappingTable?.let {
                registerMappingTable(it)
            }

            val sourceEntity =
                getEntityOrThrow(pending.sourceEntity)

            val relation =
                sourceEntity.relations[pending.relationName]
                    ?: error(
                        "Relation '${pending.sourceEntity}.${pending.relationName}' not found"
                    )

            // If the target table is registered as an entity, use its
            // logical entity name. Otherwise fall back to the physical
            // table name.
            val targetEntityName =
                entities.values
                    .firstOrNull { entity ->
                        getEntityTable(entity.name) == pending.targetTable
                    }
                    ?.name
                    ?: pending.targetTable.tableName

            val updatedEntity =
                sourceEntity.copy(
                    relations =
                        sourceEntity.relations.toMutableMap().apply {
                            this[pending.relationName] =
                                relation.copy(
                                    targetEntity = targetEntityName
                                )
                        }
                )

            entities[sourceEntity.name] = updatedEntity
        }

        pendingRelations.clear()
    }

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
