package org.evoleq.compose.symbols

import androidx.compose.runtime.Composable
import org.evoleq.symbols.*
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.StyleScope
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.dom.Li
import org.jetbrains.compose.web.dom.Span

@Composable
fun WarnLi(
    styles: LiStyles = LiStyles(),
    content: @Composable () -> Unit
) = CustomLi("\"$WARN  \"", styles, content)

@Composable
fun CheckLi(
    styles: LiStyles = LiStyles(),
    content: @Composable () -> Unit
) = CustomLi("\"$CHECK  \"", styles, content)

@Composable
fun BucketLi(
    styles: LiStyles = LiStyles(),
    content: @Composable () -> Unit
) = CustomLi("\"$BUCKET  \"", styles, content)

@Composable
fun ForbiddenLi(
    styles: LiStyles = LiStyles(),
    content: @Composable () -> Unit
) = CustomLi("\"$FORBIDDEN  \"", styles, content)

@Composable
fun InfoLi(
    styles: LiStyles = LiStyles().modifyTypeStyles {
        color(Color.blue)
    },
    content: @Composable () -> Unit
) = CustomLi("\"$INFO  \"", styles, content)

data class LiStyles(
    val typeStyles: StyleScope.()->Unit= {
        color(Color.orangered)
    },
    val contentStyles: StyleScope.()->Unit = {}
) {
    fun modifyTypeStyles(styles: StyleScope.()->Unit): LiStyles = copy(
        typeStyles = {
            typeStyles()
            styles()
        }
    )

    fun modifyContentStyles(styles: StyleScope.()->Unit): LiStyles = copy(
        typeStyles = {
            contentStyles()
            styles()
        }
    )
}

@Composable
fun CustomLi(
    listStyleType: String,
    styles: LiStyles = LiStyles(),
    content: @Composable () -> Unit) = Li(attrs = {
    style {
        property("list-style-type", listStyleType )
        with(styles){
            typeStyles()
        }
    }
}) {
    Span(attrs = {
        style {
            property("color", "initial")
            with(styles){
                contentStyles()
            }
        }
    }) { content() }
}
