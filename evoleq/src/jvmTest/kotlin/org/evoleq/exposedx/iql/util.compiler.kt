package org.evoleq.exposedx.iql

import org.evoleq.iql.data.ComparisonFilter
import org.evoleq.iql.data.EntityType
import org.evoleq.iql.data.FieldRef
import org.evoleq.iql.data.Filter


internal fun ExposedCompiler.resolveForTest(
    filter: Filter,
    currentEntity: EntityType?
): ExposedCompiler.ResolvedField {

    require(filter is ComparisonFilter) {
        "resolveForTest currently expects ComparisonFilter"
    }

    return resolveField(
        field = filter.field,
        currentEntity = currentEntity
    )
}
internal data class ResolvedRelationPath(
    val entity: String,
    val field: String,
    val path: List<Triple<String, String, String>>
)

internal fun ExposedCompiler.resolveRelationPathForTest(
    field: FieldRef,
    currentEntity: EntityType
): ResolvedRelationPath {

    val resolved =
        resolveField(field, currentEntity)

    return ResolvedRelationPath(
        entity = resolved.entity,
        field = resolved.field,
        path =
            resolved.relationPath.map {
                Triple(
                    it.sourceEntity.name,
                    it.relation.name,
                    it.targetEntity.name
                )
            }
    )
}
