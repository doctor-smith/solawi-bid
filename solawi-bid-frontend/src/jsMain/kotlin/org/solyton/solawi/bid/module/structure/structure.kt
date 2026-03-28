package org.solyton.solawi.bid.module.structure

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup

@Markup
@Composable
@Suppress( "UnusedParameter")
fun  s(name: String? = null, content: @Composable ()->Unit) = content()


