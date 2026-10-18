package org.solyton.solawi.bid.module.control.checkbox

import androidx.compose.runtime.*
import org.evoleq.compose.Markup
import org.evoleq.compose.attribute.dataId
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
    dataId: String? = null,
    onClick: (Boolean) -> Unit = {}
) = CheckBox(checked.emit(), styles, dataId, onClick)


@Markup
@Composable
@Suppress("FunctionName")
fun CheckBox(
    checked: Boolean,
    styles: CheckBoxStyles = CheckBoxStyles(),
    dataId: String? = null,
    onClick: (Boolean) -> Unit = {}
){
    var checkedState by remember { mutableStateOf(checked) }
    CheckboxInput(checkedState,{
        if(dataId != null) dataId(dataId)
        style { with(styles){ style() } }
        onClick {
            val newCheckedState = !checkedState
            checkedState = newCheckedState
            onClick(newCheckedState)
        }
    })
}
