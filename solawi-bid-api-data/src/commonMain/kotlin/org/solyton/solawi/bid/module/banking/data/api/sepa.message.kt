package org.solyton.solawi.bid.module.banking.data.api

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import org.solyton.solawi.bid.module.banking.data.SepaCollectionId

typealias ApiSepaMessageString = SepaMessageString
typealias ApiSepaMessageVersion = SepaMessageVersion

@Serializable
data class GenerateSepaMessageForCollection(
    val sepaCollectionId: SepaCollectionId,
    val executionDate: LocalDate
)

@Serializable
data class SepaMessageString(
    val version: SepaMessageVersion,
    val message: String
)

@Serializable
sealed class SepaMessageVersion(open val version: String) {
    @Serializable
    data object PAIN008 : SepaMessageVersion("PAIN.008")
}
