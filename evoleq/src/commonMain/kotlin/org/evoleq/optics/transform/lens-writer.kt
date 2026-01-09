package org.evoleq.optics.transform

import org.evoleq.math.MathDsl
import org.evoleq.math.Writer
import org.evoleq.math.merge
import org.evoleq.math.write
import org.evoleq.optics.lens.Lens

/**
 * Multiply a lens with a writer to transform and compose data structures.
 *
 * @param W The type of the whole structure.
 * @param P The type of the part within the structure that the lens focuses on.
 * @param Q The type of the part within the structure that the writer writes to.
 * @param writer The writer component to be combined with the lens.
 * @return A writer that operates on the whole structure W to transform and output type Q.
 */
operator fun <W, P, Q> Lens<W, P>.times(writer: Writer<P, Q>): Writer<W, Q> = this timesW writer

/**
 * Multiply a `Lens` with a `Writer` to produce a new `Writer`. The resulting `Writer` allows
 * modifications to the part of a structure focused by the `Lens`, using the provided `Writer`
 * transformation for that part.
 *
 * @param W The type of the entire structure.
 * @param P The type of the part within the structure being focused by the `Lens`.
 * @param Q The type produced by the transformation applied by the `Writer` to the focused part.
 * @param writer The `Writer` to apply to the part of the structure focused by the `Lens`.
 * @return A new `Writer` that, when invoked, applies the transformation to the focused part of
 *         the structure and updates the structure accordingly.
 */
@MathDsl
infix fun <W, P, Q> Lens<W, P>.timesW(writer: Writer<P, Q>): Writer<W, Q> = {
        q -> {w -> set(writer(q)(get(w)))(w)}
}

/**
 * Merges the existing list focused by the `Lens` with another list, using the provided equality
 * function to determine which items are considered equal.
 *
 * @param consideredEqual A function that takes two elements of type `P` and returns a boolean
 *                         indicating whether the two elements are considered equal.
 * @return A `Writer` that produces a list of merged elements when applied on the wrapped structure.
 */
@MathDsl
inline infix fun <W, reified P> Lens<W, List<P>>.merge(crossinline consideredEqual: (P, P)->Boolean): Writer<W, List<P>> =
    this timesW Writer{inc -> {given -> given.merge(inc,consideredEqual)}}

/**
 * Injects a `Writer` into the composite structure represented by the `Lens`.
 *
 * @param W The type of the whole structure accessed and updated by the `Lens`.
 * @param P The type of the elements in the list contained within the structure.
 * @param Q The type of the transformed list elements after applying the `Writer`.
 * @param w The `Writer` that transforms elements of type `P` to type `Q`.
 * @param select A selector function that takes an element of type `P` and a list of `Q`,
 *               returning the specific `Q` to be applied to the writer.
 * @return A `Writer` that writes a list `List<Q>` of qs to W by applying the writer w to each element p of the ps in W.
 */
@MathDsl
@Throws(LensWriterException::class)
fun <W, P, Q> Lens<W, List<P>>.inject( write: Writer<P, Q>,select: (P, List<Q>) -> Q): Writer<W, List<Q>> = { qs: List<Q> -> { w: W ->
    set(get(w).map { p: P -> write(try{select(p, qs)} catch (_: Exception){throw LensWriterException.InvalidSelector})(p)})(w)
}}

/**
 * Injects a transformation function into a `Lens` that focuses on a `List` structure, enabling a composition
 * with a `Writer` to produce a transformed `Writer` for the whole structure.
 *
 * **Important note:**
 *
 * Using a 'Writer<P, Q?>' with nullable 'part' allows us to model the case, where the qs refer to a subset of ps only,
 * i.e., where we do NOT have a p for each q and the selection would fail.
 *
 * **Example**
 * ```
 * val writer: Writer<P, Q?> = Writer{
 *      q: Q? -> {p -> when{
 *          q == null -> p
 *          else -> TODO(inject q into p)
 *      }}
 * }
 * ```
 *
 * @param W The type of the whole structure.
 * @param P The type of the elements in the focused list within the structure.
 * @param Q The output type produced by the injected writer transformation.
 * @param w A `Writer` that operates on elements of the list with type `P`.
 * @return A higher-order function that, given a selector function of type `(P, List<Q>) -> Q`, produces a
 *         `Writer` that describes transformations on the whole structure `W` and produces
 *         a list of `Q` as the resulting output.
 */
@MathDsl
@Throws(LensWriterException.InvalidSelector::class)
infix fun <W, P, Q> Lens<W, List<P>>.inject(w: Writer<P, Q>): ((P, List<Q>) -> Q) -> Writer<W, List<Q>> = {select -> { qs: List<Q> -> { w: W ->
    this@inject.set(this@inject.get(w).map { p: P -> w(select(p, qs))(p) })(w)
}}}

/**
 * Composes a function that creates a `Writer` instance using a selector function to process a pair of inputs.
 *
 * **Important note:**
 *
 * Using a 'Writer<P, Q?>' with nullable 'part' allows us to model the case, where the qs refer to a subset of ps only,
 * i.e., where we do NOT have a p for each q and the selection would fail.
 *
 * **Example**
 * ```
 * val writer: Writer<P, Q?> = Writer{
 *      q: Q? -> {p -> when{
 *          q == null -> p
 *          else -> TODO(inject q into p)
 *      }}
 * }
 * ```
 *
 * @param select the selector function that processes a value of type `P` and a list of type `Q` to produce a value of type `Q`.
 * @return a `Writer` instance that wraps a list of type `Q`.
 */
@MathDsl
@Throws(LensWriterException.InvalidSelector::class)
infix fun <W, P, Q> (((P, List<Q>) -> Q) -> Writer<W, List<Q>>).by(select: (P, List<Q>) -> Q): Writer<W, List<Q>> = this(select)

/**
 * Transforms a `Writer<P, Q>` into a `Writer<List<P>, List<Q>>` by applying a selection function
 * to handle individual elements of `P` and `Q`.
 *
 * **Important note:**
 *
 * Using a 'Writer<P, Q?>' with nullable 'part' allows us to model the case, where the qs refer to a subset of ps only,
 * i.e., where we do NOT have a p for each q and the selection would fail.
 *
 * **Example**
 * ```
 * val writer: Writer<P, Q?> = Writer{
 *      q: Q? -> {p -> when{
 *          q == null -> p
 *          else -> TODO(inject q into p)
 *      }}
 * }
 * ```
 *
 * @param select A function that determines how to map an element of type `P` and a list of type `Q`
 *               to a specific element of type `Q`.
 * @return A `Writer` that processes lists of `P` and `Q` based on the provided `select` function.
 */
@MathDsl
@Throws(LensWriterException.InvalidSelector::class)
infix fun <P, Q> Writer<P, Q>.liftBy(select: (P, List<Q>) -> Q): Writer<List<P>, List<Q>> = Writer{
    qs: List<Q> -> {ps: List<P> -> ps.map { p: P -> write(
    try { select(p, qs)} catch(_: Exception){ throw LensWriterException.InvalidSelector})(p)
    }}
}

sealed class LensWriterException(override val message: String) : Exception(message) {
    data object InvalidSelector : LensWriterException("Selector invalid")
}
