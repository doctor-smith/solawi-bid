package org.evoleq.exposedx.iql

import org.evoleq.configuration.Configuration
import org.evoleq.iql.data.*
import org.jetbrains.exposed.sql.*
import kotlin.reflect.KClass

@DslMarker
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class IqlRegistryDsl

fun registry(block: RegistryConfiguration.()->Unit): Registry = 
    RegistryConfiguration().apply{block()}.configure()

@IqlRegistryDsl
class RegistryConfiguration : Configuration<Registry> {
    
    lateinit var fieldNameStrategy: FieldNameStrategy
    lateinit var entityNameStrategy: EntityNameStrategy

    lateinit var nullSortOrder: NullSortOrder

    val fieldTypes =
        defaultFieldTypeRegistry()



    val configurations: MutableList<Registry.()->Unit> = mutableListOf()
    
    override fun configure(): Registry {

        if(!::fieldNameStrategy.isInitialized) {
            fieldNameStrategy = FieldNameStrategy.EXACT
        }

        if(!::entityNameStrategy.isInitialized) {
            entityNameStrategy = EntityNameStrategy.EXACT
        }

        if(!::nullSortOrder.isInitialized) {
            nullSortOrder = NullSortOrder.FIRST
        }

        return Registry(
            fieldNameStrategy,
            entityNameStrategy,
            nullSortOrder
        ).apply{
            configurations.forEach{it()}
            resolvePendingRelations()
        }
    }

    fun fieldType(
        columnType: KClass<out IColumnType>,
        fieldType: FieldType
    ) {
        fieldTypes.register(
            columnType,
            fieldType
        )
    }

    fun fieldTypes(
        vararg mappings: Pair<KClass<out IColumnType>, FieldType>
    ) {
        mappings.forEach { (columnType, fieldType) ->
            fieldTypes.register(
                columnType,
                fieldType
            )
        }
    }

    fun overrideFieldTypes(
        vararg mappings: Pair<KClass<out IColumnType>, FieldType>
    ) {
        mappings.forEach { (columnType, fieldType) ->
            fieldTypes.override(columnType, fieldType)
        }
    }

    fun include(registry: Registry) {
        configurations += {
            merge(registry)
        }
    }

    fun RegistryConfiguration.include(
        block: RegistryConfiguration.() -> Unit
    ) {
        RegistryConfiguration()
            .apply {
                fieldNameStrategy = this@RegistryConfiguration.fieldNameStrategy
                block()
            }
            .configure()
            .also(::include)
    }

    fun entity(
        name: String, 
        table: Table,
        entityType: EntityTypeConfiguration.()-> Unit
    ) {
        val config: Registry.()->Unit  =  {

        val entityConfig =
            EntityTypeConfiguration().apply {
            this.resolveFieldType = {
                column: Column<*> -> FieldTypeResolver(this@RegistryConfiguration.fieldTypes).resolve(column)
            }
            this.name = name
            this.table = table
            entityType()
            this.name = name
            this.table = table
        }

        val configuredEntityType = entityConfig.configure()
        entityConfig.pendingRelations.forEach { pending ->
            pending.mappingTable?.let {
                registerMappingTable(table)
            }
        }
        registerEntity(
            configuredEntityType,
            table
        )
/*
        entityConfig.pendingRelations.forEach { pending ->
            pending.mappingTable?.let {
                registerMappingTable(table)
            }
        }


 */
        registerPendingRelations(
            entityConfig.pendingRelations
        )


    }
        configurations += config
    }

    fun extend(
        entityName: String,
        block: EntityTypeConfiguration.() -> Unit
    ) {
        val config: Registry.() -> Unit = {

            val existing = getEntity(entityName)?: throw IllegalArgumentException(
                "Unknown entity: \$entityName",
            )

            val configuration =
                EntityTypeConfiguration(
                    existing,
                    getEntityTable(entityName)
                ) {
                    FieldTypeResolver(this@RegistryConfiguration.fieldTypes).resolve(this)
                }
            configuration.apply(block)

            val entity = configuration.configure()

            put(entity)

            registerPendingRelations( configuration.pendingRelations )
        }
        configurations += config
    }

}

infix fun KClass<out IColumnType>.mapsTo(
    fieldType: FieldType
): Pair<KClass<out IColumnType>, FieldType> =
    this to fieldType

