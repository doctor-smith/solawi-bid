package org.solyton.solawi.bid.module.list.style

import org.jetbrains.compose.web.css.*
import org.solyton.solawi.bid.module.style.listItemGap

val defaultListStyles: ListStyles by lazy { ListStyles() }

/**
 * Structure: nested flex-boxes
 * listWrapper
 * |- titleWrapper
 *    |- title
 * |- headerWrapper
 *    |- header
 * |- listItemWrapper
 *    |- dataWrapper
 *    |- actionsWrapper
 */
data class ListStyles (
    val listWrapper: StyleScope.()->Unit = {
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
        gap(listItemGap)
        width(100.percent)
        height(100.percent)
    },
    val titleWrapper: StyleScope.()->Unit = {
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Row)
        alignItems(AlignItems.Center)
        width(100.percent)
        gap(listItemGap)
    },
    val title: StyleScope.()-> Unit = {},
    val headerWrapper: StyleScope.()->Unit = {
        //justifyContent(JustifyContent.SpaceBetween)
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Row)
        alignItems(AlignItems.Center)
        width(100.percent)
    },
    val header: StyleScope.()->Unit = {
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Row)
        alignItems(AlignItems.FlexStart)
        width(80.percent)

        paddingLeft(20.px)
        paddingTop(10.px)
        paddingBottom(10.px)

    },
    val listItemWrapper: StyleScope.()->Unit = {
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Row)
        alignItems(AlignItems.Center)
        width(100.percent)
    },
    val dataWrapper: StyleScope.()-> Unit = {
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Row)
        width(80.percent)

        paddingLeft(20.px)
        paddingTop(10.px)
        paddingBottom(10.px)
    },
    val actionsWrapper: StyleScope.()->Unit = {
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Row)
        justifyContent(JustifyContent.End)
        width(20.percent)
        gap(2.px)
    }
)  {
    fun modify(
        wrapper: StyleScope.() -> Unit,
        headerWrapper: StyleScope.() -> Unit,
        header: StyleScope.() -> Unit,
        listItemWrapper: StyleScope.() -> Unit,
        dataWrapper: StyleScope.() -> Unit,
        actionsWrapper: StyleScope.() -> Unit
    ): ListStyles = copy(
        listWrapper = {this.wrapper(); wrapper()},
        titleWrapper = {this.titleWrapper(); titleWrapper()},
        title = {this.title(); title()},
        headerWrapper= {this.headerWrapper(); headerWrapper()},
        header = {this.header(); header()},
        listItemWrapper = {this.listItemWrapper(); listItemWrapper()},
        dataWrapper = {this.dataWrapper(); dataWrapper()},
        actionsWrapper = {this.actionsWrapper(); actionsWrapper()}
    )

    fun modifyListWrapper(newStyles: StyleScope.()->Unit): ListStyles = copy(
        listWrapper = {
            listWrapper()
            newStyles()
        }
    )

    fun modifyTitleWrapper(newStyles: StyleScope.()->Unit): ListStyles = copy(
        titleWrapper = {
            titleWrapper()
            newStyles()
        }
    )

    fun modifyTitle(newStyles: StyleScope.()->Unit): ListStyles = copy(
        title = {
            title()
            newStyles()
        }
    )

    fun modifyHeader(newStyles: StyleScope.()->Unit): ListStyles = copy(
        header = {
            header()
            newStyles()
        }
    )

    fun modifyHeaderWrapper(newStyles: StyleScope.()->Unit): ListStyles = copy(
        headerWrapper = {
            headerWrapper()
            newStyles()
        }
    )

    fun modifyListItemWrapper(newStyles: StyleScope.()->Unit): ListStyles = copy(
        listItemWrapper = {
            listItemWrapper()
            newStyles()
        }
    )

    fun modifyDataWrapper(newStyles: StyleScope.()->Unit): ListStyles = copy(
        dataWrapper = {
            dataWrapper()
            newStyles()
        }
    )

    fun modifyActionsWrapper(newStyles: StyleScope.()->Unit): ListStyles = copy(
        actionsWrapper = {
            actionsWrapper()
            newStyles()
        }
    )
}
