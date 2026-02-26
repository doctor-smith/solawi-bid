package org.solyton.solawi.bid.module.banking.action

import org.evoleq.optics.storage.ActionDispatcher
import org.solyton.solawi.bid.module.banking.data.application.BankingApplication
import org.solyton.solawi.bid.module.banking.data.environment.Environment

val testBankingApplication by lazy {  BankingApplication(
    environment = Environment(),
    actions = ActionDispatcher{}
) }