internal data class PendingRelation(
    val sourceEntity: String,
    val relationName: String,
    val targetTable: Table,
    val mappingTable: Table? = null
)

@IqlRegistryDsl
class EntityTypeConfiguration : Configuration<EntityType> {

    lateinit var resolveFieldType: Column<*>.() -> FieldType

    lateinit var name: String
    lateinit var table: Table
    private val fields: MutableMap<String, FieldInfo> = mutableMapOf()
    private val relations: MutableMap<String, RelationInfo> = mutableMapOf()
    internal val pendingRelations: MutableList<PendingRelation> = mutableListOf()

    constructor()

    internal constructor(
        entity: EntityType,
        table: Table,
        resolveFieldType: Column<*>.() -> FieldType
    ) {
        name = entity.name
        this.table = table
        this.resolveFieldType = resolveFieldType

        fields.putAll(entity.fields)
        relations.putAll(entity.relations)
    }

    override fun configure(): EntityType {
        return EntityType(
            name,
            table.tableName,
            fields,
            relations
        )
    }

    fun field(column: Column<*>) {
        require(column.table == table) {
            "Wrong table"
        }

        require(column.name !in fields) {
            "Field '${column.name}' is already registered on entity '$name'"
        }

        require(column.name !in relations) {
            "Field '${column.name}' conflicts with relation '${column.name}' on entity '$name'"
        }

        fields[column.name] = FieldInfo(
            column.name,
            column.resolveFieldType(),
            column.columnType.nullable
        )
    }

    fun fields(vararg columns: Column<*>) {
        columns.forEach { field(it) }
    }

    fun oneToMany(
        relationName: String,
        targetTable: Table,
        block: ColumnReferenceConfiguration.() -> Unit
    ) {
        requireRelationNameAvailable(relationName)

        val reference =
            ColumnReferenceConfiguration(
                relationName,
                targetTable
            )
                .apply(block)
                .configure()

        require(reference.source.table == table) {
            "Source column must be from the current table"
        }

        require(reference.target.table == targetTable) {
            "Referenced column must be from the target table"
        }

        relations[relationName] =
            RelationInfo(
                name = relationName,
                type = RelationType.ONE_TO_MANY,
                targetEntity = targetTable.tableName,
                joinColumns = listOf(reference.source.name),
                inverseJoinColumn = reference.target.name
            )
        pendingRelations += PendingRelation(
            sourceEntity = name,
            relationName = relationName,
            targetTable = targetTable
        )
    }

    fun manyToOne(
        relationName: String,
        targetTable: Table,
        block: ColumnReferenceConfiguration.() -> Unit
    ) {
        requireRelationNameAvailable(relationName)

        val reference =
            ColumnReferenceConfiguration(
                relationName,
                targetTable
            ).apply(block).configure()

        require(reference.source.table == table) {
            "Source column must be from the current table"
        }

        require(reference.target.table == targetTable) {
            "Referenced column must be from the target table"
        }

        relations[relationName] =
            RelationInfo(
                name = relationName,
                type = RelationType.MANY_TO_ONE,
                targetEntity = targetTable.tableName,
                joinColumns = listOf(reference.source.name),
                inverseJoinColumn = reference.target.name
            )

        pendingRelations += PendingRelation(
            sourceEntity = name,
            relationName = relationName,
            targetTable = targetTable
        )
    }

    fun manyToMany(
        relationName: String,
        targetTable: Table,
        mappingTable: Table,
        block: ManyToManyConfiguration.() -> Unit
    ) {
        requireRelationNameAvailable(relationName)

        val mapping =
            ManyToManyConfiguration(
                name = relationName,
                sourceTable = table,
                targetTable = targetTable,
                mappingTable = mappingTable
            )
                .apply(block)
                .configure()

        relations[relationName] =
            RelationInfo(
                name = relationName,
                type = RelationType.MANY_TO_MANY,
                targetEntity = targetTable.tableName,
                joinColumns = mapping.sourceColumns,
                mapping = mapping
            )

        pendingRelations += PendingRelation(
            sourceEntity = name,
            relationName = relationName,
            targetTable = targetTable,
            mappingTable = mappingTable
        )
    }

    private fun requireRelationNameAvailable(
        relationName: String
    ) {
        require(relationName !in relations) {
            "Relation '$relationName' is already registered on entity '$name'"
        }

        require(relationName !in fields) {
            "Relation '$relationName' conflicts with field '$relationName' on entity '$name'"
        }
    }
}

