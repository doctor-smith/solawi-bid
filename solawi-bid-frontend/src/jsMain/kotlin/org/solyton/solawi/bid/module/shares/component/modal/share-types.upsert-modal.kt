package org.solyton.solawi.bid.module.shares.component.modal


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
import org.solyton.solawi.bid.module.style.form.textInputDesktopStyle
import org.solyton.solawi.bid.module.values.ProviderId
import org.w3c.dom.HTMLElement


@Markup
@Suppress("FunctionName")
fun UpsertShareTypeModal(
    id: Int,
    texts: Lang.Block,
    modals: Storage<Modals<Int>>,
    storage: Storage<ShareManagement>,
    device: Source<DeviceType>,
    providerId: ProviderId,
    shareType: ShareType?,
    setShareType: (ShareType)->Unit,
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
    var shareType by remember { mutableStateOf<ShareType?>(shareType) }
    Form {
        Field(fieldDesktopStyle) {
            Label(
                "Name",
                id = "name-of-share-type",
                labelStyle = formLabelDesktopStyle,
                isRequired = true
            )
            TextInput(shareType?.name ?: "") {
                required()
                id("name-of-share-type")
                style { textInputDesktopStyle() }
                onInput {
                    shareType = (shareType?:ShareType.default).copy(
                        providerId = providerId.value,
                        name = it.value
                    )
                    setShareType(shareType!!)
                }
            }
        }

        Field(fieldDesktopStyle) {
            Label(
                "Description",
                id = "description-of-share-type",
                labelStyle = formLabelDesktopStyle,
                isRequired = true
            )
            TextInput(shareType?.description ?: "") {
                required()
                id("description-of-share-type")
                style { textInputDesktopStyle() }
                onInput {
                    shareType = (shareType?:ShareType.default).copy(
                        providerId = providerId.value,
                        description = it.value
                    )
                    setShareType(shareType!!)
                }
            }
        }

        Field(fieldDesktopStyle) {
            Label(
                "Key",
                id = "key-of-share-type",
                labelStyle = formLabelDesktopStyle,
                isRequired = true
            )
            TextInput(shareType?.key ?: "") {
                required()
                id("key-of-share-type")
                style { textInputDesktopStyle() }
                onInput {
                    shareType = (shareType?:ShareType.default).copy(
                        providerId = providerId.value,
                        key = it.value
                    )
                    setShareType(shareType!!)
                }
            }
        }
    }
}

@Markup
fun Storage<Modals<Int>>.showUpsertShareTypeModal(
    storage: Storage<ShareManagement>,
    texts: Lang.Block,
    device: Source<DeviceType>,
    providerId: ProviderId,
    distributionPoint: ShareType? = null,
    setDistributionPoint: (ShareType)->Unit = {},
    update: ()->Unit
) = with(nextId()) {
    put(this to ModalData(
        ModalType.Dialog,
        UpsertShareTypeModal(
            this,
            texts,
            this@showUpsertShareTypeModal,

            storage,
            device,
            providerId,
            distributionPoint,
            setDistributionPoint,
            update = update
        )
    ) )
}
