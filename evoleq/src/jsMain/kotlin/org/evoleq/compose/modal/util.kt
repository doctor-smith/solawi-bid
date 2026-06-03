package org.evoleq.compose.modal

import org.evoleq.math.Source
import org.evoleq.optics.storage.Storage
fun <Id> Storage<Modals<Id>>.components(type: ModalType<Id>): Source<List<ModalData<Id>>> = Source {
    val roots = read().values.filter { it.type == type }
    roots + read().values.toList().collectChildren(roots.map { it.id })
}

fun <Id> List<ModalData<Id>>.collectChildren(parentIds: List<Id>): List<ModalData<Id>> {
    val children = parentIds.flatMap { parentId ->
        filter { it.type is ModalType.Child<Id> && it.type.parentId == parentId }
    }
    console.log("Found ${children.size} children of $parentIds")
    if(children.isEmpty()) return emptyList()
    return children + collectChildren(children.map { it.id })
}
