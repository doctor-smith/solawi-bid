package org.evoleq.exposedx.transaction

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withContext
import org.evoleq.ktorx.result.Result
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

@OptIn(ExperimentalCoroutinesApi::class)
suspend fun <T : Any> resultTransaction(database: Database, statement: Transaction.() -> T): Result<T> = withContext(Dispatchers.Default.limitedParallelism(1)) {
    try {
        newSuspendedTransaction(
            Dispatchers.IO,
            database
        ) {
            ensureTransaction(statement).let{Result.Success(it) }
        }
    } catch (e: Throwable) {
        Result.Failure.Exception(e)
    }
}
