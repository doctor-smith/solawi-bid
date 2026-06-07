package org.solyton.solawi.bid.module.banking.repository

import org.jetbrains.exposed.sql.Transaction
import org.solyton.solawi.bid.module.banking.schema.CreditorIdentifierEntity
import org.solyton.solawi.bid.module.banking.schema.CreditorIdentifiersTable
import org.solyton.solawi.bid.module.banking.schema.SepaMessageEntity
import java.util.*

fun Transaction.readSepaMessagesByLegalEntity(
    legalEntityId: UUID
): List<SepaMessageEntity> {
    // Each sepa message is associated with a creditor identifier
    // We can use this fact to find the creditor identifier for the given legal entity
    // and then retrieve the sepa messages associated with that creditor identifier
    val creditorIdentifier = CreditorIdentifierEntity.find{
        CreditorIdentifiersTable.legalEntityId eq legalEntityId
    }.firstOrNull()

    if(creditorIdentifier == null) return emptyList()

    return creditorIdentifier.sepaMessages.toList()
}
