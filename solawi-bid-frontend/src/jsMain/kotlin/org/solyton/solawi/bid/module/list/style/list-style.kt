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
@Suppress("TooManyFunctions")
data class ListStyles (
    val listWrapper: StyleScope.()->Unit = {
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
        gap(listItemGap)
        width(100.percent)
        height(100.percent)
        minHeight(0.px)
        flexGrow(1)
    },
    val titleWrapper: StyleScope.()->Unit = {
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Row)
        alignItems(AlignItems.Center)
        width(100.percent)
        gap(listItemGap)
    },
    val title: StyleScope.()-> Unit = {},
    val filterWrapper: StyleScope.()->Unit = {
        width(100.percent)
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Row)
        alignItems(AlignItems.Center)
    },
    val filter: StyleScope.()->Unit = {

    },
    val overallActionsWrapper: StyleScope.()->Unit = {
        display(DisplayStyle.Flex)
        width(100.percent)
    },
    val overallActions: StyleScope.()->Unit = {},
    val headerWrapper: StyleScope.()->Unit = {
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Row)
        alignItems(AlignItems.Center)
        width(100.percent)
        backgroundColor(Color("#f8fafc"))
        property("border-bottom", "1px solid #e2e8f0")
        borderRadius(6.px)
    },
    val header: StyleScope.()->Unit = {
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Row)
        alignItems(AlignItems.FlexStart)
        width(80.percent)
        paddingLeft(16.px)
        paddingRight(16.px)
        paddingTop(10.px)
        paddingBottom(10.px)
        fontSize(13.px)
        color(Color("#475569"))
        fontWeight("600")
    },
    val listItemWrapper: StyleScope.()->Unit = {
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Row)
        alignItems(AlignItems.Center)
        width(100.percent)
        borderRadius(6.px)
        property("border", "1px solid #f1f5f9")
    },
    val dataWrapper: StyleScope.()-> Unit = {
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Row)
        width(80.percent)
        paddingLeft(16.px)
        paddingRight(16.px)
        paddingTop(10.px)
        paddingBottom(10.px)
        fontSize(14.px)
        color(Color("#1e293b"))
    },
    val actionsWrapper: StyleScope.()->Unit = {
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Row)
        justifyContent(JustifyContent.End)
        alignItems(AlignItems.Center)
        width(20.percent)
        paddingRight(12.px)
        gap(4.px)
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

    fun modifyFilterWrapper(newStyles: StyleScope.()->Unit): ListStyles = copy(
        filterWrapper = {
            filterWrapper()
            newStyles()
        }
    )

    fun modifyFilter(newStyles: StyleScope.()->Unit): ListStyles = copy(
        filter = {
            filter()
            newStyles()
        }
    )

    fun modifyOverallActionsWrapper(newStyles: StyleScope.()->Unit): ListStyles = copy(
        overallActionsWrapper = {
            overallActionsWrapper()
            newStyles()
        }
    )

    fun modifyOverallActions(newStyles: StyleScope.()->Unit): ListStyles = copy(
        overallActions = {
            overallActions()
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

    fun modifyListItemWrapperIndexed(newStylesIndexed: (index: Int) -> StyleScope.()->Unit): (Int)->ListStyles = { index ->
        val newStyles = newStylesIndexed(index)
        copy(
            listItemWrapper = {
                listItemWrapper()
                newStyles()
            }
        )
    }


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
