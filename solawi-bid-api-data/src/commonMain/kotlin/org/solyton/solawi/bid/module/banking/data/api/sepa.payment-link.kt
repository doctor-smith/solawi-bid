package org.solyton.solawi.bid.module.banking.data.api

import kotlinx.serialization.Serializable
import org.evoleq.ktorx.client.EmptyParams
import org.evoleq.ktorx.client.Parameters
import org.evoleq.ktorx.client.QueryParams
import org.solyton.solawi.bid.module.banking.data.SepaPaymentId

typealias ApiSepaPaymentLinks = SepaPaymentLinks
typealias ApiSepaPaymentLink = SepaPaymentLink
typealias ApiSuccessorKind = SuccessorKind

@Serializable
data class SepaPaymentLinks(
    val all: List<ApiSepaPaymentLink>
)

@Serializable
data class SepaPaymentLink(
    val successorId: SepaPaymentId,
    val predecessorId: SepaPaymentId,
    val kind: SuccessorKind
)
@Serializable
enum class SuccessorKind {
    NEXT_PERIOD,    // the regularly scheduled follow-up
    RETRY,          // re-execution after FAILED,
    MERGE,          // merge of two or more payments
    AD_HOC,         // newly introduced payment outside the scheduled sequence
    // CORRECTION,     // manual correction / adjustment
    // REPLACEMENT,    // replaces a cancelled message
}

@Serializable
data class ReadSepaPaymentLinksByLegalEntity(
    /**
     * takes param
     * "legal_entity"
     */
    override val queryParams: QueryParams
) : Parameters()

@Serializable
data object ReadPersonalSepaPaymentLinks: EmptyParams()
