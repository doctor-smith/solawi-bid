package org.solyton.solawi.bid.module.list.component

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.jetbrains.compose.web.css.StyleScope
import org.jetbrains.compose.web.dom.Div

sealed class Symbol<out T>(open val value: T) where T: Comparable<@UnsafeVariance T> {
    data object LeftParenthesis: Symbol<String>("(")
    data object RightParenthesis: Symbol<String>(")")
    data object And: Symbol<String>("and")
    data object Or: Symbol<String>("or")
    data object Not: Symbol<String>("not")
    data class Type<T : Comparable<T>>(override val value: T): Symbol<T>(value)
}

data class OptionFilterStyles(
    val filterWrapperStyle: StyleScope.()->Unit = {},
    val symbolStyle: StyleScope.()->Unit = {}
)

/*
@Markup
@Composable
@Suppress("FunctionName")
fun <T : Comparable<T>> OptionsFilter(
    title: String,
    options: Map<String, T>,
    selected: List<Symbol<T>>,
    refreshOnInput: Boolean = false,
    styles: OptionFilterStyles = OptionFilterStyles(),
    handleInput: (List<Symbol<T>>) -> Unit
) {

}

@Markup
@Composable
@Suppress("FunctionName")
fun Symbol(symbol: Symbol<*>, closable: Boolean = false, styles: StyleScope.()->Unit = {}) {
    Div({}) {

    }
}

*/
