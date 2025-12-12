package org.evoleq.compose.guard.data

import androidx.compose.runtime.Composable
import org.evoleq.math.o // function composition

data class ConditionalActionInput<out T, out A>(
    val testObject: T,
    val actionInput: A
)

typealias ConditionalAction<T, A> = (T) -> @Composable (A) -> Boolean

interface Conditional {
    @Composable
    fun execute(): Boolean
}

data class ExecutableAction<T: Any, A: Any> (
    val inputs: ConditionalActionInput<T, A>,
    val action: ConditionalAction<T, A>
): Conditional {
    @Composable
    override fun execute(): Boolean = action(inputs.testObject)(inputs.actionInput)
}

fun <S, T, A> ConditionalAction<T, A>.contraMap(f: (S) ->T): ConditionalAction<S, A> = this o f

@Composable
fun <T: Any, A: Any> onFulfilled(
    source: () -> ConditionalActionInput<T, A>,
    predicate: (T)-> Boolean,
    action: @Composable (A) -> Unit
): ExecutableAction<T, A> = ExecutableAction(
    source(),
    onFulfilled(
        predicate,
        action
    )
)



@Composable
fun <T: Any, A: Any> onFulfilled(
    predicate: (T)-> Boolean,
    action: @Composable (A) -> Unit
): ConditionalAction<T, A> = {t -> {a ->  when{
        predicate(t) -> {
            action(a)
            true
        }
        else -> false
    }
} }

@Composable
fun <T, A> ConditionalActionInput<T, A>.action(
    run: ConditionalAction<T, A>
): @Composable (A) -> Boolean = run(testObject)

/**
 * Pulsing execution of actions.
 *
 * Usage in [Composable] function
 * ```
 *  component@{
 *      ...
 *      if(sequentiallyExecuted(
 *          onFulfilled(
 *              source = ...,
 *              predicate = ...
 *              action = ...
 *          )
 *      )) return@component
 *  }
 * ```
 */
@Composable
fun sequentiallyExecuted(vararg conditionals: ExecutableAction<*, *>): Boolean = when{
    conditionals.isEmpty() -> false
    else -> {
        val first = conditionals.first()
        val executed = first.execute()
        if(executed) true
        else sequentiallyExecuted(*conditionals.drop(1).toTypedArray())
    }
}
