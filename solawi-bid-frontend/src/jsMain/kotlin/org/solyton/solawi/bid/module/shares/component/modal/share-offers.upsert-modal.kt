package org.solyton.solawi.bid.module.shares.component.modal

import org.evoleq.compose.conditional.When
import org.jetbrains.compose.web.css.marginTop
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.solyton.solawi.bid.module.banking.data.fiscalyear.FiscalYear
import org.solyton.solawi.bid.module.banking.data.fiscalyear.format
import org.solyton.solawi.bid.module.control.dropdown.Dropdown
import org.solyton.solawi.bid.module.control.dropdown.DropdownStyles
import org.solyton.solawi.bid.module.navbar.component.SimpleUpDown
import org.solyton.solawi.bid.module.shares.data.api.PricingType
import org.solyton.solawi.bid.module.shares.data.offers.ShareOffer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.evoleq.compose.Markup
import org.evoleq.compose.form.field.Field
import org.evoleq.compose.form.label.Label
import org.evoleq.compose.modal.Modal
import org.evoleq.compose.modal.ModalData
import org.evoleq.compose.modal.ModalType
import org.evoleq.compose.modal.Modals
import org.evoleq.compose.style.data.device.DeviceType
import org.evoleq.device.data.mediaType
import org.evoleq.language.Lang
import org.evoleq.math.Source
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.nextId
import org.evoleq.optics.storage.put
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.attributes.required
import org.jetbrains.compose.web.dom.ElementScope
import org.jetbrains.compose.web.dom.Form
import org.jetbrains.compose.web.dom.TextInput
import org.solyton.solawi.bid.module.bid.component.styles.auctionModalStyles
import org.solyton.solawi.bid.module.shares.data.management.deviceData
import org.solyton.solawi.bid.module.shares.data.management.ShareManagement
import org.solyton.solawi.bid.module.shares.data.types.ShareType
import org.solyton.solawi.bid.module.style.form.fieldDesktopStyle
import org.solyton.solawi.bid.module.style.form.formLabelDesktopStyle
import org.solyton.solawi.bid.module.values.ProviderId
import org.w3c.dom.HTMLElement


@Markup
@Suppress("FunctionName")
fun UpsertShareOffersModal(
    id: Int,
    texts: Lang.Block,
    modals: Storage<Modals<Int>>,
    storage: Storage<ShareManagement>,
    device: Source<DeviceType>,
    fiscalYears: List<FiscalYear>,
    shareTypes: List<ShareType>,
    shareOffer: ShareOffer?,
    setShareOffer: (ShareOffer)->Unit,
    update: ()->Unit
): @Composable ElementScope<HTMLElement>.()->Unit = Modal(
    id,
    modals,
    storage * deviceData * mediaType.get,
    onOk = {
        update()
    },
    onCancel = {},
    texts = texts,
    styles = auctionModalStyles(device),
) {
    var shareOffer by remember { mutableStateOf<ShareOffer?>(shareOffer) }

    val dropdownStyles = DropdownStyles()
        .modifyContainerStyle {
            marginTop(5.px)
            width(100.percent)
        }

    Form {
        Field(fieldDesktopStyle) {
            Label(
                "Fiscal year",
                id = "fiscal-year-of-share-type",
                labelStyle = formLabelDesktopStyle,
                isRequired = true
            )
            val fiscalYearsMap: Map<String, FiscalYear> = fiscalYears.associateBy{it.format()}
            Dropdown(
                options = fiscalYearsMap,
                selected = shareOffer?.fiscalYear?.format() ?: "Select fiscal year",
                styles = dropdownStyles,
                iconContent = {expanded -> SimpleUpDown(expanded) }
            ) { (_, value) ->
                shareOffer = (shareOffer?:ShareOffer.default).copy(
                    fiscalYear = value
                )
                setShareOffer(shareOffer!!)
            }

        }

        Field(fieldDesktopStyle) {
            Label(
                "Share type",
                id = "share-type-of-share-offer",
                labelStyle = formLabelDesktopStyle,
                isRequired = true
            )
            val shareTypesMap: Map<String, ShareType> = shareTypes.associateBy{it.name}
            Dropdown(
                options = shareTypesMap,
                selected = shareOffer?.shareType?.name ?: "Select share type",
                styles = dropdownStyles,
                iconContent = {expanded -> SimpleUpDown(expanded) }
            ) { (_, value) ->
                shareOffer = (shareOffer?:ShareOffer.default).copy(
                    shareType = value
                )
                setShareOffer(shareOffer!!)
            }
        }

        Field(fieldDesktopStyle) {
            Label(
                "Pricing Type",
                id = "key-of-share-type",
                labelStyle = formLabelDesktopStyle,
                isRequired = true
            )

            val pricingTypesMap = mapOf<String, PricingType>(
                PricingType.FIXED.name to PricingType.FIXED,
                PricingType.FLEXIBLE.name to PricingType.FLEXIBLE
            )

            Dropdown(
                options = pricingTypesMap,
                selected = shareOffer?.pricingType?.name ?: "Select pricing type",
                styles = dropdownStyles,
                iconContent = {expanded -> SimpleUpDown(expanded) }
            ) { (_, value) ->
                shareOffer = (shareOffer?:ShareOffer.default).copy(
                    pricingType = value
                )
                setShareOffer(shareOffer!!)
            }
        }
        When(shareOffer?.pricingType == PricingType.FIXED) {
            Field(fieldDesktopStyle) {
                Label(
                    "Price",
                    id = "price-of-share-type",
                    labelStyle = formLabelDesktopStyle,
                    isRequired = true
                )
                TextInput(shareOffer?.price?.toString() ?: "") {
                    required()
                    id("price-of-share-type")
                    onInput {
                        shareOffer = (shareOffer?:ShareOffer.default).copy(
                            price = it.value.toDoubleOrNull()
                        )
                        setShareOffer(shareOffer!!)
                    }
                }
            }
        }
        Field(fieldDesktopStyle) {
            Label(
                "SEPA required",
                id = "sepa-of-share-offer",
                labelStyle = formLabelDesktopStyle,
                isRequired = true
            )
            val booleansMap: Map<String, Boolean> = mapOf(
                "☑\uFE0F" to true,
                "❌" to false
            )
            fun getKeyOf(value: Boolean): String = booleansMap.filter { it.value == value }.keys.first()
            Dropdown(
                options = booleansMap,
                selected = getKeyOf(shareOffer?.ahcAuthorizationRequired?:false),
                styles = dropdownStyles,
                iconContent = {expanded -> SimpleUpDown(expanded) }
            ) { (_, value) ->
                shareOffer = (shareOffer?:ShareOffer.default).copy(
                    ahcAuthorizationRequired = value
                )
                setShareOffer(shareOffer!!)
            }
        }
    }
}

@Markup
fun Storage<Modals<Int>>.showUpsertShareOffersModal(
    storage: Storage<ShareManagement>,
    texts: Lang.Block,
    device: Source<DeviceType>,
    fiscalYears: List<FiscalYear>,
    shareTypes: List<ShareType>,
    shareOffer: ShareOffer? = null,
    setShareOffer: (ShareOffer)->Unit = {},
    update: ()->Unit
) = with(nextId()) {
    put(this to ModalData(
        ModalType.Dialog,
        UpsertShareOffersModal(
            this,
            texts,
            this@showUpsertShareOffersModal,
            storage,
            device,
            fiscalYears,
            shareTypes,
            shareOffer,
            setShareOffer,
            update = update
        )
    ) )
}
