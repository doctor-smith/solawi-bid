package org.evoleq.exposedx.transaction

import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction

fun <T> ensureTransaction(statement: Transaction.() -> T): T {
    val tx = TransactionManager.currentOrNull()
    return if (tx != null) {
        println(">>>>>>>>>>>>>>>>>>>>>>>> Transaction already exists, execute within it")
        tx.statement()
    } else {
        println(">>>>>>>>>>>>>>>>>>>>>>>> No transaction in context, start a new one")
        // No transaction in context, start a new one
        transaction {
            statement()
        }
    }
}