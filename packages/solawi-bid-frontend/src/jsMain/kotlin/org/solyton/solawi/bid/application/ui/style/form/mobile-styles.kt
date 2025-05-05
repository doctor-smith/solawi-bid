package org.solyton.solawi.bid.application.ui.style.form

import org.jetbrains.compose.web.css.*

const val mobileFontSizeFactor = 1.6

val formPageMobileStyle: StyleScope.()->Unit by lazy { {
    width(94.percent)
    marginLeft(3.percent)
} }

val formMobileStyle: StyleScope.()->Unit by lazy {{
    width(100.percent)
    padding(20.px)
    borderRadius(16.px)
}}

val fieldMobileStyle: StyleScope.()->Unit by lazy {{
    width(100.percent)
    display(DisplayStyle.Flex)
    flexDirection(FlexDirection.Column)
    alignItems(AlignItems.Center)
    justifyContent(JustifyContent.Center)
}}

val formLabelMobileStyle: StyleScope.()->Unit by lazy { {
    marginTop(20.px)
    width(100.percent)
} }

val formControlBarMobileStyle: StyleScope.()->Unit by lazy { {
    fontSize(mobileFontSizeFactor.em)
    marginTop(20.px)
    width(100.percent)
    display(DisplayStyle.Flex)
    flexDirection(FlexDirection.Column)
    //height(80.px)
    //alignItems(AlignItems.Center)
    //justifyContent(JustifyContent.Center)
} }

val textInputMobileStyle: StyleScope.()->Unit by lazy {{
    fontSize(mobileFontSizeFactor.em)
    marginTop(20.px)
    width(100.percent)
}}

val numberInputMobileStyle: StyleScope.()->Unit by lazy {{
    fontSize(mobileFontSizeFactor.em)
    marginTop(20.px)
    width(100.percent)
}}

val dateInputMobileStyle: StyleScope.()->Unit by lazy {{
    fontSize(mobileFontSizeFactor.em)
    backgroundColor(Color.white)
}}

val formButtonMobileStyle: StyleScope.()->Unit by lazy {{
    property("font-size", "${mobileFontSizeFactor.em} !important" )
    //fontSize(mobileFontSizeFactor.em)
    marginTop(20.px)
    padding(10.px)
    width(100.percent)
    //height(100.percent)
    //minHeight(50.px)
}}
