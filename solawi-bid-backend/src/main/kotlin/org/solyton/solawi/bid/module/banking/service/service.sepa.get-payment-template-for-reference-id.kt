package org.solyton.solawi.bid.module.banking.service

import org.evoleq.exposedx.migrations.isNotNull
import org.jetbrains.exposed.sql.Transaction
import org.solyton.solawi.bid.module.banking.repository.readSepaPaymentTemplateByMandateAndCollection
import org.solyton.solawi.bid.module.banking.schema.*
import java.util.*

/**
 * Get the payment template for a given reference id
 */
fun Transaction.getSepaPaymentTemplateByReferenceId(referenceId: UUID): SepaPaymentTemplateEntity? {
    // Assumption:
    // the reference is in 1-1* relation with payment templates.
    // If it exists, it is uniquely identified by the reference
    // Fact:
    // By construction, the template is uniquely determined by the collection and the mandate
    //
    // Conclusion:
    // If we find a collection mappings for the reference, and mandate mappings at the same time,
    // all key-combinations of (collection.id, mandate.id) lead to the same template.
    //
    // => We can take the first pair (collection.id, mandate.id) to compute the template

    val collectionMapping = SepaCollectionMapping.find{
        SepaCollectionMappings.referenceId eq referenceId
    }.firstOrNull()
    val mandateMapping = SepaMandateDataMappingEntity.find{
        SepaMandateDataMappingsTable.referenceId eq referenceId
    }.firstOrNull()

    if(collectionMapping.isNotNull() || mandateMapping.isNotNull()) return null

    requireNotNull(collectionMapping)
    requireNotNull(mandateMapping)
    return readSepaPaymentTemplateByMandateAndCollection(
        collectionMapping.id.value,
        mandateMapping.id.value
    )
}
