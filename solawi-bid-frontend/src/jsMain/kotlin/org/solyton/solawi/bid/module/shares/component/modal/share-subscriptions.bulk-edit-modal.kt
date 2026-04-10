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
import org.evoleq.language.Lang
import org.evoleq.language.texts
import org.evoleq.math.Source
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.nextId
import org.evoleq.optics.storage.put
import org.jetbrains.compose.web.css.CSSUnit
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.css.gap
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.ElementScope
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.TextInput
import org.solyton.solawi.bid.module.banking.data.creditor.identifier.CreditorIdentifier
import org.solyton.solawi.bid.module.banking.data.fiscalyear.FiscalYear
import org.solyton.solawi.bid.module.banking.data.fiscalyear.format
import org.solyton.solawi.bid.module.banking.data.sepa.MandateStatus
import org.solyton.solawi.bid.module.banking.data.sepa.collection.SepaCollection
import org.solyton.solawi.bid.module.bid.component.styles.auctionModalStyles
import org.solyton.solawi.bid.module.constants.CHECK_FALSE
import org.solyton.solawi.bid.module.constants.CHECK_TRUE
import org.solyton.solawi.bid.module.constants.checkIcon
import org.solyton.solawi.bid.module.control.checkbox.CheckBox
import org.solyton.solawi.bid.module.control.dropdown.Dropdown
import org.solyton.solawi.bid.module.distribution.data.distributionpoint.DistributionPoint
import org.solyton.solawi.bid.module.navbar.component.SimpleUpDown
import org.solyton.solawi.bid.module.shares.data.internal.ChangeReason
import org.solyton.solawi.bid.module.shares.data.internal.ChangedBy
import org.solyton.solawi.bid.module.shares.data.internal.ShareStatus
import org.solyton.solawi.bid.module.shares.data.internal.shareStatusTransitionsWithPermissions
import org.solyton.solawi.bid.module.shares.data.management.ShareManagement
import org.solyton.solawi.bid.module.shares.data.offers.ShareOffer
import org.solyton.solawi.bid.module.shares.data.subscriptions.ShareSubscription
import org.solyton.solawi.bid.module.shares.data.values.ShareOfferId
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
    data class AddSepaMandate(
        val shareOfferId: ShareOfferId,
        val creditorIdentifier: CreditorIdentifier,
        val sepaCollection: SepaCollection,
        val status: MandateStatus,
        val isActive: Boolean,
        val mandateReferencePadStart: Int,
        val minimalMandateNumber: Int
    ): BulkEditShareSubscriptionChanges()
    sealed class Trivial: BulkEditShareSubscriptionChanges() {
        data class DistributionPoint(val distributionPointId: String?) : Trivial()
        data class PricePerShare(val pricePerShare: Double?) : Trivial()
        data class NumberOfShares(val numberOfShares: Int = 1) : Trivial()
        data class AhcAuthorization(val isAhcAuthorized: Boolean?) : Trivial()
    }
}

enum class Checked {
    NONE,
    CHANGE_DISTRIBUTION_POINT,
    CHANGE_NUMBER_OF_SHARES,
    CHANGE_PRICE_PER_SHARE,
    CHANGE_SHARE_STATUS,
    CHANGE_AHC_AUTHORIZED,
    ADD_SHARE_SUBSCRIPTION,
    ADD_SEPA_MANDATE
}

