package org.solyton.solawi.bid.module.tabs.style

import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexDirection
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.StyleScope
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.flexDirection
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.justifyContent
import org.solyton.solawi.bid.module.style.cursor.Cursor
import org.solyton.solawi.bid.module.style.cursor.cursor
import org.jetbrains.compose.web.css.paddingLeft
import org.jetbrains.compose.web.css.paddingRight
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.color
// import org.jetbrains.compose.web.css

data class TabStyles(
    val tabsWrapperStyles: StyleScope.()->Unit = {
        paddingLeft(20.px)
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
        width(100.percent)
    },
    val tabSelectionBarStyles: StyleScope.()->Unit = {
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Row)
        justifyContent(JustifyContent.FlexStart)
        alignItems(AlignItems.Center)
        width(100.percent)
    },
    val tabTriggerStyles: StyleScope.(selected: Boolean)->Unit = { selected ->
        paddingLeft(5.px)
        cursor(Cursor.Pointer)
        if(!selected) {
            color(Color.gray)
        }
    },
    val tabContentWrapperStyles: StyleScope.()->Unit = {

    },
    val tabContentStyles: StyleScope.(visible: Boolean)->Unit = {

    },
)
