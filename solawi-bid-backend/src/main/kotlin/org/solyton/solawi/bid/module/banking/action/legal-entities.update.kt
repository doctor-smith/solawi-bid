package org.solyton.solawi.bid.module.banking.action

import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.Contextual
import org.evoleq.ktorx.DbAction
import org.evoleq.ktorx.KlAction
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.math.MathDsl
import org.evoleq.math.x
import org.solyton.solawi.bid.module.banking.data.api.LegalEntity
import org.solyton.solawi.bid.module.banking.data.api.UpdateLegalEntity
import org.solyton.solawi.bid.module.banking.data.toApiType
import org.solyton.solawi.bid.module.banking.data.toDomainType
import org.solyton.solawi.bid.module.banking.repository.updateLegalEntity
import java.util.*


@MathDsl
@Suppress("FunctionName")
fun UpdateLegalEntity(): KlAction<Result<Contextual<UpdateLegalEntity>>, Result<LegalEntity>> = KlAction { result ->
    DbAction { database ->
        result bindSuspend { contextual ->
            resultTransaction(database) {
                val data = contextual.data
                val userId = contextual.userId
                updateLegalEntity(
                    UUID.fromString(data.legalEntityId.value),
                    UUID.fromString(data.partyId.value),
                    data.name,
                    data.legalForm,
                    data.legalEntityType.toDomainType(),
                    UUID.fromString(data.address.id),
                    userId
                ).toApiType()
            }
        } x database
    }
}
