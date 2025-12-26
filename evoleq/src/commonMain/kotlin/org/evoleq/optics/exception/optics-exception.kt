package org.evoleq.optics.exception

sealed class OpticsException(override val message: String): Exception(message) {
    sealed class Lens(override val message: String) : OpticsException(message) {
        data object Empty : Lens("Empty lens")
        data object NotSet : Lens("Lens is not set")
        sealed class DeepSearch(override val message: String) : Lens("Lens was de") {
            data object Empty : DeepSearch("Deep search was empty")
        }
    }
}
