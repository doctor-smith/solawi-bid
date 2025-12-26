package org.solyton.solawi.bid.application.ui.page.user

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.launch
import org.evoleq.compose.Markup
import org.evoleq.compose.guard.data.isLoading
import org.evoleq.compose.guard.data.onEmpty
import org.evoleq.compose.guard.data.withLoading
import org.evoleq.compose.layout.Horizontal
import org.evoleq.compose.routing.navigate
import org.evoleq.device.data.mediaType
import org.evoleq.optics.exception.OpticsException
import org.evoleq.optics.lens.DeepSearch
import org.evoleq.optics.lens.FirstBy
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.dispatch
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.justifyContent
import org.solyton.solawi.bid.module.control.button.ArrowUpButton
import org.solyton.solawi.bid.module.loading.component.Loading
import org.solyton.solawi.bid.module.page.component.Page
import org.solyton.solawi.bid.module.style.page.PageTitle
import org.solyton.solawi.bid.module.style.page.verticalPageStyle
import org.solyton.solawi.bid.module.user.action.organization.readOrganizations
import org.solyton.solawi.bid.module.user.data.Application
import org.solyton.solawi.bid.module.user.data.deviceData
import org.solyton.solawi.bid.module.user.data.organization.name
import org.solyton.solawi.bid.module.user.data.user
import org.solyton.solawi.bid.module.user.data.user.organizations
import org.solyton.solawi.bid.module.user.data.userActions


@Markup
@Composable
@Suppress("FunctionName")
fun OrganizationPage(storage: Storage<Application>, organizationId: String) = withLoading(
    isLoading = isLoading(
        onEmpty(
            storage * user * organizations.get
        ){
            LaunchedEffect(Unit) {
                launch {
                    (storage * userActions).dispatch(readOrganizations())
                }
            }
        }
    ),
    onLoading = { Loading() }
) {
    val device = storage * deviceData * mediaType

    val organization = storage * user * organizations * DeepSearch { it.organizationId == organizationId }

    Page(verticalPageStyle) {
        Horizontal({
            justifyContent(JustifyContent.SpaceBetween)
            alignItems(AlignItems.Center)
        }) {
            PageTitle(organization * name.get)
            Horizontal {
                ArrowUpButton(
                    Color.black,
                    Color.white,
                    {"All organizations"},
                    {device.read()}
                ) {
                    navigate("/app/management/organizations")
                }
            }
        }
    }
}
