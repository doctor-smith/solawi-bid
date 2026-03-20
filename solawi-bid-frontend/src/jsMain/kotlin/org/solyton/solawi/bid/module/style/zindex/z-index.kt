package org.solyton.solawi.bid.module.style.zindex

import org.jetbrains.compose.web.css.StyleScope

fun StyleScope.zIndex(index: Int) = property("z-index", "$index")
