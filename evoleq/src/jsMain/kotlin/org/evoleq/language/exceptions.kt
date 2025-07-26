package org.evoleq.language

sealed class LanguageException(override val  message: String) : Exception(message) {
    data object CannotMergeBlocks : LanguageException("Cannot merge blocks")
    data object CannotMergeLangs : LanguageException("Cannot merge langs")
    data class ElementNotFoundInBlock(val key: String, val block: String): LanguageException("There is no Element in block '$block' with key = '$key'")
    data class BlockNotFoundInBlock(val key: String, val block: String): LanguageException("There is no block in block '$block' with key = '$key'")
}
