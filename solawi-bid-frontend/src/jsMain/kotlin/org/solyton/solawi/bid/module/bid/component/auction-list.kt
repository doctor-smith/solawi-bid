package org.solyton.solawi.bid.module.bid.component

import androidx.compose.runtime.Composable
import kotlinx.coroutines.DelicateCoroutinesApi
import org.evoleq.compose.Markup
import org.evoleq.compose.date.format
import org.evoleq.compose.modal.Modals
import org.evoleq.compose.routing.navigate
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
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.device.DeviceType
import org.solyton.solawi.bid.application.permission.Right
import org.solyton.solawi.bid.application.ui.page.auction.action.configureAuction
import org.solyton.solawi.bid.module.bid.action.deleteAuctionAction
import org.solyton.solawi.bid.module.bid.component.form.showUpdateAuctionModal
import org.solyton.solawi.bid.module.bid.data.Auction
import org.solyton.solawi.bid.module.bid.data.date
import org.solyton.solawi.bid.module.bid.data.name
import org.solyton.solawi.bid.module.bid.data.reader.auctionAccepted
import org.solyton.solawi.bid.module.control.button.StdButton
import org.solyton.solawi.bid.module.i18n.data.I18N
import org.solyton.solawi.bid.module.i18n.data.language
import org.solyton.solawi.bid.module.user.data.User
import org.solyton.solawi.bid.module.user.isNotGranted
import org.solyton.solawi.bid.application.data.auctions as auctionLens

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
    dispatch: (Action<Application, *, *>) -> Unit
) = Div(
    attrs = {style{styles.wrapper(this)}}
) {
    with(auctions.read()) {
        forEach{ auction ->
            AuctionListItem(
                auctions * FirstBy<Auction> { it.auctionId == auction.auctionId},
                user,
                i18n,
                modals,
                device,
                styles,
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
    dispatchDelete: ()->Unit,
    dispatchConfiguration: ()->Unit
) = Div(attrs = {
    style { styles.item(this) }
}) {
    Div (attrs = {style {
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Row)
        width(80.percent)
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

        StdButton(
            buttons * subComp("details") * title,
            device,
            false,
        ) {
            navigate("/solyton/auctions/${auction.read().auctionId}")
        }

        // Edit
        StdButton(
            buttons * subComp("edit") * title,
            device,
            (auction * auctionAccepted).emit() || user.emit().isNotGranted(Right.Auction.manage),
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
        StdButton(
            buttons * subComp("delete") * title,
            device,
            (auction * auctionAccepted).emit() || user.emit().isNotGranted(Right.Auction.manage)
        ) {
            dispatchDelete()
        }
    }
}

data class AuctionListStyles (
    val wrapper: StyleScope.()->Unit = {
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
        width(100.percent)
        height(100.percent)
        paddingTop(10.px)
        margin(5.px)

    },
    val item: StyleScope.()->Unit = {
        paddingTop(10.px)
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Row)
        width(100.percent)
    }
)
