package org.solyton.solawi.bid.module.banking.action

import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.Contextual
import org.evoleq.ktorx.DbAction
import org.evoleq.ktorx.KlAction
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.math.MathDsl
import org.evoleq.math.x
import org.solyton.solawi.bid.module.banking.data.api.BankAccount
import org.solyton.solawi.bid.module.banking.data.api.UpdateBankAccount
import org.solyton.solawi.bid.module.banking.data.toApiType
import org.solyton.solawi.bid.module.banking.data.toDomainType
import org.solyton.solawi.bid.module.banking.data.toUUID
import org.solyton.solawi.bid.module.banking.repository.updateBankAccount
import org.solyton.solawil.bid.module.bid.data.api.toUUID


@MathDsl
@Suppress("FunctionName")
fun UpdateBankAccount(): KlAction<Result<Contextual<UpdateBankAccount>>, Result<BankAccount>> = KlAction { result ->
    DbAction { database ->
        result bindSuspend { (creator, _ , data) ->
            resultTransaction(database) {
                val (bankAccountId, userId, bic, iban, accountHolder, isActive, accountType) = data

                updateBankAccount(
                    bankAccountId = bankAccountId.toUUID(),
                    userId = userId.toUUID(),
                    iban = iban,
                    bic = bic,
                    accountHolder = accountHolder.orEmpty(),
                    isActive = isActive,
                    accountType = accountType.toDomainType(),
                    modifierId = creator
                ).toApiType()
            }
        } x database
    }
}
