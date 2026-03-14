package org.solyton.solawi.bid.application.ui.component.organization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import org.evoleq.compose.Markup
import org.evoleq.compose.form.Form
import org.evoleq.compose.style.data.device.DeviceType
import org.evoleq.language.Lang
import org.evoleq.language.get
import org.evoleq.language.subComp
import org.evoleq.language.title
import org.evoleq.math.Reader
import org.evoleq.math.Source
import org.evoleq.math.emit
import org.evoleq.math.times
import org.evoleq.uuid.NIL_UUID
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.backgroundColor
import org.jetbrains.compose.web.css.border
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.css.justifyContent
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.style
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.application.ui.page.user.style.listItemWrapperStyle
import org.solyton.solawi.bid.module.banking.data.fiscalyear.format
import org.solyton.solawi.bid.module.control.button.EditButton
import org.solyton.solawi.bid.module.distribution.data.distributionpoint.DistributionPoint
import org.solyton.solawi.bid.module.list.component.ActionsWrapper
import org.solyton.solawi.bid.module.list.component.DataWrapper
import org.solyton.solawi.bid.module.list.component.EditableIntCell
import org.solyton.solawi.bid.module.list.component.EditableNullablePriceCell
import org.solyton.solawi.bid.module.list.component.EditableSelectCell
import org.solyton.solawi.bid.module.list.component.EditableSelectCellStyles
import org.solyton.solawi.bid.module.list.component.EditableTextCell
import org.solyton.solawi.bid.module.list.component.Header
import org.solyton.solawi.bid.module.list.component.HeaderCell
import org.solyton.solawi.bid.module.list.component.HeaderWrapper
import org.solyton.solawi.bid.module.list.component.ListItemWrapper
import org.solyton.solawi.bid.module.list.component.ListItemsIndexed
import org.solyton.solawi.bid.module.list.component.ListWrapper
import org.solyton.solawi.bid.module.list.component.TextCell
import org.solyton.solawi.bid.module.list.component.Title
import org.solyton.solawi.bid.module.list.component.TitleWrapper
import org.solyton.solawi.bid.module.list.style.defaultListStyles
import org.solyton.solawi.bid.module.navbar.component.SimpleUpDown
import org.solyton.solawi.bid.module.shares.component.dropdown.ShareOffersDropdown
import org.solyton.solawi.bid.module.shares.data.api.PricingType
import org.solyton.solawi.bid.module.shares.data.api.UpdateShareStatus
import org.solyton.solawi.bid.module.shares.data.internal.ChangedBy
import org.solyton.solawi.bid.module.shares.data.internal.ShareStatus
import org.solyton.solawi.bid.module.shares.data.internal.shareStatusTransitionsWithPermissions
import org.solyton.solawi.bid.module.shares.data.offers.ShareOffer
import org.solyton.solawi.bid.module.shares.data.subscriptions.ShareSubscription
import org.solyton.solawi.bid.module.shares.data.subscriptions.ShareSubscriptions
import org.solyton.solawi.bid.module.shares.data.subscriptions.ahcAuthorized
import org.solyton.solawi.bid.module.shares.data.subscriptions.coSubscribers
import org.solyton.solawi.bid.module.shares.data.subscriptions.distributionPointId
import org.solyton.solawi.bid.module.shares.data.subscriptions.numberOfShares
import org.solyton.solawi.bid.module.shares.data.subscriptions.pricePerShare
import org.solyton.solawi.bid.module.shares.data.toApiType
import org.solyton.solawi.bid.module.shares.data.values.ShareSubscriptionId
import org.solyton.solawi.bid.module.style.form.formDesktopStyle
import org.solyton.solawi.bid.module.user.data.managed.ManagedUser
import org.solyton.solawi.bid.module.user.data.profile.UserProfile
import org.solyton.solawi.bid.module.values.ModifierId
import org.solyton.solawi.bid.module.values.Price
import org.solyton.solawi.bid.module.values.ProviderId
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.orEmpty
import kotlin.collections.plus

