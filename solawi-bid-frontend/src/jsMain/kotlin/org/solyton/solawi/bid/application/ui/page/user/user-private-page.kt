package org.solyton.solawi.bid.application.ui.page.user

import androidx.compose.runtime.*
import org.evoleq.compose.Markup
import org.evoleq.compose.layout.Horizontal
import org.evoleq.compose.layout.Property
import org.evoleq.compose.layout.ReadOnlyProperty
import org.evoleq.compose.layout.Vertical
import org.evoleq.language.component
import org.evoleq.language.subComp
import org.evoleq.language.title
import org.evoleq.math.Reader
import org.evoleq.math.emit
import org.evoleq.math.times
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.justifyContent
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.application.data.*
import org.solyton.solawi.bid.application.data.device.mediaType
import org.solyton.solawi.bid.application.data.transform.user.userIso
import org.solyton.solawi.bid.application.service.useI18nTransform
import org.solyton.solawi.bid.application.ui.effect.LaunchComponentLookup
import org.solyton.solawi.bid.application.ui.page.user.effect.TriggerPasswordChange
import org.solyton.solawi.bid.application.ui.page.user.i18n.UserLangComponent
import org.solyton.solawi.bid.application.ui.style.page.verticalPageStyle
import org.solyton.solawi.bid.application.ui.style.wrap.Wrap
import org.solyton.solawi.bid.module.control.button.StdButton
import org.solyton.solawi.bid.module.i18n.data.language
import org.solyton.solawi.bid.module.user.data.api.ChangePassword
import org.solyton.solawi.bid.module.user.data.password
import org.solyton.solawi.bid.module.user.data.reader.changePassword
import org.solyton.solawi.bid.module.user.data.reader.personalData
import org.solyton.solawi.bid.module.user.data.reader.properties
import org.solyton.solawi.bid.module.user.data.reader.value
import org.solyton.solawi.bid.module.user.data.username
import org.solyton.solawi.bid.module.user.component.modal.showChangePasswordModal

@Markup
@Composable
@Suppress("FunctionName")
fun PrivateUserPage(storage: Storage<Application>) = Div {
    // Data
    val userData = storage * userData
    val environment = storage * environment
    val i18n = storage * i18N

    // Data / I18N
    val texts = storage * i18N * language * component(UserLangComponent.UserPrivatePage)
    val buttons = texts * subComp("buttons")
    val dialogs = texts * subComp("dialogs")

    // Effect
    LaunchComponentLookup(
        langComponent = UserLangComponent.UserPrivatePage,
        environment = Reader{ environment.read().useI18nTransform() },
        i18n = i18n,
    )

    // State
    var user by remember { mutableStateOf(ChangePassword("","")) }

    // Markup
    Vertical(verticalPageStyle) {
        Wrap {
            Horizontal(styles = { justifyContent(JustifyContent.SpaceBetween); width(100.percent) }) {
                H1 { Text((texts * title).emit()) }
                Horizontal {
                    StdButton(
                        buttons * changePassword * title,
                        (storage * deviceData * mediaType.get),
                        false
                    ) {
                        (storage * modals).showChangePasswordModal(
                            texts = dialogs * subComp("changePassword"),
                            device = storage * deviceData * mediaType.get,
                            storedPassword = (userData * password).read(),
                            setUserData = {password -> user = ChangePassword((userData * username).read() , password) },
                            cancel = {}
                        ) {
                            TriggerPasswordChange(
                                user = user,
                                storage = storage * userIso
                            )
                        }
                    }
                }
            }
        }

        Wrap {
            H2{ Text((texts * personalData * title).emit()) }
            Vertical {
                ReadOnlyProperty(Property((texts * personalData * properties * org.solyton.solawi.bid.module.user.data.reader.username * value).emit(), (userData * username).read()))

            }
        }
    }
}
