package org.solyton.solawi.bid.module.shares.component.modal

import androidx.compose.runtime.*
import org.evoleq.compose.Markup
import org.evoleq.compose.conditional.When
import org.evoleq.compose.form.field.Field
import org.evoleq.compose.form.label.Label
import org.evoleq.compose.layout.Horizontal
import org.evoleq.compose.modal.Modal
import org.evoleq.compose.modal.ModalData
import org.evoleq.compose.modal.ModalType
import org.evoleq.compose.modal.Modals
import org.evoleq.compose.style.data.device.DeviceType
import org.evoleq.device.data.mediaType
import org.evoleq.language.Lang
import org.evoleq.language.texts
import org.evoleq.math.Source
import org.evoleq.math.emit
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.nextId
import org.evoleq.optics.storage.put
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.attributes.required
import org.jetbrains.compose.web.css.gap
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.ElementScope
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.TextInput
import org.solyton.solawi.bid.module.banking.data.fiscalyear.FiscalYear
import org.solyton.solawi.bid.module.banking.data.fiscalyear.format
import org.solyton.solawi.bid.module.bid.component.styles.auctionModalStyles
import org.solyton.solawi.bid.module.bid.data.deviceData
import org.solyton.solawi.bid.module.control.checkbox.CheckBox
import org.solyton.solawi.bid.module.control.dropdown.Dropdown
import org.solyton.solawi.bid.module.distribution.data.distributionpoint.DistributionPoint
import org.solyton.solawi.bid.module.navbar.component.SimpleUpDown
import org.solyton.solawi.bid.module.shares.data.internal.ChangeReason
import org.solyton.solawi.bid.module.shares.data.api.PricingType
import org.solyton.solawi.bid.module.shares.data.internal.ChangedBy
import org.solyton.solawi.bid.module.shares.data.internal.ShareStatus
import org.solyton.solawi.bid.module.shares.data.internal.shareStatusTransitionsWithPermissions
import org.solyton.solawi.bid.module.shares.data.management.ShareManagement
import org.solyton.solawi.bid.module.shares.data.management.deviceData
import org.solyton.solawi.bid.module.shares.data.offers.ShareOffer
import org.solyton.solawi.bid.module.shares.data.subscriptions.ShareSubscription
import org.solyton.solawi.bid.module.shares.data.types.ShareType
import org.solyton.solawi.bid.module.style.form.fieldDesktopStyle
import org.solyton.solawi.bid.module.style.form.formLabelDesktopStyle
import org.solyton.solawi.bid.module.style.form.textInputDesktopStyle
import org.solyton.solawi.bid.module.style.wrap.Wrap
import org.solyton.solawi.bid.module.values.ModifierId
import org.w3c.dom.HTMLElement

sealed class BulkEditShareSubscriptionChanges {
    data object None: BulkEditShareSubscriptionChanges()
    data class Status(
        val status: ShareStatus,
        val reason: ChangeReason,
        val changedBy: ChangedBy,
        val modifier: ModifierId,
        val comment: String? = null
    ): BulkEditShareSubscriptionChanges()
    data class AddShareOffer(
        val shareOfferId: String,
        val fiscalYearId: String,
        val pricePerShare: Double?,
        val numberOfShares: Int = 1,
        val isAhcAuthorized: Boolean? = null,
    ): BulkEditShareSubscriptionChanges()
    sealed class Trivial: BulkEditShareSubscriptionChanges() {
        data class DistributionPoint(val distributionPointId: String?) : Trivial()
        data class PricePerShare(val pricePerShare: Double?) : Trivial()
        data class NumberOfShares(val numberOfShares: Int = 1) : Trivial()
        data class AhcAuthorization(val isAhcAuthorized: Boolean?) : Trivial()
    }
}


