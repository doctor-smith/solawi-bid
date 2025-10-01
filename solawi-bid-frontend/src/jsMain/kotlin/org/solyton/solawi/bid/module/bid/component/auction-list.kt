package org.solyton.solawi.bid.module.bid.component

import androidx.compose.runtime.Composable
import kotlinx.coroutines.DelicateCoroutinesApi
import org.evoleq.compose.Markup
import org.evoleq.compose.date.format
import org.evoleq.compose.modal.Modals
import org.evoleq.compose.routing.navigate
import org.evoleq.compose.style.data.device.DeviceType
import org.evoleq.language.*
import org.evoleq.math.Source
import org.evoleq.math.emit
import org.evoleq.math.times
import org.evoleq.optics.lens.FirstBy
import org.evoleq.optics.lens.times
import org.evoleq.optics.storage.Action
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.module.bid.action.configureAuction
import org.solyton.solawi.bid.module.bid.action.deleteAuctionAction
import org.solyton.solawi.bid.module.bid.component.form.showUpdateAuctionModal
import org.solyton.solawi.bid.module.bid.component.styles.actionButtonBgColor
import org.solyton.solawi.bid.module.bid.component.styles.actionButtonColor
import org.solyton.solawi.bid.module.bid.data.BidApplication
import org.solyton.solawi.bid.module.bid.data.auction.Auction
import org.solyton.solawi.bid.module.bid.data.auction.date
import org.solyton.solawi.bid.module.bid.data.auction.name
import org.solyton.solawi.bid.module.bid.data.biduser.User
import org.solyton.solawi.bid.module.bid.data.reader.auctionAccepted
import org.solyton.solawi.bid.module.bid.permission.BidRight
import org.solyton.solawi.bid.module.bid.service.isNotGranted
import org.solyton.solawi.bid.module.control.button.DetailsButton
import org.solyton.solawi.bid.module.control.button.EditButton
import org.solyton.solawi.bid.module.control.button.TrashCanButton
import org.solyton.solawi.bid.module.i18n.data.I18N
import org.solyton.solawi.bid.module.i18n.data.language
import org.solyton.solawi.bid.module.separator.LineSeparatorStyles
import org.solyton.solawi.bid.module.style.listEven
import org.solyton.solawi.bid.module.style.listItemGap
import org.solyton.solawi.bid.module.style.listOdd
import org.solyton.solawi.bid.module.style.verticalAccentBar
import org.solyton.solawi.bid.module.bid.data.auctions as auctionLens

@Markup
@Composable
@Suppress("FunctionName")
fun AuctionList(
    auctions: Storage<List<Auction>>,
    user: Source<User>,
    i18n: Storage<I18N>,
    modals: Storage<Modals<Int>>,
    device: Source<DeviceType>,
    styles: AuctionListStyles = AuctionListStyles(),
    dispatch: (Action<BidApplication, *, *>) -> Unit
) = Div(
    attrs = {style{styles.wrapper(this)}}
) {
    AuctionListHeader(styles)
    // LineSeparator(headerSeparatorStyles)
    with(auctions.read()) {
        forEachIndexed{ index, auction ->
            AuctionListItem(
                auctions * FirstBy<Auction> { it.auctionId == auction.auctionId},
                user,
                i18n,
                modals,
                device,
                styles,
                index,
                dispatchDelete = { dispatch(deleteAuctionAction(auction)) },
                dispatchConfiguration = {dispatch(configureAuction(auctionLens * FirstBy<Auction> { it.auctionId == auction.auctionId}))    }
            )
        }
    }
}

@OptIn(DelicateCoroutinesApi::class)
@Markup
@Composable
@Suppress("FunctionName") // actions: (Auction)->Actions = auctionListItemActions
fun AuctionListItem(
    auction: Storage<Auction>,
    user: Source<User>,
    i18n: Storage<I18N>,
    modals: Storage<Modals<Int>>,
    device: Source<DeviceType>,
    styles: AuctionListStyles = AuctionListStyles(),
    index: Int,
    dispatchDelete: ()->Unit,
    dispatchConfiguration: ()->Unit
) = Div(attrs = {
    style {
        styles.item(this)
        backgroundColor(when{
            index % 2 == 0 -> listEven
            else -> listOdd
        })

        border {
            style(LineStyle.Solid)
            color(Color.ghostwhite)
        }
        borderWidth(1.px, 1.px, 1.px, 1.px)
    }
}) {
    Div (attrs = {style {
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Row)
        width(80.percent)
        marginTop(10.px)
        marginBottom(10.px)
        marginLeft(20.px)
    }}){
        // date
        Div(
            attrs = { style { width(20.percent) } }
        ) {
            val date = (auction * date).read()
            Text(date.format(Locale.De))
        }
        // name
        Div(attrs = { style { width(80.percent) } }) {
            val name = (auction * name).read()
            Text(name)
        }
    }
    Div(attrs = {style {
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Row)
        justifyContent(JustifyContent.End)
        width(20.percent)
        marginRight(10.px)
        gap(2.px)
    }}) {
        val buttons = (i18n *
            language *
            subComp("solyton") *
            subComp("auction") *
            subComp("auctionsPage") *
            subComp("auctionList") *
            subComp("items") *
            subComp("buttons")
        )

        DetailsButton(
            actionButtonColor,
            actionButtonBgColor,
            buttons * subComp("details") * tooltip,
            device,
            false,
        ) {
            navigate("/app/auctions/${auction.read().auctionId}")
        }

        // Edit
        EditButton(
            actionButtonColor,
            actionButtonBgColor,
            buttons * subComp("edit") * tooltip,
            device,
            (auction * auctionAccepted).emit() || user.emit().isNotGranted(BidRight.Auction.manage),
        ) {
            // open edit dialog
            (modals).showUpdateAuctionModal(
                auction =  auction,
                texts = ((i18n * language).read() as Lang.Block).component("solyton.auction.updateDialog"),
                device = device,
                cancel = {}
            ) {
                dispatchConfiguration()
            }
        }

        // Delete
        TrashCanButton(
            actionButtonColor,
            actionButtonBgColor,
            buttons * subComp("delete") * tooltip,
            device,
            (auction * auctionAccepted).emit() || user.emit().isNotGranted(BidRight.Auction.manage)
        ) {
            dispatchDelete()
        }
    }
}

@Markup
@Composable
@Suppress("FunctionName")
fun AuctionListHeader(
    styles: AuctionListStyles = AuctionListStyles()
) {
        Div(attrs = {
            style {
                styles.item(this)
                justifyContent(JustifyContent.Start)
            }
        }) {
            Div({
                style {
                    display(DisplayStyle.Flex)
                    flexDirection(FlexDirection.Row)
                    width(80.percent)
                    marginLeft(20.px)
                    marginTop(10.px)
                    marginBottom(10.px)
                }
            }) {
                // todo:i18n - AuctionListHeader
                Div({ style { width(20.percent); fontWeight("bold") } }) { Text("Datum") }
                // todo:i18n - AuctionListHeader
                Div({ style { width(20.percent); fontWeight("bold") } }) { Text("Name") }
            }
        }
}

data class AuctionListStyles (
    val wrapper: StyleScope.()->Unit = {
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
        gap(listItemGap)
        width(100.percent)
        height(100.percent)
    },
    val item: StyleScope.()->Unit = {
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Row)
        alignItems(AlignItems.Center)
        width(100.percent)
    }
)

@Suppress("UNUSED_VARIABLE")
val headerSeparatorStyles = LineSeparatorStyles().copy (
    separatorStyles = {
        width(100.percent)
        height(2.px)
        backgroundColor(verticalAccentBar)
    }
)
