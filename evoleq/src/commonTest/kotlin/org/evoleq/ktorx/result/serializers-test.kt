package org.evoleq.ktorx.result

import kotlinx.serialization.builtins.serializer
import kotlin.test.Test
import kotlin.test.assertTrue

class SerializersTest {

    @Test
    fun serializers() {
        serializers {
            // Standard
            add<Int>(Int.serializer())
            add<Long>(Long.serializer())
            add<String>(String.serializer())
            add<Boolean>(Boolean.serializer())
            add<Double>(Double.serializer())
        }
        assertTrue (serializers.isNotEmpty())
    }
}
