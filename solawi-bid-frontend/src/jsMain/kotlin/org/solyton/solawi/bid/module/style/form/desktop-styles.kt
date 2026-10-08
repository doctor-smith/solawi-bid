package org.solyton.solawi.bid.module.style.form

import org.jetbrains.compose.web.css.*

val formPageDesktopStyle: StyleScope.()->Unit by lazy { {
    padding(24.px)
} }

val formDesktopStyle: StyleScope.()->Unit by lazy { {
    width(100.percent)
    boxSizing("border-box")
} }

val fieldDesktopStyle: StyleScope.()->Unit by lazy {{
    width(100.percent)
    minWidth(0.px)
    display(DisplayStyle.Flex)
    flexDirection(FlexDirection.Column)
    alignItems(AlignItems.FlexStart)
    justifyContent(JustifyContent.Center)
    marginBottom(16.px)
    boxSizing("border-box")
}}

val formLabelDesktopStyle: StyleScope.()->Unit by lazy {{
    marginTop(0.px)
    marginBottom(6.px)
    width(100.percent)
    fontWeight("500")
    fontSize(14.px)
    color(Color("#334155"))
}}

val formControlBarDesktopStyle: StyleScope.()->Unit by lazy { {
    marginTop(8.px)
    width(100.percent)
    display(DisplayStyle.Flex)
    flexDirection(FlexDirection.Row)
    justifyContent(JustifyContent.FlexEnd)
    gap(12.px)
} }

val textInputDesktopStyle: StyleScope.()->Unit by lazy {{
    width(100.percent)
    boxSizing("border-box")
}}

val numberInputDesktopStyle: StyleScope.()->Unit by lazy {{
    width(100.percent)
    boxSizing("border-box")
}}

val dateInputDesktopStyle: StyleScope.()->Unit by lazy {{
    backgroundColor(Color.white)
    width(100.percent)
    boxSizing("border-box")
}}

val formButtonDesktopStyle: StyleScope.()->Unit by lazy {{
}}
