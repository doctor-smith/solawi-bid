package org.solyton.solawi.bid.module.banking.action

import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.Contextual
import org.evoleq.ktorx.DbAction
import org.evoleq.ktorx.KlAction
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.math.MathDsl
import org.evoleq.math.x
import org.solyton.solawi.bid.module.banking.data.api.CreateLegalEntity
import org.solyton.solawi.bid.module.banking.data.api.LegalEntity
import org.solyton.solawi.bid.module.banking.data.toApiType
import org.solyton.solawi.bid.module.banking.data.toDomainType
import org.solyton.solawi.bid.module.banking.repository.createLegalEntity
import java.util.*


@MathDsl
@Suppress("FunctionName")
fun CreateLegalEntity(): KlAction<Result<Contextual<CreateLegalEntity>>, Result<LegalEntity>> = KlAction { result ->
    DbAction { database ->
        result bindSuspend { contextual ->
            resultTransaction(database) {
                val data = contextual.data
                val userId = contextual.userId
                createLegalEntity(
                    UUID.fromString(data.partyId.value),
                    data.name,
                    data.legalForm.orEmpty(),
                    data.legalEntityType.toDomainType(),
                    UUID.fromString(data.address.id),
                    userId
                ).toApiType()
            }
        } x database
    }
}
