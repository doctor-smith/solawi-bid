package org.evoleq.exposedx

const val NO_MESSAGE_PROVIDED = "No message provided"

sealed class DatabaseException(override val message: String?) : Exception(message) {
    // data object NoMessageProvided : DatabaseException("No message provided")
}
