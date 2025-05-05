package org.evoleq.compose.attribute

import org.evoleq.compose.Markup
import org.jetbrains.compose.web.attributes.AttrsScope
import org.w3c.dom.Element

@Markup
fun <T: Element> AttrsScope<T>.disabled(): AttrsScope<T> = attr("disabled","true")
