package org.evoleq.compose.modal

import org.jetbrains.compose.web.css.StyleScope
import org.jetbrains.compose.web.css.flexGrow
data class ModalStyles(
    val containerStyle: StyleScope.()->Unit = {},
    val contentWrapperStyle: StyleScope.()->Unit = {
        //flexGrow(1)
    },
    val footerWrapperStyle:  StyleScope.()->Unit = {},
    val okButtonStyles: StyleScope.()->Unit = {},
    val cancelButtonStyles: StyleScope.()->Unit = {},
    val okButtonDisabled: Boolean = false
)
