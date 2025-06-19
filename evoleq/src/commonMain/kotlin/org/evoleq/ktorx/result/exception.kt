package org.evoleq.ktorx.result

sealed class SerializationException(override val message: String) : Exception(message) {
    data object Default : SerializationException("Serializ")
}
