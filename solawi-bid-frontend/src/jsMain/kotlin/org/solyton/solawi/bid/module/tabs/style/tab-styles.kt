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
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.justifyContent
import org.solyton.solawi.bid.module.style.cursor.Cursor
import org.solyton.solawi.bid.module.style.cursor.cursor
import org.jetbrains.compose.web.css.paddingLeft
import org.jetbrains.compose.web.css.paddingRight
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.css.border
import org.jetbrains.compose.web.css.borderRadius
import org.jetbrains.compose.web.css.borderWidth
import org.jetbrains.compose.web.css.flexGrow
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.minHeight

// import org.jetbrains.compose.web.css

data class TabStyles(
    val tabsWrapperStyles: StyleScope.()->Unit = {
        paddingLeft(20.px)
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
        width(100.percent)
        minHeight(0.px)
        flexGrow(1)
        // height(100.percent)
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
        paddingRight(5.px)
        cursor(Cursor.Pointer)
        val color = when{
            selected -> Color.black
            else -> Color.gray
        }
        if(!selected) {
            color(color)
        }
        border{

            this.color = color
            style = LineStyle.Solid
        }
        borderWidth(1.px, 1.px,0.px, 1.px)
        borderRadius(5.px, 5.px, 0.px, 0.px)
    },
    val tabContentWrapperStyles: StyleScope.()->Unit = {
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
        minHeight(0.px)
        // height(100.percent)
        flexGrow(1)
        padding(5.px)
        border{
            color = Color.black
            style = LineStyle.Solid
            width = 1.px
        }
    },
    val tabContentStyles: StyleScope.(visible: Boolean)->Unit = {
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
        minHeight(0.px)
        // height(100.percent)
        flexGrow(1)
    },
    val tabTitleStyles: StyleScope.()->Unit = {

    },
    val tabParagraphStyles: StyleScope.()->Unit = {
    },
) {
    fun modifyTabContentStyles(): TabStyles = this
}
