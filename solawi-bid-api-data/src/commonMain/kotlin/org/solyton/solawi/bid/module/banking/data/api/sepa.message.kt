package org.solyton.solawi.bid.module.banking.data.api

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import org.evoleq.ktorx.client.Parameters
import org.evoleq.ktorx.client.QueryParams
import org.solyton.solawi.bid.module.banking.data.*

typealias ApiSepaMessageString = SepaMessageString
typealias ApiSepaMessageVersion = SepaMessageVersion
typealias ApiSepaMessages = SepaMessages
typealias ApiSepaMessage = SepaMessage
typealias ApiSepaMessageStatus = SepaMessageStatus

@Serializable
data class SepaMessages(
    val all: List<SepaMessage>
)

@Serializable
data class SepaMessage(
    val sepaMessageId: SepaMessageId,
    val messageIdentifier: SepaMessageIdentifier, // messageId in the message, e.g. MSG-20230101-123456789
    val executionDate: LocalDate,
    val status: SepaMessageStatus,
    val totalAmount: Double?,
    val numberOfPayments: Int,
    val remittanceInformation: RemittanceInformation,
    val paymentIds: List<SepaPaymentId>,
)

@Serializable
enum class SepaMessageStatus {
    CREATED,   // Message prepared but not yet sent
    SENT,      // Message sent to bank
    PENDING, // Bank is processing the message
    CONFIRMED, // Bank confirmed execution
    FAILED     // Message rejected or failed
}

@Serializable
data class GenerateSepaMessageForCollection(
    val sepaCollectionId: SepaCollectionId,
    val executionDate: LocalDate,
    val sepaPaymentIds: List<SepaPaymentId>? = null,
    val remittanceInformation: RemittanceInformation? = null,
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

@Serializable
data class ReadSepaMessagesByLegalEntityId(
    /**
     * takes param
     * "legal_entity"
     */
    override val queryParams: QueryParams
) : Parameters()

@Serializable
data class UpdateSepaMessageStatus(
    val sepaMessageId: SepaMessageId,
    val newStatus: SepaMessageStatus
)
