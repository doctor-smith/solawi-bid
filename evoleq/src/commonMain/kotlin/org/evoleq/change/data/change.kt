package org.evoleq.change.data

interface Change<out T> {
    val old: T?
    val new: T?
    val onChange: () -> Unit
}

fun <T> Change(oldValue: T?, newValue: T?, onChange: ()->Unit = {}): Change<T> = object : Change<T> {
    override val old: T? = oldValue
    override val new: T? = newValue
    override val onChange: () -> Unit = {
        if(new != old) onChange()
    }
}

@Suppress("FunctionName")
fun <T> Keep(oldValue: T?): Change<T> = Change(oldValue, oldValue)