fun Column<*>.fieldType(): FieldType = try {
    columnType.fieldType()
} catch (e: Throwable) {
    fieldTypeFromSqlType()
}

fun IColumnType.fieldType(): FieldType =
    when (this) {

        is EntityIDColumnType<*> ->
            idColumn.fieldType()

        is UUIDColumnType ->
            FieldType.UUID

        is TextColumnType,
        is VarCharColumnType,
        is StringColumnType->
            FieldType.STRING

        is IntegerColumnType ->
            FieldType.INTEGER

        is LongColumnType ->
            FieldType.LONG

        is DoubleColumnType,
        is FloatColumnType,
        is DecimalColumnType ->
            FieldType.DOUBLE

        is BooleanColumnType ->
            FieldType.BOOLEAN

        else -> error(
            "Unsupported Exposed column type " +
                    "'${this::class.simpleName}' " +
                    "(SQL type '${sqlType()}')"
        )

    }

private fun Column<*>.fieldTypeFromSqlType(): FieldType {
    val sqlType =
        columnType
            .sqlType()
            .substringBefore("(")
            .trim()
            .uppercase()

    return when (sqlType) {
        "DATE" ->
            FieldType.DATE

        "TIMESTAMP",
        "DATETIME" ->
            FieldType.DATETIME

        else -> unsupportedColumnType()
    }
}
private fun Column<*>.unsupportedColumnType(): Nothing =
    error(
        "Unsupported Exposed column type " +
                "'${columnType::class.simpleName}' " +
                "(SQL type '${columnType.sqlType()}') " +
                "for column '$name'"
    )

data class ColumnReference(
    val source: Column<*>,
    val target: Column<*>
)

infix fun Column<*>.references(
    target: Column<*>
): () -> ColumnReference = {
    ColumnReference(
        source = this,
        target = target
    )
}

@IqlRegistryDsl
class ColumnReferenceConfiguration(
    val name: String,
    val targetTable: Table
) : Configuration<ColumnReference> {
    private var reference: ColumnReference? = null

    infix fun Column<*>.references(
        target: Column<*>
    ) {
        reference = ColumnReference(
            source = this,
            target = target
        )
    }

    override fun configure(): ColumnReference =
        requireNotNull(reference) {
            "Relation '$name' requires a column reference"
        }
}

@IqlRegistryDsl
class ManyToManyConfiguration(
    val name: String,
    val sourceTable: Table,
    val targetTable: Table,
    val mappingTable: Table
) : Configuration<MappingInfo> {

    private var sourceReferences: List<ColumnReference> = emptyList()
    private var targetReferences: List<ColumnReference> = emptyList()

    fun source(vararg references: () -> ColumnReference) {
        sourceReferences = references.map { it() }
    }

    fun target(vararg references: () -> ColumnReference) {
        targetReferences = references.map { it() }
    }

    override fun configure(): MappingInfo {

        require(sourceReferences.isNotEmpty()) {
            "Relation '$name' requires at least one source mapping reference"
        }

        require(targetReferences.isNotEmpty()) {
            "Relation '$name' requires at least one target mapping reference"
        }

        require(sourceReferences.size == targetReferences.size) {
            "Relation '$name' has ${sourceReferences.size} source references " +
                    "but ${targetReferences.size} target references"
        }

        sourceReferences.forEach { reference ->
            require(reference.source.table == sourceTable) {
                "Source column '${reference.source.name}' must be from the source table"
            }

            require(reference.target.table == mappingTable) {
                "Source mapping column '${reference.target.name}' must be from the mapping table"
            }
        }

        targetReferences.forEach { reference ->
            require(reference.source.table == targetTable) {
                "Target column '${reference.source.name}' must be from the target table"
            }

            require(reference.target.table == mappingTable) {
                "Target mapping column '${reference.target.name}' must be from the mapping table"
            }
        }

        return MappingInfo(
            table = mappingTable.tableName,

            sourceColumns =
                sourceReferences.map { it.source.name },

            mappingSourceColumns =
                sourceReferences.map { it.target.name },

            mappingTargetColumns =
                targetReferences.map { it.target.name },

            targetColumns =
                targetReferences.map { it.source.name }
        )
    }
}
