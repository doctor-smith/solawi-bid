package org.solyton.solawi.bid.module.distribution.component.modal

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
import org.solyton.solawi.bid.module.distribution.data.distributionpoint.DistributionPoint
import org.solyton.solawi.bid.module.distribution.data.management.DistributionManagement
import org.solyton.solawi.bid.module.distribution.data.management.deviceData
import org.solyton.solawi.bid.module.style.form.fieldDesktopStyle
import org.solyton.solawi.bid.module.style.form.formLabelDesktopStyle
import org.solyton.solawi.bid.module.style.form.textInputDesktopStyle
import org.solyton.solawi.bid.module.values.ProviderId
import org.w3c.dom.HTMLElement


@Markup
@Suppress("FunctionName")
fun UpsertDistributionPointModal(
    id: Int,
    texts: Lang.Block,
    modals: Storage<Modals<Int>>,
    storage: Storage<DistributionManagement>,
    device: Source<DeviceType>,
    providerId: ProviderId,
    distributionPoint: DistributionPoint?,
    setDistributionPoint: (DistributionPoint)->Unit,
    // cancel: ()->Unit,
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
    Form {
        var distributionPointName by remember { mutableStateOf(distributionPoint?.name ?: "") }
        Field(fieldDesktopStyle) {
            Label(
                "Name",
                id = "distribution-point-name",
                labelStyle = formLabelDesktopStyle,
                isRequired = true
            )
            TextInput(distributionPointName) {
                required()
                id("distribution-point-name")
                style { textInputDesktopStyle() }
                onInput {
                    distributionPointName = it.value
                    if(it.value.isNotBlank()) setDistributionPoint(
                        distributionPoint?.copy(name = it.value) ?: DistributionPoint(
                            distributionPointId = "",
                            name = it.value,
                            address =  null,
                            organizationId = providerId.value
                        )
                    )
                }
            }
        }
    }
}

@Markup
fun Storage<Modals<Int>>.showUpsertDistributionPointModal(
    storage: Storage<DistributionManagement>,
    texts: Lang.Block,
    device: Source<DeviceType>,
    providerId: ProviderId,
    distributionPoint: DistributionPoint? = null,
    setDistributionPoint: (DistributionPoint)->Unit = {},
    update: ()->Unit
) = with(nextId()) {
    put(this to ModalData(
        ModalType.Dialog,
        UpsertDistributionPointModal(
            this,
            texts,
            this@showUpsertDistributionPointModal,

            storage,
            device,
            providerId,
            distributionPoint,
            setDistributionPoint,
            update = update
        )
    ) )
}
