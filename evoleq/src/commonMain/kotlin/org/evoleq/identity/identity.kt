package org.evoleq.identity

/**
 * Represents a generic identity wrapper for a specific type.
 *
 * This interface provides a mechanism for obtaining an identity value
 * of a given type through a lambda function.
 *
 * @param T The type of the identity value, which must be non-nullable.
 */
interface Identity<out T : Any> {
    val getIdentity: () -> T
}
