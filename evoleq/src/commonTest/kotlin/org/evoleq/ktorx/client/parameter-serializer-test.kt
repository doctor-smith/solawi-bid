package org.evoleq.ktorx.client

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class ParameterSerializerTest {

    @Serializable
    data class Data(val name: String, override val all: Map<String, String>): Parameters()

    @Serializable
    data class Empty(val x: Int) : EmptyParams()

    @Test fun testIt() {
        val data = Data("child", mapOf("age" to "10"))
        val serialized = Json.encodeToString(data )

        val deserialized = Json.decodeFromString<Data>(serialized)

        assertEquals(data, deserialized)
    }

    @Test fun serializeWithEmptyParams() {
        val e = Empty(1)
        val serialized = Json.encodeToString(e)
        val deserialized = Json.decodeFromString<ParameterSerializerTest.Empty>(serialized)

        assertEquals(e, deserialized)
    }
}

