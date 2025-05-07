package org.evoleq.ktorx.result

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder


object ResultSerializer : KSerializer<Result<*>> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Result") {
        element<String>("type")
        element<String>("classType", isOptional = true)
        element<String>("data", isOptional = true)
        element<String>("message", isOptional = true)
    }

    override fun serialize(encoder: Encoder, value: Result<*>) {
        val compositeEncoder = encoder.beginStructure(descriptor)
        when (value) {
            is Result.Success -> {
                compositeEncoder.encodeStringElement(descriptor, 0, "Success")

                val typeName = value.data!!::class.simpleName ?: "UnknownType"
                compositeEncoder.encodeStringElement(descriptor, 1, typeName)
                compositeEncoder.encodeSerializableElement(descriptor, 2, serializers[value.data::class]!! as KSerializer<Any>,value.data)
            }
            is Result.Failure.Message -> {

                compositeEncoder.encodeStringElement(descriptor, 0, "Failure")
                compositeEncoder.encodeStringElement(descriptor, 3, value.value)
            }
            is Result.Failure.Exception -> {

                compositeEncoder.encodeStringElement(descriptor, 0, "Failure")
                compositeEncoder.encodeStringElement(descriptor, 3, value.value.message?: "No message provided")
            }
        }
        compositeEncoder.endStructure(descriptor)
    }

    override fun deserialize(decoder: Decoder): Result<*> {
        val dec = decoder.beginStructure(descriptor)
        var type: String? = null
        var classType: String? = null
        var data: Any? = null
        var message: String? = null

        loop@ while (true) {
            when (val index = dec.decodeElementIndex(descriptor)) {
                0 -> type = dec.decodeStringElement(descriptor, 0)
                1 -> classType = dec.decodeStringElement(descriptor, 1)
                2 -> data = dec.decodeSerializableElement(descriptor, 2, serializers[classType!!] as KSerializer<Any>)
                3 -> message = dec.decodeStringElement(descriptor, 3)
                CompositeDecoder.DECODE_DONE -> break@loop
                else -> throw SerializationException("Unknown index $index")
            }
        }
        dec.endStructure(descriptor)

        return when (type) {
            "Success" -> Result.Success(data ?: "")
            "Failure" -> Result.Failure.Message(message ?: "Unknown message")
            else -> throw SerializationException("Unknown type $type")
        }
    }
}
