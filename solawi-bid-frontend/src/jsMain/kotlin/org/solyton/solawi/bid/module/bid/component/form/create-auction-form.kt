package org.solyton.solawi.bid.module.bid.component.form

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.selects.select
import org.evoleq.compose.Markup
import org.evoleq.compose.attribute.dataId
import org.evoleq.compose.date.format
import org.evoleq.compose.date.parse
import org.evoleq.compose.label.Label
import org.evoleq.compose.modal.Modal
import org.evoleq.compose.modal.ModalData
import org.evoleq.compose.modal.ModalType
import org.evoleq.compose.modal.Modals
import org.evoleq.compose.style.data.device.DeviceType
import org.evoleq.kotlinx.date.toDateTime
import org.evoleq.language.Lang
import org.evoleq.language.Locale
import org.evoleq.language.component
import org.evoleq.language.get
import org.evoleq.math.Reader
import org.evoleq.math.Source
import org.evoleq.math.emit
import org.evoleq.math.map
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.nextId
import org.evoleq.optics.storage.put
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.ElementScope
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.TextInput
import org.solyton.solawi.bid.module.bid.component.dropdown.OrganizationsDropdown
import org.solyton.solawi.bid.module.bid.component.styles.auctionModalStyles
import org.solyton.solawi.bid.module.bid.data.auction.Auction
import org.solyton.solawi.bid.module.bid.data.auction.contextId
import org.solyton.solawi.bid.module.bid.data.auction.date
import org.solyton.solawi.bid.module.bid.data.auction.name
import org.solyton.solawi.bid.module.style.form.*
import org.solyton.solawi.bid.module.user.data.organization.Organization
import org.w3c.dom.HTMLElement
import kotlin.uuid.Uuid

const val DEFAULT_AUCTION_ID = "DEFAULT_AUCTION_ID"

@Markup
@Suppress("FunctionName")
fun AuctionModal(
    id: Int,
    texts: Lang.Block,
    modals: Storage<Modals<Int>>,
    auction: Storage<Auction>,
    organizations: Source<List<Organization>>,
    device: Source<DeviceType>,
    cancel: ()->Unit,
    update: ()->Unit
): @Composable ElementScope<HTMLElement>.()->Unit = Modal(
    id,
    modals,
    device,
    onOk = {
        update()
    },
    onCancel = {
        cancel()
    },
    texts = texts,
    styles = auctionModalStyles(device),
) {

    // input texts
    val inputs: Lang.Block = texts.component("inputs")

    Div(attrs = {style { formDesktopStyle() }}) {

        Div(attrs = {style { fieldDesktopStyle() }}) {
            Label(inputs["title"], id = "name" , labelStyle = formLabelDesktopStyle)
            TextInput((auction * name).read()) {
                id("name")
                dataId("create-auction.form.input.name")
                style { textInputDesktopStyle() }
                onInput { (auction * name).write(it.value) }
            }
        }
        Div(attrs = {style { fieldDesktopStyle() }}) {
            // State
            val initDate = (auction * date).read().format(Locale.Iso)
            var dateString by remember{ mutableStateOf( initDate ) }

            Label(inputs["date"], id = "date" , labelStyle = formLabelDesktopStyle)
            Input(InputType.Date) {
                id("date")
                dataId("create-auction.form.input.date")
                value(dateString)
                style { dateInputDesktopStyle() }
                onInput {
                    dateString = it.value
                    (auction * date).write(it.value.parse(Locale.Iso).toDateTime())
                }
            }
        }
        Div(attrs = {style { fieldDesktopStyle() }}) {
            Label(inputs["hostOrganization"], id = "host-organization", labelStyle = formLabelDesktopStyle)
            OrganizationsDropdown(
                selected = with((auction * contextId).read()) contextId@{
                    organizations map { orgs -> orgs.firstOrNull { it.contextId == this@contextId }}},
                organizations = organizations,
                // todo:dev SMA-403 POC
                isSelectable = { organizations.emit().any { o -> o.organizationId == organizationId } },
                scope = CoroutineScope(Job())
            ) {
                // add organization context to auction
                organization -> (auction * contextId).write(organization.contextId)
            }
        }
    }
}

@Markup
fun Storage<Modals<Int>>.showAuctionModal(
    auction: Storage<Auction>,
    organizations: Source<List<Organization>>,
    texts: Lang.Block,
    device: Source<DeviceType>,
    cancel: ()->Unit,
    update: ()->Unit
) = with(nextId()) {
    put(this to ModalData(
        ModalType.Dialog,
        AuctionModal(
            this,
            texts,
            this@showAuctionModal,
            auction,
            organizations,
            device,
            cancel,
            update
        )
    ))
}
