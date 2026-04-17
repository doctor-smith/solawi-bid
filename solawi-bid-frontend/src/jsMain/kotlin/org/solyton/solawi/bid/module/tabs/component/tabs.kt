package org.solyton.solawi.bid.module.tabs.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import org.evoleq.compose.Markup
import org.evoleq.compose.conditional.When
import org.jetbrains.compose.web.css.StyleScope
import org.jetbrains.compose.web.dom.Div
import org.solyton.solawi.bid.module.shares.data.types.key

@Markup
@Composable
@Suppress("FunctionName")
fun TabsWrapper(styles:  StyleScope.()->Unit, content: @Composable ()->Unit) {
    Div({style{styles()}}) {content()}
}

@Markup
@Composable
@Suppress("FunctionName")
fun TabSelectionBar(styles: StyleScope.()->Unit, content: @Composable ()->Unit) {
    Div({style{styles()}}) {
        content()
    }
}

@Markup
@Composable
@Suppress("FunctionName")
fun TabTrigger(styles: StyleScope.(Boolean)->Unit,id: Int, currentTab: Int, trigger: ()->Unit,  content: @Composable ()->Unit) {
    Div({
        style{ styles(id == currentTab) }
        onClick {
            it.preventDefault()
            trigger()
        }
    } ) {
        content()
    }
}

@Markup
@Composable
@Suppress("FunctionName")
fun TabContentWrapper(styles: StyleScope.()->Unit, content: @Composable ()->Unit) {
    Div({style{styles()}}) {content() }
}

@Markup
@Composable
@Suppress("FunctionName")
fun TabContent(styles: StyleScope.(Boolean)->Unit, id: Int, currentTab: Int, content: @Composable ()->Unit) {
    key(id, currentTab){ When( condition = id == currentTab){
        Div({style{styles(currentTab == id)}}) {content() }
    } }
}
