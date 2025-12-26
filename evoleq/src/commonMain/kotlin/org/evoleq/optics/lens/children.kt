package org.evoleq.optics.lens

import org.evoleq.identity.Identity
import org.evoleq.math.Children
import org.evoleq.math.MathDsl
import org.evoleq.optics.exception.OpticsException

/**
 * Creates a lens for working with the children of a structure that implements the `Children` interface.
 * The lens focuses on the list of children within the structure, allowing functional transformations
 * and retrieval without mutating the original structure.
 *
 * @return A `Lens` that enables getting and setting the list of children of type `T` for structures
 * implementing the `Children` interface.
 */
@MathDsl
@Suppress("FunctionName")
fun <T : Children<T>> Children(): Lens<T, List<T>> = Lens(
    get = {t -> t.getChildren()},
    set = {children -> {t -> t.setChildren(children)}}
)

/**
 * Performs a deep search on a list of items, returning a functional `LensType` that
 * focuses on the first element in the list and its nested structure matching a given predicate.
 * The items in the list must implement the `Children` and `Identity` interfaces to support navigation
 * through their nested structure and identification.
 *
 * @param predicate A function that defines the condition for selecting the desired element.
 *                  It determines if an element matches the search criteria.
 * @return A `LensType` that facilitates both accessing and updating the first element
 *         within the list and the nested structure that satisfies the given predicate.
 */
@MathDsl
@Suppress("FunctionName")
fun <T, I> DeepSearch(predicate: (T)-> Boolean): Lens<List<T>, T> where T : Children<T>, T: Identity<I> {

    var lens: Lens<List<T>, T>? = null

    fun build(list: List<T>, predicate: (T)-> Boolean): Lens<List<T>, T>  {
        val paths = list.associate { it.getIdentity() to it.Step(predicate, emptyList()) }

        val focusPaths = paths.values.first { it.isNotEmpty() }
        val focus: LensType<T, T> = Focus(focusPaths.first(), *focusPaths.drop(1).toTypedArray())

        return FirstBy { item: T -> paths[item.getIdentity()]!!.isNotEmpty() } * focus
    }

    return lens ?: Lens(
        get = lens?.get?:{list: List<T> -> with(build(list, predicate)){
            lens = this
            get(list)
        }},
        set = lens?.set?:{t: T -> { list -> with(build(list,predicate)){
            lens = this
            set(t)(list)
        } }}
    )
}

/**
 * Performs a deep search on a hierarchical structure of type `T`, using the provided predicate to
 * determine a focus within the structure. If the predicate matches, a lens representing the
 * focused part of the structure is returned. If no match is found, an exception is thrown.
 *
 * @param T The type of the data structure being searched, which must implement `Children` and `Identity`.
 * @param I The type of the identity value within the structure.
 * @param predicate A function that determines the matching condition for the focus within the structure.
 * @return A `LensType` that provides access to and allows transformation of the matched part of the structure.
 * @throws OpticsException.Lens.DeepSearch.Empty if the search path is empty or no match is found.
 */
@MathDsl
@Suppress("FunctionName")
fun <T, I> T.DeepSearch(predicate: (T)-> Boolean): LensType<T, T> where T : Children<T>, T: Identity<I> {
    val path = Step(predicate, emptyList<LensType<T, T>>())
    return when{
        path.isEmpty() -> Lens(
            get = {_: T -> throw OpticsException.Lens.DeepSearch.Empty},
            set = {throw OpticsException.Lens.DeepSearch.Empty})
        else -> Focus(path.first(), *path.drop(1).toTypedArray())
    }
}

/**
 * Combines a series of lenses into a single composite lens, providing a focused
 * and functional way to access or transform nested data within a structure.
 *
 * @param T The type of the structure being manipulated.
 * @param first The first lens in the sequence, which acts as the starting point
 *              for composition.
 * @param path A variadic list of additional lenses to be combined with the first lens.
 * @return A composite lens that represents the combination of the provided lenses.
 */
@MathDsl
@Suppress("FunctionName")
fun <T> Focus(first: LensType<T, T>, vararg path: LensType<T, T>): LensType<T, T> = path.fold(first) {
    acc, lens -> acc * lens
}

/**
 * Traverses a data structure based on a predicate and returns a list of lens types representing
 * the traversal path where the predicate is satisfied. This method is defined for types that
 * have children and an identity.
 *
 * @param predicate A lambda function to evaluate each element in the structure. The traversal
 *                  stops and the path is returned if the predicate returns true for an element.
 * @param path A list representing the current path of lens transformations. This is used to
 *             accumulate the path during recursive traversal.
 * @return A list of `LensType` objects representing the path to the first element where the
 *         predicate is satisfied. If no such element is found, an empty list is returned.
 */
@MathDsl
@Suppress("FunctionName", "ReturnCount")
fun <T, I> T.Step(predicate: (T)-> Boolean, path: List<LensType<T, T>>): List<LensType<T, T>> where T : Children<T>, T: Identity<I> {
    if(predicate(this)) return (path + IdLens())
    val children = this.getChildren()
    if(children.isEmpty()) return emptyList()

    return children.Step(predicate, path)
}

/**
 * Recursively traverses a list of elements to find a path of transformations that satisfies
 * the given predicate. If a match is found, a list of `LensType` elements representing the
 * series of transformations leading to the match is returned.
 *
 * @param predicate A lambda function that takes an element of type `T` and returns `true`
 *                  if the element satisfies the condition, otherwise `false`.
 * @param path A list of `LensType<T, T>` objects representing the current traversal path.
 *             This is used to accumulate the path during the recursive traversal.
 * @return A list of `LensType<T, T>` objects representing the traversal path to the
 *         element that satisfies the predicate. If no element matches the predicate,
 *         an empty list is returned.
 */
@MathDsl
@Suppress("FunctionName", "ReturnCount")
tailrec fun <T, I> List<T>.Step(predicate: (T)-> Boolean, path:List<LensType<T, T>>): List<LensType<T, T>> where T : Children<T>, T: Identity<I> {
    if(isEmpty()) return emptyList()

    val first = first()
    val firstPath = first.Step(predicate,path)
    if(firstPath.isNotEmpty()) return path + (Children<T>() * FirstBy { it.getIdentity() == first.getIdentity() }) + firstPath

    return drop(1).Step(predicate, path)
}
