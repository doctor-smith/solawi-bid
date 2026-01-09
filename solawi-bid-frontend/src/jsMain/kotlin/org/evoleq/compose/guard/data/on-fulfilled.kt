package org.evoleq.compose.guard.data

import androidx.compose.runtime.Composable
import org.evoleq.math.o // function composition

/**
 * Represents a data structure that holds a condition and an associated action input.
 *
 * This class is primarily used for conditional actions where a `testObject` is evaluated,
 * and if the condition is met, an `actionInput` is provided to execute a specific action.
 *
 * @param T The type of the test object used to evaluate conditions.
 * @param A The type of the input passed to the action when the condition is satisfied.
 *
 * Instances of this class are commonly utilized in composable functions or workflows
 * where conditional logic determines the execution of certain actions.
 */
data class ConditionalActionInput<out T, out A>(
    val testObject: T,
    val actionInput: A
)

/**
 * A type alias that represents a conditional action function.
 *
 * This function type takes an input of type T and returns a composable function.
 * The returned composable function takes an argument of type A and evaluates
 * if a certain condition is fulfilled, returning a Boolean result.
 *
 * @param T The type of the input to the conditional action function, test object.
 * @param A The type of the argument provided to the returned composable function, the action.
 */
typealias ConditionalAction<T, A> = (T) -> @Composable (A) -> Boolean

/**
 * Interface representing a composable conditional operation.
 * Implementing classes define a composable `execute` function
 * that evaluates a conditional logic and returns a boolean result.
 */
interface Conditional {
    @Composable
    fun execute(): Boolean
}

/**
 * Represents an executable conditional action consisting of its inputs and the associated action logic.
 *
 * This class is utilized to encapsulate the inputs necessary for evaluating a condition and performing
 * an associated action upon fulfillment of the condition. It implements the `Conditional` interface,
 * allowing composable execution of the action.
 *
 * @param T The type of the object used in the condition evaluation.
 * @param A The type of the input passed to the action when the condition is fulfilled.
 * @property inputs An instance of [ConditionalActionInput] that provides the test object and action input.
 * @property action A composable function that represents the conditional action logic, executed when conditions are met.
 */
data class ExecutableAction<T: Any, A: Any> (
    val inputs: ConditionalActionInput<T, A>,
    val action: ConditionalAction<T, A>
): Conditional {
    @Composable
    override fun execute(): Boolean = action(inputs.testObject)(inputs.actionInput)
}

/**
 * Contravariant functoriality of ConditionalActions
 * Transforms the input type of a `ConditionalAction` using the provided mapping function.
 *
 * @param f A function to map from type `S` to type `T`.
 * @return A new `ConditionalAction` where the input type has been transformed from `T` to `S` using the provided mapping function.
 */
fun <S, T, A> ConditionalAction<T, A>.contraMap(f: (S) ->T): ConditionalAction<S, A> = this o f

/**
 * Creates an `ExecutableAction` that evaluates a condition and executes a specific composable action
 * if the condition is satisfied.
 *
 * @param source A function providing a `ConditionalActionInput` containing the test object and action input.
 * @param predicate A lambda function representing the condition to evaluate on the test object.
 * @param action A composable lambda function to execute when the predicate evaluates to `true`.
 * @return An instance of `[ExecutableAction]` that encapsulates the condition and the associated action logic.
 */
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

/**
 * Evaluates a condition using a provided predicate and executes a specific composable action
 * when the condition is satisfied.
 *
 * The evaluation process:
 * 1. The predicate is evaluated on a provided test object
 * 2. If the condition is met, the action will be executed, and we return 'true'.
 * 3. Otherwise, we return 'false'.
 *
 * @param T The type of the test object to evaluate.
 * @param A The type of the parameter passed to the composable action.
 * @param predicate A lambda function representing the condition to evaluate on the test object.
 * @param action A composable lambda function to execute when the predicate evaluates to `true`.
 * @return A `[ConditionalAction]` that encapsulates the condition and the associated action logic.
 */
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

/**
 * Executes a conditional action based on the given `ConditionalActionInput` and test object.
 *
 * @param run The conditional action to be evaluated and executed if the condition is met.
 * @return A composable function that takes an input of type `A` and returns `true` if the condition is satisfied and the action is executed; otherwise, returns `false`.
 */
@Composable
fun <T, A> ConditionalActionInput<T, A>.action(
    run: ConditionalAction<T, A>
): @Composable (A) -> Boolean = run(testObject)

/**
 * Pulsing execution of conditional actions.
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
 *
 * Evaluation Details:
 * This function takes a list of actions,
 * starts to execute them in sequential order and stops after the first successful try.
 *
 * The idea behind this process is to provoke recomposition until all desired conditions are met.
 */
@Composable
tailrec fun sequentiallyExecuted(vararg conditionals: ExecutableAction<*, *>): Boolean = when{
    conditionals.isEmpty() -> false
    else -> {
        val first = conditionals.first()
        val executed = first.execute()
        if(executed) true
        else sequentiallyExecuted(*conditionals.drop(1).toTypedArray())
    }
}