@Markup
@Suppress("FunctionName", "UnusedParameter", "CyclomaticComplexMethod")
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
    sepaCollections: List<SepaCollection>,
    creditorIdentifier: CreditorIdentifier?,
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

    var checked by remember { mutableStateOf<Checked>(Checked.NONE)}

    Text("Edit share subscriptions: ${shareSubscriptions.size}")
    key(checked){
        Wrap {
            Horizontal({
                width(100.percent)
                gap(2.px)
            }) {
                CheckBox(checked == Checked.CHANGE_DISTRIBUTION_POINT) { state ->
                    if(state) checked = Checked.CHANGE_DISTRIBUTION_POINT
                }
                Text("Edit distribution point")
            }
            When(checked == Checked.CHANGE_DISTRIBUTION_POINT) {
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
                CheckBox(checked == Checked.CHANGE_SHARE_STATUS) { status ->
                    if(status)  checked = Checked.CHANGE_SHARE_STATUS
                }
                Text("Edit status")
            }
            val sourceStatuses = shareSubscriptions.map { it.status }.distinct()
            When((checked == Checked.CHANGE_SHARE_STATUS) && sourceStatuses.size == 1) {
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
                CheckBox(checked == Checked.CHANGE_PRICE_PER_SHARE) { status ->
                    if(status) checked = Checked.CHANGE_PRICE_PER_SHARE
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

        Wrap {
            Horizontal({
                gap(2.px)
                width(100.percent)
            }) {
                CheckBox(checked == Checked.CHANGE_AHC_AUTHORIZED) { status ->
                    if (status) checked = Checked.CHANGE_AHC_AUTHORIZED
                }
                Field(fieldDesktopStyle) {
                    var ahcAuthorized by remember { mutableStateOf<Boolean?>(null) }
                    Label(
                        "Sepa Authorized",
                        id = "sepa-authorized",
                        labelStyle = formLabelDesktopStyle,
                        isRequired = true
                    )
                    val booleanOptions = mapOf(
                        "--" to null,
                        CHECK_TRUE to true,
                        CHECK_FALSE to false
                    )
                    Dropdown(
                        options = booleanOptions,
                        selected = ahcAuthorized.checkIcon("--"),
                        iconContent = {opened -> SimpleUpDown(opened)}
                    ) {
                        (_, value) ->
                        ahcAuthorized = value
                        setChanges(BulkEditShareSubscriptionChanges.Trivial.AhcAuthorization(
                            isAhcAuthorized = value
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
                CheckBox(checked == Checked.ADD_SHARE_SUBSCRIPTION) { status ->
                    if(status) checked = Checked.ADD_SHARE_SUBSCRIPTION
                }
                Text("Add subscription next to each checked share")

            }

            When(checked == Checked.ADD_SHARE_SUBSCRIPTION) {
                var numberOfShares by remember { mutableStateOf<Int>(1) }

                val shareOffersOptions =
                    shareOffers.associateBy { "${it.fiscalYear.format()} - ${it.shareType.name}" }.let {
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
                    when (value) {
                        null -> setChanges(BulkEditShareSubscriptionChanges.None)
                        else -> setChanges(
                            BulkEditShareSubscriptionChanges.AddShareOffer(
                                value.shareOfferId,
                                value.fiscalYear.fiscalYearId,
                                value.price,
                                numberOfShares,
                                null,
                            )
                        )
                    }
                }
            }
        }

        Wrap {
            Horizontal({
                gap(2.px)
                width(100.percent)
            }) {
                CheckBox(checked == Checked.ADD_SEPA_MANDATE) { status ->
                    if (status) checked = Checked.ADD_SEPA_MANDATE
                }
                Text("Add sepa mandate to each checked share")

            }
            val creditorDefined = creditorIdentifier != null
            val collectionsDefined = sepaCollections.isNotEmpty()
            val offersDefined = shareOffers.isNotEmpty()
            val ahcAuthorizationsProvided = shareSubscriptions.none { it.ahcAuthorized != true }


            val active = creditorDefined && collectionsDefined && offersDefined && ahcAuthorizationsProvided
            When(!active) {
                val texts = listOf(
                    !creditorDefined to "No creditor defined",
                    !collectionsDefined to "No sepa collections defined",
                    !offersDefined to "No offers defined",
                    !ahcAuthorizationsProvided to "Missing Sepa Authorizations"
                ).filter{it.first}.map { it.second }

                texts.forEach {
                    Span({ style { color(Color.red) } }) {
                        Text(it)
                    }
                }
            }
            When(checked == Checked.ADD_SEPA_MANDATE && active) {
                requireNotNull(creditorIdentifier)
                require(sepaCollections.isNotEmpty())
                require(shareOffers.isNotEmpty())

                var selectedShareOffer by remember { mutableStateOf<String>("Select Share Offer") }
                var selectedSepaCollection by remember { mutableStateOf("Select Sepa Collection")}
                var selectedMandateStatus by remember { mutableStateOf(MandateStatus.ACTIVE)}

                val sepaCollectionOptions =
                    sepaCollections.associateBy { it.mandateReferencePrefix.value }.let {
                        it.toMutableMap<String, SepaCollection?>().apply {
                            this["Select Sepa Collection"] = null
                        }
                    }

                var shareOffersOptions by remember(selectedSepaCollection){
                    val allowedOffers = sepaCollectionOptions[selectedSepaCollection]?.referenceIds?.map { it.value }.orEmpty()
                    mutableStateOf(shareOffers
                        .filter { it.shareOfferId in allowedOffers }
                        .associateBy { "${it.fiscalYear.format()} - ${it.shareType.name}" }
                        .let {
                            it.toMutableMap<String, ShareOffer?>().apply {
                                this["Select Share Offer"] = null
                            }
                        }
                    )
                }

                val mandateStatusOptions = mapOf(
                    MandateStatus.ACTIVE.name to MandateStatus.ACTIVE,
                    MandateStatus.EXPIRED.name to MandateStatus.EXPIRED,
                    MandateStatus.REVOKED.name to MandateStatus.REVOKED,
                    MandateStatus.SUSPENDED.name to MandateStatus.SUSPENDED
                )
                val activeMandateStatuses = setOf(MandateStatus.ACTIVE, MandateStatus.REVOKED)
                Horizontal {
                    Dropdown(
                        shareOffersOptions,
                        selected = selectedShareOffer,
                        iconContent = { opened -> SimpleUpDown(opened) },
                    ) { (key, value) ->
                        selectedShareOffer = key
                        when (value) {
                            null -> setChanges(BulkEditShareSubscriptionChanges.None)
                            else -> {
                                val sepaCollection = sepaCollectionOptions[selectedSepaCollection]!!
                                val minimalMandateNumber = sepaCollection.sepaMandates.size
                                val mandateReferencePadStart = 4

                                setChanges(
                                    BulkEditShareSubscriptionChanges.AddSepaMandate(
                                        shareOfferId = ShareOfferId(value.shareOfferId),
                                        creditorIdentifier = creditorIdentifier,
                                        sepaCollection = sepaCollection,
                                        status = selectedMandateStatus,
                                        isActive = selectedMandateStatus in activeMandateStatuses,
                                        mandateReferencePadStart = mandateReferencePadStart,
                                        minimalMandateNumber = minimalMandateNumber,
                                    )
                                )
                            }
                        }
                    }

                    Dropdown(
                        sepaCollectionOptions,
                        selected = selectedSepaCollection,
                        iconContent = { opened -> SimpleUpDown(opened) },
                    ) { (key, value) ->
                        selectedSepaCollection = key
                        when (value) {
                            null -> setChanges(BulkEditShareSubscriptionChanges.None)
                            else -> {
                                val shareOffer = shareOffersOptions[selectedShareOffer]!!
                                val minimalMandateNumber = value.sepaMandates.size
                                val mandateReferencePadStart = 4

                                setChanges(
                                    BulkEditShareSubscriptionChanges.AddSepaMandate(
                                        shareOfferId = ShareOfferId(shareOffer.shareOfferId),
                                        creditorIdentifier = creditorIdentifier,
                                        sepaCollection = value,
                                        status = selectedMandateStatus,
                                        isActive = selectedMandateStatus in activeMandateStatuses,
                                        mandateReferencePadStart = mandateReferencePadStart,
                                        minimalMandateNumber = minimalMandateNumber,
                                    )
                                )
                            }
                        }
                    }

                    Dropdown(
                        mandateStatusOptions,
                        selected = selectedMandateStatus.name,
                        iconContent = { opened -> SimpleUpDown(opened) },
                    ) { (_, value) ->
                        selectedMandateStatus = value
                        when (value) {
                            null -> setChanges(BulkEditShareSubscriptionChanges.None)
                            else -> {
                                val sepaCollection = sepaCollectionOptions[selectedSepaCollection]!!
                                val shareOffer = shareOffersOptions[selectedShareOffer]!!
                                val minimalMandateNumber = sepaCollection.sepaMandates.size
                                val mandateReferencePadStart = 4

                                setChanges(
                                    BulkEditShareSubscriptionChanges.AddSepaMandate(
                                        shareOfferId = ShareOfferId(shareOffer.shareOfferId),
                                        creditorIdentifier = creditorIdentifier,
                                        sepaCollection = sepaCollection,
                                        status = selectedMandateStatus,
                                        isActive = selectedMandateStatus in activeMandateStatuses,
                                        mandateReferencePadStart = mandateReferencePadStart,
                                        minimalMandateNumber = minimalMandateNumber,
                                    )
                                )
                            }
                        }
                    }
                }

            }
        } }
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
    sepaCollections: List<SepaCollection>,
    creditorIdentifier: CreditorIdentifier?,
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
            sepaCollections,
            creditorIdentifier,
        //    setShareSubscriptions,
            setChanges,
            update = update
        )
    ) )
}

fun defaultBulkEditTexts(): Lang.Block = "dialog" texts {
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
