package org.evoleq.optics.lens

import org.evoleq.math.Maths
import org.evoleq.math.o
import org.evoleq.math.x as X

/**
 * LensType represents a generic interface for functional lenses, allowing both focused
 * access and composable transformations of data nested within structures. It forms
 * the foundation for various lens implementations and operations, enabling data
 * manipulation while maintaining immutability.
 *
 * @param W The type of the main structure being manipulated.
 * @param P The type of the specific part within the structure being focused on.
 */
sealed interface LensType<W, P>

/**
 * Type representing the identity lens. It is meant to be the neutral element w.r.t. lens multiplication.
 */
class IdLens<T> : LensType<T, T>

/**
 * Represents a `Lens`, a powerful functional programming construct for accessing and updating
 * nested structures without mutating the original object. A `Lens` provides a composable way
 * to focus on a part of a larger structure, perform transformations or retrieval of data
 * while maintaining immutability.
 *
 * @param W The type of the whole structure.
 * @param P The type of the focused part within the structure.
 * @property get A function that retrieves the focused part from the whole structure.
 * @property set A function that, given a new focused part, creates a transformation
 *               function to update the whole structure.
 */
@Maths
data class Lens<W, P> (
    val get: (W) -> P,
    val set: (P) -> (W) -> W
) : LensType<W,P>

/**
 * Lens Multiplication:
 * Combines two lenses into a composite lens by performing a multiplication operation.
 *
 * @param W the source type of the first lens and the resulting composite lens.
 * @param P the intermediary type shared between the two lenses.
 * @param D the target type of the second lens and the resulting composite lens.
 * @param other the lens to be multiplied with this lens.
 * @return a new lens resulting from the combination of this lens and the given lens.
 */
@Maths
operator fun <W, P, D> Lens<W, P>.times(other: Lens<P, D>): Lens<W, D> = Lens(
    other.get o get) {
    with(other.set(it)) {
        {w:W ->   (set o this o get) (w) (w) }
    }
}

/**
 * Lens multiplication with identity: [Lens] * [IdLens].
 *
 * @param id The identity lens of type [T] to combine with this lens.
 * @return A new [Lens] that represents the composition of the current lens and the given identity lens.
 */
operator fun <S, T> Lens<S, T>.times(id: IdLens<T>): Lens<S, T> = this

/**
 * Lens multiplication with identity: [IdLens] * [Lens]
 *
 * The `IdLens` represents an identity lens, meaning it has no effect on the data.
 * This operator essentially returns the other `Lens` unchanged, allowing seamless
 * integration of `IdLens` with general lens constructs.
 *
 * @param S The source type for the `Lens`.
 * @param T The target type for the `Lens`.
 * @param other The `Lens` to be combined with the `IdLens`.
 * @return The resulting `Lens` which is equivalent to the `other` `Lens`.
 */
operator fun <S, T> IdLens<S>.times(other: Lens<S, T>): Lens<S, T> = other

/**
 * Defines the multiplication operation for `IdLens` objects, combining two identity lenses into one.
 *
 * @param other Another `IdLens` to be multiplied with this `IdLens`.
 * @return A new `IdLens` resulting from the multiplication of the two input `IdLens` objects.
 */
operator fun <T> IdLens<T>.times(other: IdLens<T>): IdLens<T> = this

/**
 * Combines two LensType objects into a single LensType by applying a multiplier-like operation.
 *
 * @param other The other LensType to combine with the current one.
 * @return The resulting LensType after combining this LensType with the other.
 */
operator fun <T> LensType<T, T>.times(other: LensType<T, T>): LensType<T, T> = when(this){
    is IdLens<T> -> other
    is Lens<T, T> -> when(other) {
        is IdLens<T> -> this
        is Lens<T, T> -> lensTimes(this,other)
    }
}

operator fun <S, T> Lens<S, T>.times(other: LensType<T, T>): Lens<S, T> = when(other){
    is IdLens<T> -> this
    is Lens<T, T> -> lensTimes(this, other)
}