@Markup
@Suppress("FunctionName", "UnusedParameter")
fun BulkEditShareShareSubscriptionsModal(
    id: Int,
    texts: Lang.Block,
    modals: Storage<Modals<Int>>,
    storage: Storage<ShareManagement>,
    device: Source<DeviceType>,
    // Auxiliary data
    modifier: ModifierId,
    fiscalYears: List<FiscalYear>,
    shareOffers: List<ShareOffer>,
    distributionPoints: List<DistributionPoint>,
    // Data to be edited
    shareSubscriptions: List<ShareSubscription>,
//    setShareSubscriptions: (List<ShareSubscription>) -> Unit,
    setChanges: (BulkEditShareSubscriptionChanges) -> Unit,
    update: ()->Unit
): @Composable ElementScope<HTMLElement>.()->Unit = Modal(
    id,
    modals,
    device,
    onOk = {
        update()
    },
    onCancel = {},
    texts = texts,
    styles = auctionModalStyles(device),
) {
    var changeDistributionPoint by remember { mutableStateOf<Boolean>(false) }

    var changeStatus by remember { mutableStateOf<Boolean>(false) }

    var addShareOffer by remember { mutableStateOf<Boolean>(false) }

    var changePricePerShareStatus by remember { mutableStateOf<Boolean>(false) }

    Text("Edit share subscriptions: ${shareSubscriptions.size}")

    Wrap {
        Horizontal({
            width(100.percent)
            gap(2.px)
        }) {
            CheckBox(changeDistributionPoint) {
                changeDistributionPoint = it
                changeStatus = false
                addShareOffer = false
                changePricePerShareStatus = false
            }
            Text("Edit distribution point")
        }
        When(changeDistributionPoint) {
            var selectedDistributionPoint by remember { mutableStateOf<String>("Select") }
            val distributionPointsMap: Map<String, DistributionPoint?> = distributionPoints.associateBy{it.name}.let{
                it.toMutableMap<String, DistributionPoint?>().apply{
                    this["Select"] = null
                }
            }
            Dropdown(
                distributionPointsMap,
                selected = selectedDistributionPoint
            ) { (key, value) ->
                selectedDistributionPoint = key
                when(value){
                    null -> setChanges(BulkEditShareSubscriptionChanges.None)
                    else -> setChanges(BulkEditShareSubscriptionChanges.Trivial.DistributionPoint(value.distributionPointId))
                }
            }
        }
    }

    Wrap {
        Horizontal({
            gap(2.px)
            width(100.percent)
        }) {
            CheckBox(changeStatus) { status ->
                changeStatus = status
                changeDistributionPoint = false
                addShareOffer = false
                changePricePerShareStatus = false

            }
            Text("Edit status")
        }
        val sourceStatuses = shareSubscriptions.map { it.status }.distinct()
        When(changeStatus && sourceStatuses.size == 1) {
            val sourceStatus = sourceStatuses.first()
            val targetStatusPermissions = shareStatusTransitionsWithPermissions[sourceStatus]
            var comment by remember { mutableStateOf<String?>(null) }
            var selectedStatusState by remember { mutableStateOf<String>("Select Status") }
            val shareStatusMap: Map<String, ShareStatus?> = shareStatusTransitionsWithPermissions.keys.associateBy {
                it.value
            }.let {
                it.toMutableMap<String, ShareStatus?>().apply {
                    this["Select Status"] = null
                }
            }
            Dropdown(
                options = shareStatusMap,
                selected = selectedStatusState,
                iconContent = { opened -> SimpleUpDown(opened) },
            ) { (key, value) ->
                selectedStatusState = key
                when (value) {
                    null -> setChanges(BulkEditShareSubscriptionChanges.None)
                    else -> {
                        val changeReason = targetStatusPermissions?.first { permissions ->
                            permissions.shareStatus == value
                        }?.permissions[ChangedBy.PROVIDER]?.first()!!
                        setChanges(
                            BulkEditShareSubscriptionChanges.Status(
                                value,
                                changeReason,
                                ChangedBy.PROVIDER,
                                modifier,
                                comment = comment
                            )
                        )
                    }
                }
            }
        }
    }

    Wrap {
        Horizontal({
            gap(2.px)
            width(100.percent)
        }) {
            CheckBox(addShareOffer) { status ->
                addShareOffer = status
                changeStatus = false
                changeDistributionPoint = false
                changePricePerShareStatus = false
            }
            Text("Add subscription next to each checked share")

        }

        When(addShareOffer) {
            var numberOfShares by remember { mutableStateOf<Int>(1) }

            val shareOffersOptions = shareOffers.associateBy{"${it.fiscalYear.format()} - ${it.shareType.name}"}.let {
                it.toMutableMap<String, ShareOffer?>().apply {
                    this["Select Share Offer"] = null
                }
            }
            var selectedShareOffer by remember { mutableStateOf<String>("Select Share Offer") }


            Dropdown(
                shareOffersOptions,
                selected = selectedShareOffer,
                iconContent = { opened -> SimpleUpDown(opened) },
            ) { (key, value) ->
                selectedShareOffer = key
                when(value){
                    null -> setChanges(BulkEditShareSubscriptionChanges.None)
                    else -> setChanges(BulkEditShareSubscriptionChanges.AddShareOffer(
                        value.shareOfferId,
                        value.fiscalYear.fiscalYearId,
                        value.price,
                        numberOfShares,
                        null,
                    ))
                }
            }
        }
    }
    Wrap {
        Horizontal({
            gap(2.px)
            width(100.percent)
        }) {
            CheckBox(changePricePerShareStatus) { status ->
                changePricePerShareStatus = status
                changeStatus = false
                changeDistributionPoint = false
                addShareOffer = false
            }
        }
        Field(fieldDesktopStyle) {
            var price by remember { mutableStateOf<Double?>(null) }
            Label(
                "Price Per Share",
                id = "name-of-share-type",
                labelStyle = formLabelDesktopStyle,
                isRequired = true
            )
            TextInput("${price ?: "0"}") {
                id("name-of-share-type")
                style { textInputDesktopStyle() }
                onInput {
                    price = it.value.toDoubleOrNull()
                    setChanges(BulkEditShareSubscriptionChanges.Trivial.PricePerShare(price))
                }
            }
        }

    }
}

