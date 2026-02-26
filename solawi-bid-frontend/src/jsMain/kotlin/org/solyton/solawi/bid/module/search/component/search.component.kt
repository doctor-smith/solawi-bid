package org.solyton.solawi.bid.module.search.component

import androidx.compose.runtime.*
import org.evoleq.compose.Markup
import org.jetbrains.compose.web.ExperimentalComposeWebApi
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

data class SearchInputStyles(
    val containerStyle: StyleScope.()->Unit = {},
    val inputStyle: StyleScope.()->Unit = {},
)

@OptIn(ExperimentalComposeWebApi::class)
@Markup
@Composable
@Suppress("FunctionName")
fun SearchInput(
    initialText: String,
    styles: SearchInputStyles,
    searchOnEnter: Boolean = true,
    onInput: (String)->Unit
) {
    var searchText by remember { mutableStateOf(initialText) }

    Div({

        style {
            position(Position.Relative)
            width(250.px)
            with(styles){
                containerStyle()
            }
        }
    }) {

        Input(type = InputType.Text) {
            style {
                paddingLeft(30.px)
                width(100.percent)
                with(styles){
                    inputStyle()
                }
            }
            value(searchText)
            onInput {
                searchText = it.value
                if(searchOnEnter) return@onInput
                onInput(searchText)
            }
            onKeyDown { event ->
                val isSearch = event.key == "Enter"
                val isClear = event.key == "Escape" ||
                    ((event.ctrlKey || event.metaKey) && event.key.lowercase() == "c")

                when{
                    isSearch -> { onInput(searchText) }
                    isClear -> { searchText = ""; onInput("") }
                }
            }
        }

        Span({
            style {
                position(Position.Absolute)
                left(8.px)
                top(50.percent)
                this.transform { translateY((-50).percent) }
                cursor("pointer")
            }

            onClick { onInput(searchText) }
        }) {
            Text("🔍")
        }

        // ❌ RIGHT CLEAR ICON
        if (searchText.isNotBlank()) {
            Span({
                style {
                    position(Position.Absolute)
                    right(8.px)
                    top(50.percent)
                    transform { translateY((-50).percent) }
                    cursor("pointer")
                }
                onClick {
                    searchText = ""
                    onInput("") // trigger clear search
                }
            }) {
                Text("✕")
            }
        }
    }
}