/**
 * Helper function to distinguish different kinds of lens-multiplication
 * Combines two lenses into a new lens by sequentially connecting them.
 *
 * @param fst The first lens to be composed, which extracts part of the data structure.
 * @param snd The second lens to be composed, further refining the focus of the first lens.
 * @return A new lens that represents the combination of both input lenses, enabling a focus from the first structure to the final target.
 */
fun <R, S, T> lensTimes(fst: Lens<R, S>, snd: Lens<S, T>): Lens<R, T> = fst * snd

/**
 * Products of Lenses:
 * Composes two lenses to create a new lens that focuses on the pair of the structures and values targeted
 * by the original lenses.
 *
 * This method enables the combination of two independent lenses into a single lens that focuses on a pair structure
 * composed of the two original structures and their respective values. It operates using functional composition on
 * the `get` and `set` methods of the involved lenses.
 *
 * @param V The type of the structure targeted by the first lens.
 * @param W The type of the structure targeted by the second lens.
 * @param P The type of the value focused by the first lens.
 * @param Q The type of the value focused by the second lens.
 * @param other Another lens that will be combined with this lens.
 * @return A new lens that targets a pair structure (`Pair<V, W>`) and focuses on a pair of values (`Pair<P, Q>`).
 */
@Maths
fun <V, W, P, Q> Lens<V, P>.x(other: Lens<W, Q>): Lens<Pair<V, W>, Pair<P, Q>> = Lens(
    get X other.get
) {
        pXq -> set(pXq.first) X other.set(pXq.second)
}

/**
 * Products Lenses: Focus on the product of types
 * Composes two `Lens` objects to focus on a pair of values within the same structure.
 *
 * This method combines the functionalities of two `Lens` objects into a single `Lens`
 * that focuses on a `Pair` containing the results from both lenses. It allows you to
 * access and update two separate parts of a structure concurrently, preserving immutability.
 *
 * @param W The type of the whole structure from which both lenses operate.
 * @param P The type of the part focused on by the first lens.
 * @param Q The type of the part focused on by the second lens.
 * @param other Another `Lens` that operates on the same structure and focuses on a different part.
 * @return A new `Lens` that focuses on a `Pair` of values, combining the results of both lenses.
 */
@Maths
infix fun <W, P, Q> Lens<W, P>.pX(other: Lens<W, Q>): Lens<W, Pair<P, Q>> = Lens(
    get = {w -> get(w) X other.get(w)}
) {
        pXq -> set(pXq.first) o other.set(pXq.second)
}

/**
 * Products Lenses: Focus on the product of types
 * Combines two lenses to focus on a `Triple` constructed from the focused types of the provided pair lens
 * and the lens for a third component.
 *
 * This function allows for the composition of a `Lens<W, Pair<P, Q>>` and a `Lens<W, R>` into a new `Lens<W, Triple<P, Q, R>>`,
 * enabling access to and modification of nested structures represented by such triples.
 *
 * @param W The type of the whole structure on which the lenses operate.
 * @param P The type of the first element in the pair focused by the first lens.
 * @param Q The type of the second element in the pair focused by the first lens.
 * @param R The type of the component focused by the second lens.
 * @param other A lens focusing on the third component of the resulting `Triple` within the same overall structure.
 * @return A new `Lens<W, Triple<P, Q, R>>` that focuses on a `Triple` where the first two elements come from the pair
 *         focused by the first lens and the third element comes from the second lens.
 */
@Maths
infix fun <W, P, Q, R> Lens<W, Pair<P, Q>>.tX(other: Lens<W, R>): Lens<W, Triple<P, Q, R>> = Lens(
    get = {w -> with(get(w)){ Triple(first, second, other.get(w))}}
) {
        pXqXr -> set(pXqXr.first X pXqXr.second) o other.set(pXqXr.third)
}