@Markup
@Composable
@Suppress("FunctionName", "CyclomaticComplexMethod")
fun ShareSubscriptionsForm(
    inputs: Source<Lang.Block>,
    device: Source<DeviceType>,
    organizationId: ProviderId,
    changesDoneBy: ChangedBy,
    currentUser: ManagedUser,
    userProfile: UserProfile,
    shareSubscriptions: ShareSubscriptions?,
    setShareSubscriptions: (ShareSubscriptions) -> Unit,
    updateShareStatus: suspend (UpdateShareStatus) -> Unit,
    shareOffers: List<ShareOffer>,
    distributionPoints: List<DistributionPoint>
) {
    val scope = rememberCoroutineScope()
    Form(formDesktopStyle) {
        val shareOffersMap = shareOffers.associateBy { shareOffer ->
            shareOffer.shareOfferId
        }
        // Subscriptions
        val subscriptionsInputs = inputs * subComp("listOfShareSubscriptions")
        val subscriptionHeaders = subscriptionsInputs * subComp("headers")
        var shareSubscriptions by remember { mutableStateOf(shareSubscriptions) }
        val unsubscribedShareOffers = shareOffers.filter { shareOffer ->
            shareOffer.shareOfferId !in (shareSubscriptions?.all?.map { it.shareOfferId } ?: emptyList())
        }
        ListWrapper {
            TitleWrapper {
                Title { H3 { Text((subscriptionsInputs * title).emit()) } }
                if (unsubscribedShareOffers.isEmpty()) return@TitleWrapper
                ShareOffersDropdown(
                    options = unsubscribedShareOffers.associateBy {
                        it.fiscalYear.format() + "/" + it.shareType.name
                    },
                    selected = null,
                    closeOnSelect = true
                ) { (_, shareOffer) ->

                    shareSubscriptions = (shareSubscriptions?.all.orEmpty() + ShareSubscription(
                        NIL_UUID,
                        shareOffer.shareType.providerId,
                        shareOffer.shareOfferId,
                        userProfile.userProfileId,
                        null,
                        shareOffer.fiscalYear.fiscalYearId,
                        1,
                        null,
                        false,
                        ShareStatus.PendingActivation,
                        emptyList()

                    )).let { ShareSubscriptions(it) }
                }
            }
            val tableStyles = defaultListStyles

                .modifyHeaderWrapper {
                    width(100.percent)
                    justifyContent(JustifyContent.SpaceBetween)
                }
            /*
                .modifyHeader { width(90.percent) }
            .modifyDataWrapper { width(90.percent) }
            .modifyActionsWrapper { width(10.percent) }
        */

            HeaderWrapper(tableStyles.headerWrapper) {
                Header(tableStyles.header) {
                    HeaderCell(subscriptionHeaders * subComp("fiscalYear") * title) {
                        width(10.percent)
                    }
                    HeaderCell(subscriptionHeaders * subComp("shareType") * title) {
                        width(10.percent)
                    }
                    HeaderCell(subscriptionHeaders * subComp("pricingType") * title) {
                        width(10.percent)
                    }
                    HeaderCell(subscriptionHeaders * subComp("numberOfShares") * title) {
                        width(5.percent)
                    }
                    HeaderCell(subscriptionHeaders * subComp("pricePerShare") * title) {
                        width(5.percent)
                    }
                    HeaderCell(subscriptionHeaders * subComp("state") * title) {
                        width(10.percent)
                    }
                    HeaderCell(subscriptionHeaders * subComp("ahcAuthorized") * title) {
                        width(5.percent)
                    }
                    HeaderCell(subscriptionHeaders * subComp("depository") * title) {
                        width(5.percent)
                    }
                    HeaderCell(subscriptionHeaders * subComp("coSubscribers") * title) {
                        width(40.percent)
                    }
                }
            }
            val checkIt: (Boolean) -> Reader<Lang.Block, String> = { bool: Boolean ->
                Reader { lang: Lang.Block ->
                    lang["$bool"]
                }
            }
            ListItemsIndexed(shareSubscriptions?.all ?: emptyList()) { index, shareSubscription ->
                var editShareSubscriptionState by remember { mutableStateOf(false) }
                val shareOffer = requireNotNull(shareOffersMap[shareSubscription.shareOfferId]) {
                    "Share offer not found"
                }
                ListItemWrapper({

                    listItemWrapperStyle(this, index)
                    if (editShareSubscriptionState) {
                        backgroundColor(Color.orange)
                        border {
                            style(LineStyle.Solid)
                            color(Color.orange)
                            width(1.px)
                        }
                    }
                }) {
                    DataWrapper(defaultListStyles.dataWrapper) {
                        TextCell(shareOffer.fiscalYear.format()) {
                            width(10.percent)
                        }
                        TextCell(shareOffer.shareType.name) {
                            width(10.percent)
                        }
                        TextCell(shareOffer.pricingType.name) {
                            width(10.percent)
                        }
                        EditableIntCell(
                            initValue = shareSubscription.numberOfShares,
                            disabled = !editShareSubscriptionState,
                            style = { width(5.percent) }
                        ) { numberOfShares ->
                            shareSubscriptions =
                                requireNotNull(shareSubscriptions).all.mapIndexed { shareSubscriptionIndex, shareSubscription ->
                                    when (shareSubscriptionIndex) {
                                        index -> shareSubscription.numberOfShares {
                                            numberOfShares ?: 0
                                        }

                                        else -> shareSubscription
                                    }
                                }.let { list -> ShareSubscriptions(list) }
                            setShareSubscriptions(shareSubscriptions!!)
                        }
                        EditableNullablePriceCell(
                            initValue = (shareSubscription.pricePerShare ?: shareOffer.price)?.let { Price(it) },
                            disabled = shareOffer.pricingType == PricingType.FIXED || !editShareSubscriptionState,
                            style = { width(5.percent) }
                        ) { price ->
                            shareSubscriptions =
                                requireNotNull(shareSubscriptions).all.mapIndexed { shareSubscriptionIndex, shareSubscription ->
                                    when (shareSubscriptionIndex) {
                                        index -> shareSubscription.pricePerShare {
                                            price?.value
                                        }

                                        else -> shareSubscription
                                    }
                                }.let { list -> ShareSubscriptions(list) }
                            setShareSubscriptions(shareSubscriptions!!)
                        }

                        var shareStatusState by remember { mutableStateOf(shareSubscription.status) }
                        val allowedShareStatusTransitionTargets = requireNotNull(
                            shareStatusTransitionsWithPermissions[shareStatusState]
                        ) {
                            "Share status transition not found for status $shareStatusState"
                        }.filter { it.permissions[changesDoneBy] != null }.associateBy({ it.shareStatus.value }) {
                            it.shareStatus
                        } + (shareSubscription.status.value to shareSubscription.status)
                        val changeReasons = requireNotNull(
                            shareStatusTransitionsWithPermissions[shareStatusState]
                        ) {
                            "Share status transition not found for status $shareStatusState"
                        }.filter { it.permissions[changesDoneBy] != null }.associateBy({ it.shareStatus.value }) {
                            it.permissions[changesDoneBy].orEmpty()
                        }

                        EditableSelectCell(
                            options = allowedShareStatusTransitionTargets,
                            selected = shareStatusState,
                            disabled = !editShareSubscriptionState,
                            styles = EditableSelectCellStyles.modifyContainerStyle {
                                width(10.percent)
                            },
                            iconContent = { expanded ->
                                SimpleUpDown(expanded)
                            }
                        ) { shareStatus ->
                            scope.launch {
                                updateShareStatus(
                                    UpdateShareStatus(
                                        providerId = organizationId,
                                        shareSubscriptionId = ShareSubscriptionId(shareSubscription.shareSubscriptionId),
                                        nextState = shareStatus.toApiType(),
                                        reason = changeReasons[shareStatus.value]!!.first()
                                            .toApiType(), // todo:dev chose in dialog?
                                        changedBy = changesDoneBy.toApiType(), //
                                        modifier = ModifierId(currentUser.id),
                                        comment = "Subscription status changed by user '${currentUser.username}'" // todo:dev set in dialog?
                                    )
                                )
                            }
                            shareStatusState = shareStatus
                        }

                        val check = { checked: Boolean ->
                            (subscriptionHeaders * subComp("ahcAuthorized") * checkIt(
                                checked
                            )).emit()
                        }
                        val ahcAuthorized = shareSubscription.ahcAuthorized ?: false
                        EditableSelectCell(
                            options = mapOf(check(true) to true, check(false) to false),
                            selected = ahcAuthorized, disabled = !editShareSubscriptionState,
                            styles = EditableSelectCellStyles.modifyContainerStyle {
                                width(5.percent)
                            },
                            iconContent = { expanded ->
                                SimpleUpDown(expanded)
                            }
                        ) { ahcAuthorized ->
                            shareSubscriptions =
                                requireNotNull(shareSubscriptions).all.mapIndexed { shareSubscriptionIndex, shareSubscription ->
                                    when (shareSubscriptionIndex) {
                                        index -> shareSubscription.ahcAuthorized {
                                            ahcAuthorized
                                        }

                                        else -> shareSubscription
                                    }
                                }.let { list -> ShareSubscriptions(list) }
                            setShareSubscriptions(shareSubscriptions!!)
                        }


                        val selected = distributionPoints.firstOrNull {
                            it.distributionPointId == shareSubscription.distributionPointId
                        }
                        EditableSelectCell(
                            options = distributionPoints.associateBy { it.name },
                            selected = selected,
                            disabled = !editShareSubscriptionState,
                            styles = EditableSelectCellStyles.modifyContainerStyle {
                                width(5.percent)
                            },
                            iconContent = { expanded ->
                                SimpleUpDown(expanded)
                            }
                        ) { distributionPoint ->
                            shareSubscriptions =
                                requireNotNull(shareSubscriptions).all.mapIndexed { shareSubscriptionIndex, shareSubscription ->
                                    when (shareSubscriptionIndex) {
                                        index -> shareSubscription.distributionPointId {
                                            distributionPoint.distributionPointId
                                        }

                                        else -> shareSubscription
                                    }
                                }.let { list -> ShareSubscriptions(list) }
                            setShareSubscriptions(shareSubscriptions!!)
                        }

                        EditableTextCell(
                            text = shareSubscription.coSubscribers.joinToString(", "),
                            disabled = !editShareSubscriptionState,
                            style = { width(40.percent) }
                        ) { coSubscribers ->
                            shareSubscriptions =
                                requireNotNull(shareSubscriptions).all.mapIndexed { shareSubscriptionIndex, shareSubscription ->
                                    when (shareSubscriptionIndex) {
                                        index -> shareSubscription.coSubscribers {
                                            coSubscribers.split(",").map { it.trim() }
                                        }

                                        else -> shareSubscription
                                    }
                                }.let { list -> ShareSubscriptions(list) }
                            setShareSubscriptions(shareSubscriptions!!)
                        }
                    }
                    ActionsWrapper(tableStyles.actionsWrapper) {
                        EditButton(
                            color = Color.black,
                            bgColor = Color.white,
                            texts = { "" },
                            deviceType = device,
                            isDisabled = false
                        ) {
                            editShareSubscriptionState = !editShareSubscriptionState
                        }
                    }
                }
            }
        }
    }
}
