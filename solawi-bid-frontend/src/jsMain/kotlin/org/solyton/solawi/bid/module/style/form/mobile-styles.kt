package org.solyton.solawi.bid.module.style.form

import org.jetbrains.compose.web.css.*


val formPageMobileStyle: StyleScope.()->Unit by lazy { {
    padding(16.px)
} }

val formMobileStyle: StyleScope.()->Unit by lazy {{
    width(100.percent)
    maxWidth(400.px)
    padding(20.px)
    borderRadius(12.px)
    backgroundColor(Color.white)
    boxSizing("border-box")
}}

val fieldMobileStyle: StyleScope.()->Unit by lazy {{
    width(100.percent)
    display(DisplayStyle.Flex)
    flexDirection(FlexDirection.Column)
    alignItems(AlignItems.FlexStart)
    justifyContent(JustifyContent.Center)
    marginBottom(14.px)
}}

val formLabelMobileStyle: StyleScope.()->Unit by lazy { {
    marginTop(0.px)
    marginBottom(6.px)
    width(100.percent)
    fontWeight("500")
    fontSize(14.px)
    color(Color("#334155"))
} }

val formControlBarMobileStyle: StyleScope.()->Unit by lazy { {
    marginTop(8.px)
    width(100.percent)
    display(DisplayStyle.Flex)
    flexDirection(FlexDirection.Column)
    gap(8.px)
} }

val textInputMobileStyle: StyleScope.()->Unit by lazy {{
    width(100.percent)
    boxSizing("border-box")
}}

val numberInputMobileStyle: StyleScope.()->Unit by lazy {{
    marginTop(4.px)
    width(100.percent)
}}

val dateInputMobileStyle: StyleScope.()->Unit by lazy {{
    backgroundColor(Color.white)
}}

val formButtonMobileStyle: StyleScope.()->Unit by lazy {{
    marginTop(10.px)
    width(100.percent)
}}
