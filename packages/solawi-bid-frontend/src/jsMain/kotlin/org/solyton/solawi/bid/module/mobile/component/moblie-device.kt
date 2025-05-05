package org.solyton.solawi.bid.module.mobile.component

import androidx.compose.runtime.*
import org.evoleq.compose.Markup
import org.jetbrains.compose.web.ExperimentalComposeWebApi
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.ElementScope
import org.w3c.dom.HTMLElement

@OptIn(ExperimentalComposeWebApi::class)
@Markup
@Composable
@Suppress("FunctionName")
fun MobileDevice(scale: Double, content: @Composable ElementScope<HTMLElement>.()->Unit) {


    // State for dynamic screen color
    var screenColor by remember { mutableStateOf(Color.white) }

    Div(attrs = {
        style {
            width((300 * scale).px) // Scale width
            height((600 * scale).px) // Scale height
            backgroundColor(Color.black)
            borderRadius((40 * scale).px)
            position(Position.Relative)
            display(DisplayStyle.Flex)
            flexShrink(0) // Prevent shrinking in flex containers
            justifyContent(JustifyContent.Center)
            alignItems(AlignItems.Center)
            alignSelf(AlignSelf.Center) // Keep centered in flex parents
        }
    }) {
        // Notch at the top
        Div(attrs = {
            style {
                width((100 * scale).px)
                height((20 * scale).px)
                backgroundColor(Color("#222"))
                position(Position.Absolute)
                top((10 * scale).px)
                left(50.percent)
                transform { translateX((-50).percent) }
                borderRadius((10 * scale).px)
            }
        })

        // Screen with dynamic color on click
        Div(attrs = {
            style {
                width(85.percent)
                height(90.percent)
                backgroundColor(screenColor)
                borderRadius((30 * scale).px)
                overflow("hidden")
                display(DisplayStyle.Flex)
                justifyContent(JustifyContent.Center)
                alignItems(AlignItems.Center)
                fontSize((20 * scale).px)
                color(Color.darkgray)
                cursor("pointer")
            }
            // onClick { screenColor = if (screenColor == Color.white) Color.lightgray else Color.white }
        }) {
            content()
        }
/*
        // Home Button
        Div(attrs = {
            style {
                width((50 * scale).px)
                height((50 * scale).px)
                backgroundColor(Color("#444"))
                position(Position.Absolute)
                bottom((15 * scale).px)
                left(50.percent)
                transform { translateX((-50).percent) }
                borderRadius(50.percent)
            }
        })

 */
    }


}