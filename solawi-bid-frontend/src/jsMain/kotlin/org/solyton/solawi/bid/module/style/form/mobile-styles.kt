package org.solyton.solawi.bid.module.style.form

import org.jetbrains.compose.web.css.*


val formPageMobileStyle: StyleScope.()->Unit by lazy { {
    width(94.percent)
    property("margin-left", "auto")
    property("margin-right", "auto")
} }

val formMobileStyle: StyleScope.()->Unit by lazy {{
    width(100.percent)
    padding(16.px)
    borderRadius(12.px)
    backgroundColor(Color.white)
}}

val fieldMobileStyle: StyleScope.()->Unit by lazy {{
    width(100.percent)
    display(DisplayStyle.Flex)
    flexDirection(FlexDirection.Column)
    alignItems(AlignItems.FlexStart)
    justifyContent(JustifyContent.Center)
    marginBottom(12.px)
}}

val formLabelMobileStyle: StyleScope.()->Unit by lazy { {
    marginTop(10.px)
    marginBottom(4.px)
    width(100.percent)
    fontWeight("500")
    color(Color("#334155"))
} }

val formControlBarMobileStyle: StyleScope.()->Unit by lazy { {
    marginTop(16.px)
    width(100.percent)
    display(DisplayStyle.Flex)
    flexDirection(FlexDirection.Column)
    gap(8.px)
} }

val textInputMobileStyle: StyleScope.()->Unit by lazy {{
    marginTop(4.px)
    width(100.percent)
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
