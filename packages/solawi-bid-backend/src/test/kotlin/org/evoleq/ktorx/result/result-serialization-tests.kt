package org.evoleq.ktorx.result

import kotlinx.serialization.KSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.solyton.solawi.bid.Unit
import org.solyton.solawi.bid.module.user.data.api.UserD
import java.util.*
import kotlin.reflect.KClass
import kotlin.test.assertEquals
class ResultSerializationTests {

    // This test arose during tdd -> keep it
    @Unit@Test
    fun testSerializationInGeneral() {
        val user = UserD(
            UUID.randomUUID(),
            "test-user",
            "test-password"
        )

        val serialized = Json.encodeToString(user)

        val deserialized = Json.decodeFromString<UserD>(serialized)

        assertEquals(user, deserialized)
    }

    // This is exaggerated, but we keep it as an example
    @Unit@ParameterizedTest
    @MethodSource("userInputProvider")
    fun serialize(input: TestData<UserD>) {
        val (clazz, serializer, data) = input

        serializers[clazz] = serializer
        serializers[Any::class] = serializer
        val success = Result.Success(data)

        val serialized = Json.encodeToString(success)
        val deserialized = Json.decodeFromString<Result.Success<UserD>>(serialized)

        assertEquals(success, deserialized)
    }

    companion object {
        @JvmStatic
        fun userInputProvider() = listOf(
            TestData(
                UserD::class,
                UserD.serializer(),
                UserD(
                    UUID.randomUUID(),
                    "test-user",
                    "test-password"
                )
            ),
            TestData(
                UserD::class,
                UserD.serializer(),
                UserD(
                    UUID.randomUUID(),
                    "test-user-2",
                    "test-password"
                )
            ),
            TestData(
                UserD::class,
                UserD.serializer(),
                UserD(
                    UUID.randomUUID(),
                    "test-user-3",
                    "test-password"
                )
            )

        )
    }



    data class TestData<out T: Any>(
        val clazz: KClass<out T>,
        val serializer: KSerializer<out T>,
        val data: T,
    )
}


