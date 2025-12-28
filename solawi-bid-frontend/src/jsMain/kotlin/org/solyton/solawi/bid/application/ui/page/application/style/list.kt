package org.solyton.solawi.bid.application.ui.page.application.style

import org.jetbrains.compose.web.css.AlignSelf
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.StyleScope
import org.jetbrains.compose.web.css.alignSelf
import org.jetbrains.compose.web.css.backgroundColor
import org.jetbrains.compose.web.css.paddingRight
import org.jetbrains.compose.web.css.paddingTop
import org.jetbrains.compose.web.css.px
import org.solyton.solawi.bid.module.list.style.defaultListStyles

/**
 * This is a custom style-applier function for styling a list item wrapper based on its index.
 * The function applies different styles to the list item wrapper depending on whether the index is even or odd.
 *
 * @param it The index of the list item, used to determine which style to apply.
 */
val listItemWrapperStyle: StyleScope.(Int) ->Unit = {when{
    it % 2 == 0 -> darkListItemStyle(this)
    else -> liteListItemStyle(this)
}}

/**
 * A styling function for applying a predefined light style to a list item.
 *
 * This style utilizes the `listItemWrapper` function from `defaultListStyles` to apply
 * consistent appearance to list items intended for a "light" theme.
 *
 * Use this function when styling list items in a light-themed context.
 */
val liteListItemStyle: StyleScope.() ->Unit = {
    defaultListStyles.listItemWrapper(this)
}

/**
 * A styling function for applying a predefined dark style to a list item.
 *
 * This style utilizes the `listItemWrapper` function from `defaultListStyles` to ensure
 * a consistent base appearance for list items in a "dark" theme context. It then applies
 * a specific background color, `Color.ghostwhite`, to differentiate the dark list items.
 *
 * Use this function when styling list items in a dark-themed context.
 */
val darkListItemStyle: StyleScope.() ->Unit = {
    defaultListStyles.listItemWrapper(this)
    backgroundColor(Color.ghostwhite)
}

/**
 * A style configuration for the actions wrapper within the UI component. This defines
 * specific styling rules that determine the appearance and alignment of the actions section.
 *
 * The styling includes:
 * - Applying default styles from `defaultListStyles.actionsWrapper`.
 * - Aligning the actions wrapper to the start of the container using `alignSelf(AlignSelf.FlexStart)`.
 * - Adding padding of 5 pixels to the top (`paddingTop`) and right (`paddingRight`) of the wrapper.
 *
 * This style is applied in a `StyleScope` to ensure modularity and reusability across the application.
 */
val actionsWrapperStyle: StyleScope.() ->Unit = {

    defaultListStyles.actionsWrapper(this)
    alignSelf(AlignSelf.FlexStart)
    paddingTop(5.px)
    paddingRight(5.px)
}

