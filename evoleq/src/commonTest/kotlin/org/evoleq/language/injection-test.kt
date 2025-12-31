package org.evoleq.language

import org.evoleq.math.Reader
import org.evoleq.math.Source
import kotlin.test.Test
import kotlin.test.assertEquals

class InjectionTest {

    /**
     * Tests for the `inject` function in the InjectKt class.
     *
     * The inject function is an extension on the Reader class that replaces placeholders (keys
     * surrounded by `${}`) in the source string with corresponding values from the provided
     * `Injection` objects.
     */

    @Test
    fun `inject should replace placeholder with corresponding value`() {
        // Arrange
        val reader: Reader<Unit, String> = Reader { "Hello, \${name}!" }
        val injections = arrayOf(Injection("name", "World"))

        // Act
        val result = reader.inject(*injections)(Unit)

        // Assert
        assertEquals("Hello, World!", result)
    }

    @Test
    fun `inject should replace multiple placeholders with their corresponding values`() {
        // Arrange
        val reader: Reader<Unit, String> = Reader { "Welcome, \${firstName} \${lastName}!" }
        val injections = arrayOf(
            Injection("firstName", "John"),
            Injection("lastName", "Doe")
        )

        // Act
        val result = reader.inject(*injections)(Unit)

        // Assert
        assertEquals("Welcome, John Doe!", result)
    }

    @Test
    fun `inject should not replace placeholders without corresponding injections`() {
        // Arrange
        val reader: Reader<Unit, String> = Reader { "Hello, \${name}!" }
        val injections = emptyArray<Injection>()

        // Act
        val result = reader.inject(*injections)(Unit)

        // Assert
        assertEquals("Hello, \${name}!", result)
    }

    @Test
    fun `inject should handle empty input and no injections`() {
        // Arrange
        val reader: Reader<Unit, String> = Reader { "" }
        val injections = emptyArray<Injection>()

        // Act
        val result = reader.inject(*injections)(Unit)

        // Assert
        assertEquals("", result)
    }

    @Test
    fun `inject should replace placeholders in complex strings`() {
        // Arrange
        val reader: Reader<Unit, String> = Reader { "Good \${timeOfDay}, \${name}! See you in \${location}." }
        val injections = arrayOf(
            Injection("timeOfDay", "evening"),
            Injection("name", "Alice"),
            Injection("location", "Paris")
        )

        // Act
        val result = reader.inject(*injections)(Unit)

        // Assert
        assertEquals("Good evening, Alice! See you in Paris.", result)
    }

    @Test
    fun `inject should prioritize later injections when keys overlap`() {
        // Arrange
        val reader: Reader<Unit, String> = Reader { "Hello, \${name}!" }
        val injections = arrayOf(
            Injection("name", "World"),
            Injection("name", "Universe")
        )

        // Act
        val result = reader.inject(*injections)(Unit)

        // Assert
        assertEquals("Hello, World!", result)
    }

    /**
     * This class tests the `inject` function from InjectKt.
     * The `inject` function replaces placeholders in a string with corresponding values provided by Injection objects.
     * Each test case focuses on a specific scenario for clarity and comprehensiveness.
     */

    @Test
    fun `should inject single key-value pair into a string`() {
        // Arrange
        val key = "name"
        val value = "Alice"
        val input = "Hello \${name}!"
        val expected = "Hello Alice!"

        // Act
        val result = inject(key, value)(input)

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun `should inject multiple key-value pairs into a string`() {
        // Arrange
        val injection1 = Injection("name", "Alice")
        val injection2 = Injection("greeting", "Hello")
        val input = "\${greeting}, \${name}!"
        val expected = "Hello, Alice!"

        // Act
        val result = inject(injection1, injection2)(input)

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun `should inject values using shorthand infix function`() {
        // Arrange
        val input = "Goodbye \${name}!"
        val expected = "Goodbye Bob!"

        // Act
        val result = inject("name" by "Bob")(input)

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun `should perform no changes on string with no matchable placeholders`() {
        // Arrange
        val injection = Injection("key", "value")
        val input = "This string has no placeholders."
        val expected = input

        // Act
        val result = inject(injection)(input)

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun `should handle empty string as input`() {
        // Arrange
        val injection = Injection("key", "value")
        val input = ""
        val expected = ""

        // Act
        val result = inject(injection)(input)

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun `should replace multiple occurrences of the same placeholder`() {
        // Arrange
        val key = "fruit"
        val value = "apple"
        val input = "An \${fruit} a day keeps the \${fruit} doctor away."
        val expected = "An apple a day keeps the apple doctor away."

        // Act
        val result = inject(key, value)(input)

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun `should inject values from a Reader with placeholders`() {
        // Arrange
        val reader = Reader { _: Unit -> "I am a \${attribute}." }
        val injection = Injection("attribute", "developer")
        val expected = "I am a developer."

        // Act
        val injectedReader = reader.inject(injection)
        val result = injectedReader(Unit)

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun `should handle empty injections`() {
        // Arrange
        val input = "This is a test string."
        val expected = input

        // Act
        val result = inject()(input)

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun `should handle a Source-based injection`() {
        // Arrange
        val source = Source { "Dynamic Value" }
        val input = "Here is a \${dynamicKey}."
        val expected = "Here is a Dynamic Value."

        // Act
        val result = inject("dynamicKey" by source)(input)

        // Assert
        assertEquals(expected, result)
    }
}
