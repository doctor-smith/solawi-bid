package org.evoleq.ktorx.client

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object QueryParamsSerializer : KSerializer<QueryParams> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("QueryParams", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: QueryParams) {
        val query = value.joinToString("&") { (k, v) ->
            "${encode(k)}=${encode(v)}"
        }
        encoder.encodeString(query)
    }

    override fun deserialize(decoder: Decoder): QueryParams {
        val query = decoder.decodeString()
        if (query.isBlank()) return emptyList()

        return query.split("&")
            .mapNotNull {
                val parts = it.split("=", limit = 2)
                if (parts.size == 2) parts[0] to parts[1] else null
            }
    }
}
