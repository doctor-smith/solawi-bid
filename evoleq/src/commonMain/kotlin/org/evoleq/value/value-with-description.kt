package org.evoleq.value



interface ValueWithDescription<out T>: Value<T> {
    val description: String
}

interface StringValueWithDescription : ValueWithDescription<String>
