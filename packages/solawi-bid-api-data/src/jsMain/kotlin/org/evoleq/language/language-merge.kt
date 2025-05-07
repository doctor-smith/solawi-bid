package org.evoleq.language




tailrec fun List<Lang>.merge(list: List<Lang>): List<Lang> = when{
    list.isEmpty() -> this
    else -> {
        val merged = merge(list.first())
        merged.merge(list.drop(1))
    }
}

fun List<Lang>.merge(item: Lang): List<Lang> {
    val found: Lang? = find { it.key == item.key }
    return when{
        found == null -> listOf(*this.toTypedArray(), item)
        else -> {
            val tmpResult = when(found) {
                is Lang.Variable -> item
                is Lang.Block -> when (item) {
                    is Lang.Variable -> item
                    is Lang.Block -> {
                        Block(item.key, found.value.merge(item.value))
                    }
                }
            }
            listOf(
                *this.filter { it.key != item.key }.toTypedArray(), tmpResult
            )
        }
    }
}

fun Block.merge(block: Block): Block = when{
    key == block.key -> Block(
        key,
        value.merge(block.value)
    )
    else -> throw Exception("Cannot merge blocks")
}

fun Lang.merge(lang: Lang): Lang = when{
    key == lang.key -> when(this) {
        is Block -> when(lang) {
            is Lang.Variable -> lang
            is Block -> merge(lang)
        }
        else -> lang
    }
    else -> throw Exception("Cannot merge langs")
}