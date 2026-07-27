package org.evoleq.compose.layout

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text


data class Property<T>(
    val key: String,
    val value: T,
    val action: (@Composable (T)->Unit)? = null,
    val format: (T)->String = {"$it"}
)

data class PropertiesStyles(
    val containerStyle: StyleScope.()->Unit = {
        width(100.percent)
    },
    val propertyStyles: PropertyStyles = PropertyStyles()
) {

    fun modifyContainerStyle(styles: StyleScope. ()->Unit): PropertiesStyles = copy(
        containerStyle = {
            containerStyle()
            styles()
        }
    )

    fun modifyPropertyStyles(styles: PropertyStyles. ()-> PropertyStyles): PropertiesStyles = copy(
        propertyStyles = propertyStyles.styles()
    )
}

data class PropertyStyles(
    val propertyStyle: StyleScope.()->Unit = {
        margin(10.px)
        width(100.percent)
    },
    val keyStyle: StyleScope.()->Unit = {
        width(20.percent)
        alignContent(AlignContent.Start)
    },
    val valueStyle: StyleScope.()->Unit = {
        width(40.percent)
        alignContent(AlignContent.Start)
    },
    val actionStyle: StyleScope.()->Unit = {
        width(40.percent)
        justifyContent(JustifyContent.FlexEnd)
       alignSelf(AlignSelf.FlexEnd)
    }
) {
    fun modifyPropertyStyle(styles: StyleScope. ()->Unit): PropertyStyles = copy(
        propertyStyle = {
            propertyStyle()
            styles()
        }
    )

    fun modifyValueStyle(styles: StyleScope. ()->Unit): PropertyStyles = copy(
        valueStyle = {
            valueStyle()
            styles()
        }
    )


    fun modifyKeyStyle(styles: StyleScope. ()->Unit): PropertyStyles = copy(
        keyStyle = {
            keyStyle()
            styles()
        }
    )

    fun modifyActionStyle(styles: StyleScope. ()->Unit): PropertyStyles = copy(
        actionStyle = {
            actionStyle()
            styles()
        }
    )
}

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
            ReadOnlyProperty(it, styles.propertyStyles, it.action)
        }
    }
}

@Markup
@Composable
@Suppress("FunctionName")
fun <T> ReadOnlyProperty(
    property: Property<T>,
    styles:  PropertyStyles = PropertyStyles(),
    action: (@Composable (T)->Unit)? = null,
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
        if(action != null) {
            Div(attrs = {
                style {
                    alignSelf(AlignSelf.FlexEnd)
                    with(styles){actionStyle()}
                }
            }) {
                action(property.value)
            }
        }
    }
}