@Markup
fun Storage<Modals<Int>>.showBulkEditShareShareSubscriptionsModal(
    storage: Storage<ShareManagement>,
    texts: Lang.Block,
    device: Source<DeviceType>,
    // Auxiliary data
    modifier: ModifierId,
    fiscalYears: List<FiscalYear>,
    shareOffers: List<ShareOffer>,
    distributionPoints: List<DistributionPoint>,
    // Data to be edited
    shareSubscriptions: List<ShareSubscription>,
    // setShareSubscriptions: (List<ShareSubscription>) -> Unit,
    setChanges: (BulkEditShareSubscriptionChanges) -> Unit,
    update: ()->Unit
) = with(nextId()) {
    put(this to ModalData(
        ModalType.Dialog,
        BulkEditShareShareSubscriptionsModal(
            this,
            texts,
            this@showBulkEditShareShareSubscriptionsModal,
            storage,
            device,
            modifier,
            fiscalYears,
            shareOffers,
            distributionPoints,
            shareSubscriptions,
        //    setShareSubscriptions,
            setChanges,
            update = update
        )
    ) )
}

fun defaultBulkEditTexts(): Lang.Block = texts {
    key = "dialog"
    variable {
        key = "title"
        value = "Bulk Edit Share Subscriptions"
    }
    block{
        key = "okButton"
        variable {
            key = "title"
            value = "Ok"
        }

    }
    block{
        key = "cancelButton"
        variable {
            key = "title"
            value = "Cancel"
        }
    }
    block {
        key = "content"
        /*
        variable {
            key = "message"
            value = message
        }

         */
    }
}

