package org.solyton.solawi.bid.module.banking.data.api

import kotlinx.serialization.Serializable
import org.solyton.solawi.bid.module.banking.data.SepaCollectionId

@Serializable
data class GenerateSepaMessageForCollection(
    val sepaCollectionId: SepaCollectionId
)

@Serializable
data class SepaMessageString(
    val version: SepaMessageVersion,
    val message: String
)

@Serializable
sealed class SepaMessageVersion(open val version: String) {
    data object PAIN008 : SepaMessageVersion("PAIN.008")
}
