package org.evoleq.configuration


interface Configuration<out T> {
    fun configure(): T
}
