package org.solyton.solawi.bid.module.db

import java.util.*


sealed class ContextException (override val message: String) : Exception(message) {
    data class NoSuchContext(val id: String) : ContextException("No such context; id = $id")
    data class NoSuchRootContext(val id: String) : ContextException("No such root context; id = $id")

    data class NoSuchChildContext(val id: UUID) : ContextException("No such child context; id = $id")
    data class NoSuchContextPath(val path: String, val segment: String, val level: Int) : ContextException("No such context; path = $path, segment = $segment, level: $level")
    data class PathAlreadyExists(val path: String): ContextException("Path '$path' already exists" )
}
