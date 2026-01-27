package org.evoleq.ktorx.client

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object QueryParamsSerializer : KSerializer<Map<String, String>> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("QueryParams", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Map<String, String>) {
        val query = value.entries.joinToString("&") { (k, v) ->
            "${encode(k)}=${encode(v)}"
        }
        encoder.encodeString(query)
    }

    override fun deserialize(decoder: Decoder): Map<String, String> {
        val query = decoder.decodeString()
        if (query.isBlank()) return emptyMap()

        return query.split("&")
            .mapNotNull {
                val parts = it.split("=", limit = 2)
                if (parts.size == 2) parts[0] to parts[1] else null
            }
            .toMap()
    }
}
