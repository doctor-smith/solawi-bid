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
import org.solyton.solawi.bid.module.banking.data.toApiType
import org.solyton.solawi.bid.module.banking.exception.LegalEntityException
import org.solyton.solawi.bid.module.banking.schema.LegalEntitiesTable
import org.solyton.solawi.bid.module.banking.schema.LegalEntityEntity
import java.util.*


@MathDsl
@Suppress("FunctionName")
fun ReadLegalEntity(): KlAction<Result<Contextual<String>>, Result<LegalEntity>> = KlAction { result ->
    DbAction { database ->
        result bindSuspend { contextual ->
            resultTransaction(database) {
                val partyId = UUID.fromString(contextual.data)
                LegalEntityEntity.find {
                    LegalEntitiesTable.partyId eq partyId
                }.firstOrNull()?.toApiType()?: throw LegalEntityException.NoSuchLegalEntityParty(contextual.data)
            }
        } x database
    }
}
