package org.evoleq.compose.modal

import org.jetbrains.compose.web.css.StyleScope

data class ModalStyles(
    val containerStyle: StyleScope.()->Unit = {},
    val contentWrapperStyle: StyleScope.()->Unit = {},
    val okButtonStyles: StyleScope.()->Unit = {},
    val cancelButtonStyles: StyleScope.()->Unit = {},
    val okButtonDisabled: Boolean = false
)