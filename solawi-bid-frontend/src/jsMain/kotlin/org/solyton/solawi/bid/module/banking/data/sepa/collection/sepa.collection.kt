package org.solyton.solawi.bid.module.banking.data.sepa.collection

import org.evoleq.axioms.definition.Lensify
import org.evoleq.axioms.definition.ReadOnly
import org.evoleq.axioms.definition.ReadWrite
import org.solyton.solawi.bid.module.banking.data.*
import org.solyton.solawi.bid.module.banking.data.sepa.SepaSequenceType
import org.solyton.solawi.bid.module.banking.data.sepa.mandate.SepaMandate
import org.solyton.solawi.bid.module.banking.data.sepa.payment.SepaPayment

@Lensify
data class SepaCollection(
    @ReadOnly val sepaCollectionId: SepaCollectionId,
    @ReadWrite val creditorIdentifierId: CreditorIdentifierId,
    @ReadWrite val creditorBankAccountId: BankAccountId,
    @ReadWrite val mandateReferencePrefix: MandateReferencePrefix,
    @ReadWrite val remittanceInformation: RemittanceInformation,
    @ReadWrite val sepaSequenceType: SepaSequenceType,
    @ReadWrite val localInstrument: LocalInstrument?,
    @ReadWrite val chargeBearer: ChargeBearer = ChargeBearer("SLEV"),
    @ReadWrite val requestedCollectionDay: Int? = null,
    @ReadWrite val leadTimesDays: Int = 2,
    @ReadWrite val purposeCode: PurposeCode? = null,
    @ReadWrite val isActive: Boolean = false,
    @ReadWrite val sepaMandates: List<SepaMandate> = emptyList(),
    @ReadWrite val sepaPayments: List<SepaPayment> = emptyList(),
    @ReadWrite val referenceIds: List<SepaCollectionReferenceId> = emptyList()
)
