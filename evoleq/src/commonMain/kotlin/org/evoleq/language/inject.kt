package org.evoleq.language

import org.evoleq.math.Reader
import org.evoleq.math.Source
import org.evoleq.math.emit
import org.evoleq.math.map

/**
 * Represents a key-value pair injection for use within a language processing context.
 *
 * This data class encapsulates two string properties:
 * - `key`: The identifier or name associated with the injection.
 * - `value`: The corresponding value associated with the key.
 *
 * Used primarily in the configuration or parsing of language models, the `Injection` class
 * provides a way to store associations in a clear and type-safe manner.
 */
@I18N
data class Injection(
    val key: String,
    val value: String
)

/**
 * Constructs an `Injection` object by resolving the string value from the provided `Source`.
 *
 * @param key The identifier associated with the injection.
 * @param value A `Source` that emits the value associated with the key.
 * @return The constructed `Injection` object containing the resolved key-value pair.
 */
@I18N
@Suppress("FunctionName")
fun Injection(key: String, value: Source<String>): Injection = Injection(key, value.emit())

/**
 * Applies a series of key-value pair injections to the string content processed by the Reader.
 * Each injection replaces occurrences of a placeholder (formatted as `$key`) in the string with its associated value.
 *
 * @param injections A variable number of key-value injections where each injection specifies a key placeholder and its corresponding replacement value.
 * @return A new Reader where all specified injections have been applied to the string content.
 */
@I18N
fun <T> Reader<T, String>.inject(vararg injections: Injection): Reader<T, String> = map {
    value -> value.inject(*injections)
}

/**
 * Replaces placeholders in the string, defined in the format `${key}`, with their associated values
 * provided in the `injections`.
 *
 * @param injections A variable number of `Injection` objects containing `key` and `value` pairs.
 *                   Each placeholder in the string matching `${key}` is replaced with the corresponding `value`.
 */
@I18N
fun String.inject(vararg injections: Injection) = injections.fold(this){acc, injection -> acc.replace("\${${injection.key}}", injection.value)}

/**
 * Injects a series of key-value pairs into a string processing context using the provided injections.
 *
 * @param injections A variable number of `Injection` objects containing `key` and `value` pairs.
 * Each placeholder in the string, matching the format `${key}`, is replaced with the corresponding `value`.
 * @return A `Reader` that, when invoked with a string, applies the `inject` method to replace placeholders
 * in the string with the values from the specified injections.
 */
@I18N
fun  inject(vararg injections: Injection): Reader<String, String> = Reader { t -> t.inject(*injections) }

/**
 * Injects a single key-value pair into a string processing context.
 *
 * @param key The identifier or placeholder to be replaced in the string. It matches placeholders in the format `${key}`.
 * @param value The corresponding value that replaces the key in the string.
 */
@I18N
fun inject(key: String, value: String) = inject(Injection(key, value))

/**
 * Creates an `Injection` instance by associating the current string as a key
 * with the provided replacement string as its value.
 *
 * @param replacement The value to be associated with this string as a key.
 * @return An `Injection` instance representing the key-value pair.
 */
@I18N
infix fun String.by(replacement: String): Injection = Injection(this, replacement)

/**
 * Creates an `Injection` object that associates the calling `String` with a resolved value from the given `Source`.
 *
 * @param replacement The `Source` that emits the value to be associated with the calling string.
 * @return An `Injection` object encapsulating the key-value pair.
 */
@I18N
infix fun String.by(replacement: Source<String>): Injection = Injection(this, replacement)
