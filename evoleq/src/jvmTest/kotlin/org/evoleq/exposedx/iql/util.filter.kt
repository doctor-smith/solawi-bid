package org.evoleq.exposedx.iql

import kotlinx.serialization.json.JsonPrimitive
import org.evoleq.iql.data.ComparisonFilter
import org.evoleq.iql.data.Operator
import org.evoleq.iql.data.SimpleFieldRef

internal fun eq(
    path: String,
    value: String
): ComparisonFilter =
    ComparisonFilter(
        field = SimpleFieldRef(path),
        operator = Operator.EQ,
        value = JsonPrimitive(value)
    )
