package org.evoleq.iql.data


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
sealed class Filter {
    // abstract val type: String
}

@Serializable
@SerialName("and")
data class AndFilter(
    val filters: List<Filter>
) : Filter() {
   // override val type = "and"
}

@Serializable
@SerialName("or")
data class OrFilter(
    val filters: List<Filter>
) : Filter() {
  //  override val type = "or"
}

@Serializable
@SerialName("not")
data class NotFilter(
    val filter: Filter
) : Filter() {
  //  override val type = "not"
}

@Serializable
@SerialName("comparison")
data class ComparisonFilter(
    val field: FieldRef,
    val operator: Operator,
    val value: JsonElement
) : Filter() {
 //   override val type = "comparison"
}

@Serializable
@SerialName("in")
data class InFilter(
    val field: FieldRef,
    val values: List<JsonElement>
) : Filter() {
  //  override val type = "in"
}

@Serializable
@SerialName("is_null")
data class IsNullFilter(
    val field: FieldRef
) : Filter() {
  //  override val type = "is_null"
}

@Serializable
@SerialName("quantifier")
data class QuantifierFilter(
    val quantifier: Quantifier,
    val relation: RelationRef,
    val filter: Filter
) : Filter() {
  //  override val type = "quantifier"
}

@Serializable
sealed class FieldRef {
    abstract val path: String
}

@Serializable
data class SimpleFieldRef(override val path: String) : FieldRef()

@Serializable
data class RelationFieldRef(
    override val path: String,
    val relationPath: String
) : FieldRef()

@Serializable
enum class Operator {
    EQ, NE, GT, GTE, LT, LTE
}

@Serializable
enum class Quantifier {
    ANY, ALL, NONE
}

@Serializable
data class RelationRef(
    val entity: String,
    val relation: String
)


@Serializable
data class EntityRegistry(
    val entities: Map<String, EntityType>
)

@Serializable
data class EntityType(
    val name: String,
    val table: String,
    val fields: Map<String, FieldInfo>,
    val relations: Map<String, RelationInfo>
)

@Serializable
data class FieldInfo(
    val name: String,
    val type: FieldType,
    val nullable: Boolean = false
)

@Serializable
enum class FieldType {
    STRING,
    INTEGER,
    LONG,
    DOUBLE,
    BOOLEAN,
    UUID,
    DATE,
    DATETIME
}

@Serializable
data class RelationInfo(
    val name: String,
    val type: RelationType,
    val targetEntity: String,
    val joinColumns: List<String>,
    val inverseJoinColumn: String?
)

@Serializable
enum class RelationType {
    ONE_TO_MANY,
    MANY_TO_ONE,
    MANY_TO_MANY
}
