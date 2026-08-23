package org.evoleq.exposedx.iql



import kotlinx.serialization.json.*
import org.evoleq.iql.data.*

@Suppress("TooManyFunctions")
class FilterValidator(private val registry: Registry) {

    fun Filter.isValid(): Boolean = when(validate(this)){
        is ValidationResult.Valid -> true
        is ValidationResult.Invalid -> false
    }

    fun validate(filter: Filter): ValidationResult {
        return when (filter) {
            is AndFilter -> validateAndFilter(filter)
            is OrFilter -> validateOrFilter(filter)
            is NotFilter -> validateNotFilter(filter)
            is ComparisonFilter -> validateComparisonFilter(filter)
            is ExpressionComparisonFilter -> TODO()
            is InFilter -> validateInFilter(filter)
            is IsNullFilter -> validateIsNullFilter(filter)
            is QuantifierFilter -> validateQuantifierFilter(filter)
        }
    }

    private fun validateAndFilter(filter: AndFilter): ValidationResult {
        return filter.filters.all { it.isValid() }.let {
            if (it) ValidationResult.Valid else ValidationResult.Invalid("Invalid AND filter")
        }
    }

    private fun validateOrFilter(filter: OrFilter): ValidationResult {
        return filter.filters.all { it.isValid() }.let {
            if (it) ValidationResult.Valid else ValidationResult.Invalid("Invalid OR filter")
        }
    }

    private fun validateNotFilter(filter: NotFilter): ValidationResult {
        return validate(filter.filter).let {
            if (it is ValidationResult.Valid) ValidationResult.Valid else ValidationResult.Invalid("Invalid NOT filter")
        }
    }

    private fun validateComparisonFilter(filter: ComparisonFilter): ValidationResult {
        return when {
            !registry.validateField(filter.field.path.split(".").first(), filter.field.path.split(".").last()) ->
                ValidationResult.Invalid("Invalid field reference: ${filter.field.path}")
            !isValidValueForType(filter.value, filter.field) ->
                ValidationResult.Invalid("Invalid value type for field ${filter.field.path}")
            else -> ValidationResult.Valid
        }
    }

    private fun validateInFilter(filter: InFilter): ValidationResult {
        return when {
            !registry.validateField(filter.field.path.split(".").first(), filter.field.path.split(".").last()) ->
                ValidationResult.Invalid("Invalid field reference: ${filter.field.path}")
            !filter.values.all { isValidValueForType(it, filter.field) } ->
                ValidationResult.Invalid("Invalid value type in IN filter")
            else -> ValidationResult.Valid
        }
    }

    private fun validateIsNullFilter(filter: IsNullFilter): ValidationResult {
        return when {
            !registry.validateField(filter.field.path.split(".").first(), filter.field.path.split(".").last()) ->
                ValidationResult.Invalid("Invalid field reference: ${filter.field.path}")
            else -> ValidationResult.Valid
        }
    }

    private fun validateQuantifierFilter(filter: QuantifierFilter): ValidationResult {
        return when {
            !registry.validateRelation(filter.relation.entity, filter.relation.relation) ->
                ValidationResult.Invalid("Invalid relation reference: ${filter.relation.entity}.${filter.relation.relation}")
            !isValidFilterForRelation(filter.filter, filter.relation) ->
                ValidationResult.Invalid("Invalid filter for relation")
            else -> ValidationResult.Valid
        }
    }

    @Suppress("ReturnCount")
    private fun isValidValueForType(value: JsonElement, fieldRef: FieldRef): Boolean {
        val entityName = fieldRef.path.split(".").first()
        val fieldName = fieldRef.path.split(".").last()
        val entity = registry.getEntity(entityName) ?: return false
        val fieldInfo = entity.fields[fieldName] ?: return false

        return when (value) {
            is JsonPrimitive -> {
                when (fieldInfo.type) {
                    FieldType.STRING, FieldType.UUID -> value.isString
                    FieldType.INTEGER -> value.intOrNull != null
                    FieldType.LONG -> value.longOrNull != null
                    FieldType.DOUBLE -> value.doubleOrNull != null
                    FieldType.BOOLEAN -> value.booleanOrNull != null
                    else -> false
                }
            }
            is JsonObject -> false // Objekte sind für einfache Felder nicht erlaubt
            else -> false
        }
    }

    @Suppress("FunctionOnlyReturningConstant", "UnusedParameter")
    private fun isValidFilterForRelation(filter: Filter, relation: RelationRef): Boolean {
        // Diese Methode könnte komplexer werden, um die Filter für die Relation zu validieren
        return true
    }
}

sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val message: String) : ValidationResult()
}
