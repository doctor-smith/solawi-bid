package org.solyton.solawi.bid.application.ui.page.banking

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import org.evoleq.compose.layout.Vertical
import org.evoleq.compose.link.Link
import org.evoleq.math.*
import org.evoleq.optics.lens.FilterBy
import org.evoleq.optics.storage.Read
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.dispatch
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.css.paddingBottom
import org.jetbrains.compose.web.css.paddingLeft
import org.jetbrains.compose.web.css.paddingTop
import org.jetbrains.compose.web.css.px
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.context
import org.solyton.solawi.bid.application.data.transform.application.management.applicationManagementModule
import org.solyton.solawi.bid.application.data.transform.user.userIso
import org.solyton.solawi.bid.application.data.userData
import org.solyton.solawi.bid.application.service.setContext
import org.solyton.solawi.bid.module.application.data.management.applicationOrganizationRelations
import org.solyton.solawi.bid.module.application.data.management.availableApplications
import org.solyton.solawi.bid.module.banking.application.BANKING_APPLICATION_NAME
import org.solyton.solawi.bid.module.context.data.isEmpty
import org.solyton.solawi.bid.module.page.component.Page
import org.solyton.solawi.bid.module.style.page.PageTitle
import org.solyton.solawi.bid.module.style.page.SubTitle
import org.solyton.solawi.bid.module.style.page.verticalPageStyle
import org.solyton.solawi.bid.module.style.wrap.Wrap
import org.solyton.solawi.bid.module.user.action.organization.readOrganizations
import org.solyton.solawi.bid.module.user.data.user.organizations
import org.solyton.solawi.bid.module.user.data.userActions

@Composable
fun OrganizationsWithBankingPage(storage: Storage<Application>) {
    if (storage * context * isEmpty() * Emit) return@OrganizationsWithBankingPage

    val scope = rememberCoroutineScope()

    storage.setContext("APPLICATION")

    val applicationManagementStorage = storage * applicationManagementModule
    val applicationIds = Read(applicationManagementStorage * availableApplications) map {
            list ->list.filter{it.name == BANKING_APPLICATION_NAME}.map{it.id}
    }

    val applicationOrganizationRelations = applicationManagementStorage * applicationOrganizationRelations
    val organizationIds = Read(applicationOrganizationRelations) map { relations ->
        relations.filter { it.applicationId in applicationIds }.map { it.organizationId }
    }
    val organizations = Read(storage * userData * organizations * FilterBy{org -> org.organizationId in organizationIds})

    LaunchedEffect(Unit) {
        scope.launch {
            storage * userIso * userActions dispatch readOrganizations()
        }
    }

    Page(verticalPageStyle) {
        Wrap{
            PageTitle("Organisationen mit Bank Applikation")
            SubTitle("Liste aller Organisationen in denen der aktuelle Nutzer Bank Geschäfte erledigen darf")
        }
        Wrap{
            Vertical {
                organizations.emit().forEach { organization ->
                    Link(
                        organization.name,
                        "/app/management/organizations/${organization.organizationId}/${BANKING_APPLICATION_NAME.lowercase()}"
                    ) {
                        paddingLeft(10.px)
                        paddingTop(5.px)
                        paddingBottom(5.px)
                    }
                }
            }
        }
    }
}
