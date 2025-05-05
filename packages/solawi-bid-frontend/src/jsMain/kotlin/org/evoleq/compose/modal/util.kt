package org.evoleq.compose.modal

import androidx.compose.runtime.Composable
import org.evoleq.optics.storage.Storage
import org.jetbrains.compose.web.dom.ElementScope
import org.w3c.dom.HTMLElement

fun <Id> Storage<Modals<Id>>.components(type: ModalType): List<@Composable ElementScope<HTMLElement>.() -> Unit> =
    read().values.filter { it.type == type }.map { it.component }