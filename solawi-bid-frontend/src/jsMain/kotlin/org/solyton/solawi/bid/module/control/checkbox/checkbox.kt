package org.solyton.solawi.bid.module.control.checkbox

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.evoleq.compose.Markup
import org.evoleq.math.Source
import org.evoleq.math.emit
import org.jetbrains.compose.web.css.StyleScope
import org.jetbrains.compose.web.dom.CheckboxInput

data class CheckBoxStyles(
    val style: StyleScope.()->Unit = {}
)

@Markup
@Composable
@Suppress("FunctionName")
fun CheckBox(
    checked: Source<Boolean>,
    styles: CheckBoxStyles = CheckBoxStyles(),
    onClick: (Boolean) -> Unit = {}
) = CheckBox(checked.emit(), styles, onClick)


@Markup
@Composable
@Suppress("FunctionName")
fun CheckBox(
    checked: Boolean,
    styles: CheckBoxStyles = CheckBoxStyles(),
    onClick: (Boolean) -> Unit = {}
){
    var checkedState by remember { mutableStateOf(checked) }
    CheckboxInput(checkedState,{
        style { with(styles){ style() } }
        onClick {
            val newCheckedState = !checkedState
            checkedState = newCheckedState
            onClick(newCheckedState)
        }
    })
}
