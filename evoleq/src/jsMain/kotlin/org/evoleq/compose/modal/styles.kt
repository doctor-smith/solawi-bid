package org.evoleq.compose.modal

import org.jetbrains.compose.web.css.*

data class ModalStyles(
    val containerStyle: StyleScope.()->Unit = {
        minWidth(400.px)
        width(96.vw)
        minHeight(300.px)
        alignSelf(AlignSelf.SelfStart)
        property("margin", "0 auto")
        property(
            "box-shadow",
            "0px 4px 12px rgba(0, 0, 0, 0.5)"
        )
    },
    val contentWrapperStyle: StyleScope.()->Unit = {},
    val footerWrapperStyle:  StyleScope.()->Unit = {},
    val okButtonStyles: StyleScope.()->Unit = {},
    val cancelButtonStyles: StyleScope.()->Unit = {},
    val okButtonDisabled: Boolean = false
) {
    fun modifyContainerStyle(styles: StyleScope. ()->Unit): ModalStyles = copy(containerStyle = {
        containerStyle()
        styles()
    })

    fun modifyContentWrapperStyle(styles: StyleScope. ()->Unit): ModalStyles = copy(contentWrapperStyle = {
        contentWrapperStyle()
        styles()
    })

    fun modifyFooterWrapperStyle(styles: StyleScope. ()->Unit): ModalStyles = copy(footerWrapperStyle = {
        footerWrapperStyle()
        styles()
    })

    fun modifyOkButtonStyles(styles: StyleScope. ()->Unit): ModalStyles = copy(okButtonStyles = {
        okButtonStyles()
        styles()
    })

    fun modifyCancelButtonStyles(styles: StyleScope. ()->Unit): ModalStyles = copy(cancelButtonStyles = {
        cancelButtonStyles()
        styles()
    })
    
    fun compact(): ModalStyles = modifyContainerStyle {
        property("width", "fit-content")
        property("height", "fit-content")
    }
}
