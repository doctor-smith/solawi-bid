package org.solyton.solawi.bid.module.style.form

import org.jetbrains.compose.web.css.*

val formPageDesktopStyle: StyleScope.()->Unit by lazy { {
    width(100.percent)
    maxWidth(480.px)
    property("margin-left", "auto")
    property("margin-right", "auto")
} }

val formDesktopStyle: StyleScope.()->Unit by lazy { {
    width(100.percent)
    padding(28.px)
    borderRadius(12.px)
    backgroundColor(Color.white)
} }

val fieldDesktopStyle: StyleScope.()->Unit by lazy {{
    width(100.percent)
    display(DisplayStyle.Flex)
    flexDirection(FlexDirection.Column)
    alignItems(AlignItems.FlexStart)
    justifyContent(JustifyContent.Center)
    marginBottom(16.px)
}}

val formLabelDesktopStyle: StyleScope.()->Unit by lazy {{
    marginTop(10.px)
    marginBottom(4.px)
    width(100.percent)
    fontWeight("500")
    color(Color("#334155"))
}}

val formControlBarDesktopStyle: StyleScope.()->Unit by lazy { {
    marginTop(20.px)
    width(100.percent)
    display(DisplayStyle.Flex)
    flexDirection(FlexDirection.Row)
    justifyContent(JustifyContent.FlexEnd)
    gap(12.px)
} }

val textInputDesktopStyle: StyleScope.()->Unit by lazy {{
    marginTop(4.px)
    width(100.percent)
}}

val numberInputDesktopStyle: StyleScope.()->Unit by lazy {{
    marginTop(4.px)
    width(100.percent)
}}

val dateInputDesktopStyle: StyleScope.()->Unit by lazy {{
    backgroundColor(Color.white)
}}

val formButtonDesktopStyle: StyleScope.()->Unit by lazy {{
}}
