package org.evoleq.compose.layout

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text


data class Property<T>(
    val key: String,
    val value: T,
    val format: (T)->String = {"$it"}
)

data class PropertiesStyles(
    val containerStyle: StyleScope.()->Unit = {},
    val propertyStyles: PropertyStyles = PropertyStyles()
)

data class PropertyStyles(
    val propertyStyle: StyleScope.()->Unit = {
        margin(10.px)
    },
    val keyStyle: StyleScope.()->Unit = {
        width(20.percent)
        alignContent(AlignContent.Start)
    },
    val valueStyle: StyleScope.()->Unit = {
        alignContent(AlignContent.Start)
    }
)

@Markup
@Composable
@Suppress("FunctionName")
fun <T> ReadOnlyProperties(properties: List<Property<T>>, styles: PropertiesStyles = PropertiesStyles()) {
    Div(attrs = {
        style {
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Column)
            with(styles){containerStyle()}
        }
    }) {
        properties.forEach {
            ReadOnlyProperty(it, styles.propertyStyles)
        }
    }
}

@Markup
@Composable
@Suppress("FunctionName")
fun <T> ReadOnlyProperty(
    property: Property<T>,
    styles:  PropertyStyles = PropertyStyles()
) {
    Div(attrs = {
        style {
            display(DisplayStyle.Flex)
            with(styles){propertyStyle()}
        }
    }){
        // Key
        Div(attrs = {
            style {
                alignContent(AlignContent.Start)
                with(styles){keyStyle()}
            }
        }) {
            Text(property.key)
        }
        // Value
        Div(attrs = {
            style {
                alignContent(AlignContent.Start)
                with(styles){valueStyle()}
            }
        }) {
            Text(property.format(property.value))
        }

    }
}

