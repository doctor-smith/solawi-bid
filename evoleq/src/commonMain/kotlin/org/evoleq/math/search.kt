package org.evoleq.math

interface Children<T> {
    val getChildren: () -> List<T>
}

fun <T : Children<T>> T.search(predicate: (T) -> Boolean, children: List<T> = this.getChildren()): T? = when {
    predicate(this) -> this
    children.isEmpty() -> null
    else -> children.search(predicate)

}

tailrec fun <T : Children<T>> List<T>.search(predicate: (T) -> Boolean): T? = when{
    isEmpty() -> null
    else -> {
        val found = first().search(predicate)
        when(found) {
            null -> drop(1).search(predicate)
            else -> found
        }
    }
}
