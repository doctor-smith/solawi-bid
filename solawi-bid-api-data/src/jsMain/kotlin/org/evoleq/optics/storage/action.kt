package org.evoleq.optics.storage

import kotlinx.serialization.KSerializer
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.ResultListSerializer
import org.evoleq.ktorx.result.ResultSerializer
import org.evoleq.ktorx.result.Serializer
import org.evoleq.math.Reader
import org.evoleq.math.Writer
import org.evoleq.math.contraMap
import org.evoleq.math.map
import kotlin.reflect.KClass

/**
 * The action takes some input, which can be read from the application data with a reader [Reader<App, S>].
 * And it produces some output, which can be written to the application storage [Writer<App, T>]
 *
 * Action is functorial in S and T,
 * covariant in S and contravariant in T.
 * This turns Action into a Profunctor!
 */
data class Action<Base: Any, out I : Any,  O : Any>(

    val name: String,
    val reader: Reader<in Base, out I>,
    // key of the needed endpoint EndPoint<I,O>
    val endPoint: KClass<*>,
    val writer: Writer<Base, O>,

    val serializer: KSerializer<@UnsafeVariance I>,
    val deserializer: KSerializer<Result<O>>
)


/**
 * Constructor function for Actions
 */
inline fun <Base: Any, reified I : Any, reified O : Any> Action(
    name: String,
    noinline reader: Reader<in Base, out I>,
    // key of the needed endpoint EndPoint<I,O>
    endPoint: KClass<*>,
    noinline writer: Writer<Base, O>,
): Action<Base, I, O> = Action(
    name,
    reader,
    endPoint,
    writer ,
    Serializer<I>(),
    ResultSerializer<O>()
)


/**
 * Constructor function for Actions
 */
inline fun <Base: Any, reified I : Any, reified O : Any> Action(
    name: String,
    noinline reader: Reader<in Base, out I>,
    // key of the needed endpoint EndPoint<I,O>
    endPoint: KClass<*>,
    noinline writer: Writer<Base, List<O>>,
): Action<Base, I, List<O>> = Action(
    name,
    reader,
    endPoint,
    writer ,
    Serializer<I>(),
    ResultListSerializer<O>()
)

/**
 * Functoriality of Action
 */
inline fun <Base: Any, I : Any, reified J: Any,  reified O : Any> Action<Base, I, O>.map(noinline f: (I)->J): Action<Base, J, O> = Action(
    name,
    reader map f,
    J::class,
    writer,
    Serializer<J>(),
    ResultSerializer<O>()
)

/**
 * Contra-Functoriality of Action
 */
inline fun <Base: Any, reified I : Any,  reified O : Any, reified P : Any> Action<Base, I, O>.map(noinline f: (P)->O): Action<Base, I, P> = Action(
    name,
    reader,
    endPoint,
    writer contraMap f,
    Serializer<I>(),
    ResultSerializer<P>()
)




interface ActionType

data class Actions(
    private val dispatch: HashMap<ActionType, ()->Unit>
): Map<ActionType, ()->Unit> by dispatch

fun Actions(vararg actions: Pair<ActionType,()->Unit>): Actions = Actions(hashMapOf(*actions))

fun Actions.dispatch(type: ActionType): Unit = get(type)!!()


