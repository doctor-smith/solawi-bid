package org.evoleq.language

interface Language {
    val locale: String
}



sealed class Lang(open val key: String) {
    data class Variable(
        override val key: String,
        val value: String
    ) : Lang(key)
    data class Block(
        override val key: String,
        val value: List<Lang>
    ) : Lang(key)
}

fun Lang?.find(name: String): Lang? = when(this) {
    null -> null
    is Lang.Variable -> this
    is Lang.Block -> value.find { this.key == name }
}

@I18N
tailrec operator fun Lang.get(path: String): String {
    val (result, rest) = Segment().run(path)

    return when (this@get) {
        is Var -> this@get.value
        is Block -> {
            val found = this@get.value.find { it.key.equals(result!!) }
            when (found) {
                null -> throw LanguageException.ElementNotFoundInBlock(key = result!!, block = this@get.key)
                else -> found[rest]
            }
        }
    }
}

@I18N
infix fun String.ofComponent(component: Block): Block = component.component(this)

@I18N
infix fun String.of(component: Block): Block = component.component(this)

@I18N
tailrec fun Block.component(path: String): Block {
    val (head, rest) = with(Segment().run(path)) { result!! to rest }
    val found = this@component.value.find { it.key.equals( head ) }

    return when(found)  {
        null, is Var -> throw LanguageException.BlockNotFoundInBlock(block = this@component.key ,key = head)
        is Block -> when(rest == ""){
            true -> found
            false -> found.component(rest)
        }
    }
}
