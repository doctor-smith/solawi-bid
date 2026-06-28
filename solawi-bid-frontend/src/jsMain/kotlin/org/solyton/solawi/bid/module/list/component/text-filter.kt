package org.solyton.solawi.bid.module.list.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.evoleq.compose.Markup
import org.evoleq.compose.layout.Vertical
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.StyleScope
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.fontSize
import org.jetbrains.compose.web.css.marginBottom
import org.jetbrains.compose.web.css.paddingLeft
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.rgb
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.TextInput


data class TextFilterStyles(
    val wrapperStyle: StyleScope.()->Unit = {
        width(100.percent)
    },
    val titleStyle: StyleScope.()->Unit = {
        fontSize(12.px)
        color(rgb(120, 120, 120))
        paddingLeft(5.px)
        marginBottom(4.px)
        display(DisplayStyle.Block)
    }
)

@Markup
@Composable
@Suppress("FunctionName")
fun TextFilter(
    title: String,
    state: String = "",
    refreshOnInput: Boolean = false,
    ignoreCase: Boolean = false,
    styles: TextFilterStyles = TextFilterStyles(),
    handleInput: (String, Boolean) -> Unit
) {
    var textState by remember { mutableStateOf(state) }
    Vertical(style = styles.wrapperStyle) {
        Span({ style {
            with(styles){titleStyle()}
        }}){
            Text(title)
        }
        TextInput(textState) {
            onInput {
                textState = it.value
                if (refreshOnInput) handleInput(it.value, ignoreCase)
            }
            onKeyDown {
                if (it.key == "Enter") handleInput(textState, ignoreCase)
            }
        }
    }
}

fun String.toFilter(ignoreCase: Boolean = false): (String) -> Boolean =
    FilterParser(this, ignoreCase).parse()

fun ((String) -> Boolean).applyTo(input:String) = this(input)

class FilterParser(private val input: String, private val ignoreCase: Boolean = false) {

    private var pos = 0

    fun parse(): (String) -> Boolean {
        val expr = parseOr()
        skipWhitespace()

        if (pos < input.length) {
            error("Unexpected token at $pos")
        }

        return expr
    }

    private fun parseOr(): (String) -> Boolean {
        var left = parseAnd()

        while (true) {
            skipWhitespace()

            if (match("||")) {
                val right = parseAnd()
                val prev = left
                left = { x -> prev(x) || right(x) }
            } else {
                break
            }
        }

        return left
    }

    private fun parseAnd(): (String) -> Boolean {
        var left = parseUnary()

        while (true) {
            skipWhitespace()

            if (match("&&")) {
                val right = parseUnary()
                val prev = left
                left = { x -> prev(x) && right(x) }
            } else {
                break
            }
        }

        return left
    }

    private fun parseUnary(): (String) -> Boolean {
        skipWhitespace()

        if (match("!")) {
            val inner = parseUnary()
            return { x -> !inner(x) }
        }

        return parsePrimary()
    }

    private fun parsePrimary(): (String) -> Boolean {
        skipWhitespace()

        if (match("(")) {
            val expr = parseOr()

            skipWhitespace()

            if (!match(")")) {
                error("Expected ')'")
            }

            return expr
        }

        return parseIdentifier()
    }

    private fun parseIdentifier(): (String) -> Boolean {
        skipWhitespace()

        val start = pos

        while (pos < input.length &&
            (input[pos].isLetterOrDigit() || input[pos] == '_')
        ) {
            pos++
        }

        if (start == pos) {
            error("Expected identifier at $pos")
        }

        val token = input.substring(start, pos)

        return { x -> x.contains(token, ignoreCase) }
    }

    private fun match(s: String): Boolean {
        skipWhitespace()

        if (input.startsWith(s, pos)) {
            pos += s.length
            return true
        }

        return false
    }

    private fun skipWhitespace() {
        while (pos < input.length && input[pos].isWhitespace()) {
            pos++
        }
    }
}
