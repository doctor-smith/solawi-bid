package org.evoleq.ktorx.result

import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.solyton.solawi.bid.module.bid.data.api.Bid
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs


class ResultSerializationTests {

    // This test arose during tdd -> keep it
    @Test
    fun testSerializationInGeneral() {
        val user = Bid(

            "test-user",
            "", 2.0
        )

        val serialized = Json.encodeToString(user)

        val deserialized = Json.decodeFromString<Bid>(serialized)

        assertEquals(user, deserialized)
    }

    // This is exaggerated, but we keep it as an example
    @Test

    fun serialize() {
       // val (clazz, serializer, data) = input
        val user = Bid(

            "test-user",
            "", 2.0
        )
        serializers[Bid::class] = Bid.serializer()

        val success = Result.Success(user)

        val serialized = Json.encodeToString(success)
        val deserialized = Json.decodeFromString<Result.Success<Bid>>(serialized)

        assertEquals(success, deserialized)
    }

    @Test fun serializeListOfBids() {
        val user = Bid(

            "test-user",
            "", 2.0
        )
        serializers[Bid::class] = Bid.serializer()

        val success = Result.Success(listOf( user ))

        val serialized = Json.encodeToString(success)
        val deserialized = Json.decodeFromString<Result.Success<List<Bid>>>(serialized)

        assertEquals(success, deserialized)
    }

    @Test fun serializeMapOfBids() {
        val user = Bid(

            "test-user",
            "", 2.0
        )
        val key: String = "tu"
        serializers[String::class] = String.serializer()
        serializers[Bid::class] = Bid.serializer()

        val success = Result.Success(mapOf( key to user ))

        val serialized = Json.encodeToString(success)
        val deserialized = Json.decodeFromString<Result.Success<Map<String,Bid>>>(serialized)

        assertEquals(success, deserialized)
    }

    @Test fun serializeArrayOfBids() {
        val user = Bid(

            "test-user",
            "", 2.0
        )
        serializers[Bid::class] = Bid.serializer()

        val success: Result.Success<Array<Bid>> = Result.Success(arrayOf( user ))

        val serialized = Json.encodeToString(success)
        val deserialized = Json.decodeFromString<Result.Success<Array<Bid>>>(serialized)

        assertIs<Result.Success<Array<Bid>>>(deserialized)
        assertEquals(1, success.data.size)
        val deserializedUser = success.data[0]
        assertEquals(user, deserializedUser)
    }
}


