package org.solyton.solawi.bid.application.ui.page.user

import androidx.compose.runtime.*
import org.evoleq.compose.Markup
import org.evoleq.compose.guard.data.isLoading
import org.evoleq.compose.guard.data.withLoading
import org.evoleq.compose.layout.Horizontal
import org.evoleq.compose.routing.navigate
import org.evoleq.device.data.mediaType
import org.evoleq.language.component
import org.evoleq.language.subComp
import org.evoleq.language.title
import org.evoleq.language.tooltip
import org.evoleq.math.*
import org.evoleq.optics.lens.DeepSearch
import org.evoleq.optics.lens.FilterBy
import org.evoleq.optics.storage.ActionEnvelope
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.times
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.env.i18nEnvironment
import org.solyton.solawi.bid.application.data.i18N
import org.solyton.solawi.bid.application.data.transform.application.management.applicationManagementModule
import org.solyton.solawi.bid.application.data.transform.distribution.distributionManagementIso
import org.solyton.solawi.bid.application.data.transform.shares.shareManagementIso
import org.solyton.solawi.bid.application.data.transform.user.userIso
import org.solyton.solawi.bid.application.service.organization.importMembersFromCsv
import org.solyton.solawi.bid.application.ui.effect.LaunchComponentLookup
import org.solyton.solawi.bid.application.ui.page.application.i18n.ApplicationLangComponent
import org.solyton.solawi.bid.application.ui.page.user.i18n.OrganizationLangComponent
import org.solyton.solawi.bid.application.ui.page.user.style.actionsWrapperStyle
import org.solyton.solawi.bid.application.ui.page.user.style.listItemWrapperStyle
import org.solyton.solawi.bid.module.application.action.READ_APPLICATIONS
import org.solyton.solawi.bid.module.application.action.READ_PERSONAL_APPLICATION_ORGANIZATION_CONTEXT_RELATIONS
import org.solyton.solawi.bid.module.application.action.readApplications
import org.solyton.solawi.bid.module.application.action.readPersonalApplicationOrganizationContextRelations
import org.solyton.solawi.bid.module.application.data.management.applicationOrganizationRelations
import org.solyton.solawi.bid.module.application.data.management.availableApplications
import org.solyton.solawi.bid.module.application.i18n.ApplicationComponent
import org.solyton.solawi.bid.module.application.i18n.application
import org.solyton.solawi.bid.module.application.i18n.module
import org.solyton.solawi.bid.module.control.button.ArrowUpButton
import org.solyton.solawi.bid.module.control.button.EditButton
import org.solyton.solawi.bid.module.control.button.UploadButton
import org.solyton.solawi.bid.module.control.button.UsersButton
import org.solyton.solawi.bid.module.distribution.action.READ_DISTRIBUTION_POINTS
import org.solyton.solawi.bid.module.distribution.action.readDistributionPoints
import org.solyton.solawi.bid.module.distribution.data.distributionManagementActions
import org.solyton.solawi.bid.module.distribution.data.distributionpoint.DistributionPoint
import org.solyton.solawi.bid.module.distribution.data.management.distributionPoints
import org.solyton.solawi.bid.module.i18n.data.language
import org.solyton.solawi.bid.module.i18n.guard.onMissing
import org.solyton.solawi.bid.module.list.component.*
import org.solyton.solawi.bid.module.list.style.defaultListStyles
import org.solyton.solawi.bid.module.loading.component.Loading
import org.solyton.solawi.bid.module.page.component.Page
import org.solyton.solawi.bid.module.process.service.process.next
import org.solyton.solawi.bid.module.process.service.process.runProcesses
import org.solyton.solawi.bid.module.process.service.process.sequence
import org.solyton.solawi.bid.module.shares.action.*
import org.solyton.solawi.bid.module.shares.data.management.ShareManagement
import org.solyton.solawi.bid.module.shares.data.management.shareOffers
import org.solyton.solawi.bid.module.shares.data.management.shareSubscriptions
import org.solyton.solawi.bid.module.shares.data.mappings.ShareManagementMappings
import org.solyton.solawi.bid.module.shares.data.offers.ShareOffer
import org.solyton.solawi.bid.module.shares.data.shareManagementActions
import org.solyton.solawi.bid.module.shares.data.subscriptions.ShareSubscription
import org.solyton.solawi.bid.module.style.page.PageTitle
import org.solyton.solawi.bid.module.style.page.verticalPageStyle
import org.solyton.solawi.bid.module.style.wrap.Wrap
import org.solyton.solawi.bid.module.user.action.organization.READ_ORGANIZATIONS
import org.solyton.solawi.bid.module.user.action.organization.readOrganizations
import org.solyton.solawi.bid.module.user.action.user.GET_USERS
import org.solyton.solawi.bid.module.user.action.user.READ_USER_PROFILES
import org.solyton.solawi.bid.module.user.action.user.getUsers
import org.solyton.solawi.bid.module.user.action.user.readUserProfiles
import org.solyton.solawi.bid.module.user.component.modal.showImportMembersToOrganizationModal
import org.solyton.solawi.bid.module.user.data.*
import org.solyton.solawi.bid.module.user.data.organization.members
import org.solyton.solawi.bid.module.user.data.organization.name
import org.solyton.solawi.bid.module.user.data.user.organizations
import org.solyton.solawi.bid.module.user.i18n.Component
import org.solyton.solawi.bid.module.user.service.profile.firstAddress
import org.solyton.solawi.bid.module.user.service.profile.fullname
import org.solyton.solawi.bid.application.data.environment as appEnv


@Markup
@Composable
@Suppress("FunctionName", "CognitiveComplexMethod")
fun OrganizationPage(applicationStorage: Storage<Application>, organizationId: String) {
    val scope = rememberCoroutineScope()
    return withLoading(
        isLoading = isLoading(
            // I18N stuff
            onMissing(
                OrganizationLangComponent.OrganizationPage,
                applicationStorage * userIso * i18n.get
            ) {
                LaunchComponentLookup(
                    langComponent = OrganizationLangComponent.OrganizationPage,
                    environment = applicationStorage * userIso * environment.get,
                    i18n = (applicationStorage * userIso * i18n)
                )
            },
            *(applicationStorage * applicationManagementModule * availableApplications).read().map {
                onMissing(
                    ApplicationLangComponent.ApplicationDetails(it.name),
                    applicationStorage * i18N.get
                ){
                    LaunchComponentLookup(
                        langComponent = ApplicationLangComponent.ApplicationDetails(it.name),
                        environment = applicationStorage * appEnv * i18nEnvironment,
                        i18n = (applicationStorage * i18N)
                    )
                }
            }.toBooleanArray(),
            // Data
            *applicationStorage.runProcesses(
                scope,
                ActionEnvelope(
                    userIso * readOrganizations(),
                    READ_ORGANIZATIONS
                ),
                sequence(
                ActionEnvelope(
                        userIso * getUsers(),
                        GET_USERS,
                    ),
                    ActionEnvelope(
                        userIso * readUserProfiles(emptyList()),
                        READ_USER_PROFILES
                    ).next(
                        ActionEnvelope(
                            shareManagementIso * readShareOffers(organizationId),
                            READ_SHARE_OFFERS,
                        ),
                        ActionEnvelope(
                            shareManagementIso * readShareSubscriptions(organizationId),
                            READ_SHARE_SUBSCRIPTIONS
                        ),
                        ActionEnvelope(
                            shareManagementIso * readShareTypes(organizationId),
                            READ_SHARE_TYPES
                        ),
                        ActionEnvelope(
                            distributionManagementIso * readDistributionPoints(organizationId),
                            READ_DISTRIBUTION_POINTS
                        ),
                    )
                ),
                ActionEnvelope(
                    applicationManagementModule * readApplications,
                    READ_APPLICATIONS
                ),
                ActionEnvelope(
                    applicationManagementModule * readPersonalApplicationOrganizationContextRelations(),
                    READ_PERSONAL_APPLICATION_ORGANIZATION_CONTEXT_RELATIONS
                )
            ),

        ),
        onLoading = { Loading() }
    ) {
        val userModuleStorage = applicationStorage * userIso
        val device = userModuleStorage * deviceData * mediaType

        val organization = userModuleStorage * user * organizations * DeepSearch { it.organizationId == organizationId }
        val members = organization * members
        val memberProfilesMap = (userModuleStorage * managedUsers.get) map { user ->
            user.associateBy({ it.id }) { it.profile }
        }


        val applicationManagementStorage = applicationStorage * applicationManagementModule
        val availableApplications = applicationManagementStorage * availableApplications
        val applicationOrganizationRelations = applicationManagementStorage * applicationOrganizationRelations
        val connectedApplications = availableApplications * FilterBy { app ->
            applicationOrganizationRelations.read().any { it.applicationId == app.id && it.organizationId == organizationId }
        }

        val shareManagementStorage = applicationStorage * shareManagementIso
        // val shareManagementActions = shareManagementStorage * shareManagementActions
        val distributionManagementStorage = applicationStorage * distributionManagementIso
        // val distributionManagementActions = distributionManagementStorage * distributionManagementActions
        val shareSubscriptionsMap = shareManagementStorage * shareSubscriptions * Reader {
                shareSubscriptions: List<ShareSubscription> ->
            shareSubscriptions.groupBy { shareSubscription -> shareSubscription.userProfileId }
        }
        val shareOffersMap = shareManagementStorage * shareOffers * Reader {
                list: List<ShareOffer> -> list.associateBy { shareOffer -> shareOffer.shareOfferId }
        }

        val distributionPoints = (distributionManagementStorage * distributionPoints)
        val distributionPointsMap = distributionPoints * Reader{ distributionPoints: List<DistributionPoint> ->
            distributionPoints.associateBy ({it.name},{ it.distributionPointId })
        }

        @Suppress("UnusedPrivateProperty")
        val shareManagementMappings: Reader<ShareManagement, ShareManagementMappings?> = Reader {
                shareManagement ->
            val shareOffers = shareManagement.shareOffers.associate { offer ->
                offer.shareType.key to offer.shareOfferId
            }
            val fiscalYearId = shareManagement.shareOffers.firstOrNull()?.fiscalYear?.fiscalYearId

            when {
                fiscalYearId == null -> null
                else -> ShareManagementMappings(
                    override = false,
                    providerId = organizationId,
                    fiscalYearId = fiscalYearId,
                    shareOffers = shareOffers,
                    distributionPoints = distributionPointsMap.read()
                )
            }
        }
        // texts
        val base = applicationStorage * i18N * language * ApplicationComponent.base
        val texts = userModuleStorage * i18n * language * component(OrganizationLangComponent.OrganizationPage)
        val dialogs = texts * subComp("dialogs")
        val importMembersToOrganization = dialogs * subComp("importMembersToOrganization")
        val listOfMembers = texts * subComp("listOfMembers")
        val listOfMembersHeaders = texts * subComp("listOfMembers") * subComp("headers")

        val listOfConnectedApplications = texts * subComp("listOfConnectedApplications")
        val listOfConnectedApplicationsHeaders = listOfConnectedApplications * subComp("headers")
        val listOfConnectedApplicationsActions = listOfConnectedApplications * subComp("actions")

        Page(verticalPageStyle) {
            Wrap {
                Horizontal({
                    justifyContent(JustifyContent.SpaceBetween)
                    alignItems(AlignItems.Center)
                }) {
                    PageTitle(organization * name.get)
                    Horizontal {
                        ArrowUpButton(
                            Color.black,
                            Color.white,
                            texts * subComp("actions") * subComp("navToManagementPage") * tooltip,
                            { device.read() }
                        ) {
                            navigate("/app/management/organizations")
                        }
                    }
                }
            }

            ListWrapper({
                defaultListStyles.listWrapper(this)
                overflowX("auto")
            }) {
                var open by remember { mutableStateOf(false) }
                TitleWrapper {
                    Title { H3{ Text((listOfMembers * title).emit()) }}
                    SimpleUpDown(open, {open = !open})


                }
                if(open) {
                    HeaderWrapper {
                        Header {
                            HeaderCell(listOfMembersHeaders * Component.standard * title) { width(30.percent) }
                            HeaderCell(listOfMembersHeaders * Component.userProfile * title) { width(30.percent) }
                            HeaderCell("Solawi Anteile | Status") { width(40.percent) }
                        }
                        ActionsWrapper({
                            defaultListStyles.actionsWrapper(this)
                            alignSelf(AlignSelf.FlexEnd)
                        }){
                            var csv: String? by remember { mutableStateOf<String?>(null) }
                            UploadButton(
                                color = Color.black,
                                bgColor = Color.white,
                                texts = listOfMembers * Component.actions * Component.importMembersToOrganization * tooltip,
                                deviceType = { device.read() }
                            ) {
                                (userModuleStorage * userModals).showImportMembersToOrganizationModal(
                                    texts = importMembersToOrganization.emit(),
                                    device = { device.read() },
                                    csv = csv,
                                    setCsv = {csv = it},
                                    isOkButtonDisabled = {csv == null}
                                ) {
                                    requireNotNull(csv){ "CSV Sting is empty! "}
                                    applicationStorage.importMembersFromCsv(
                                        scope,
                                        organizationId,
                                        csv!!, ';',
                                        (shareManagementStorage * shareManagementMappings).emit()
                                    )
                                }
                            }
                        }
                    }
                    HeaderWrapper {
                        Header {
                            // User / Memeber
                            HeaderCell(listOfMembersHeaders * Component.standard * Component.username * title) {
                                width(10.percent); overflow("hidden")
                            }
                            HeaderCell(listOfMembersHeaders * Component.standard * Component.roles * title) { width(20.percent) }
                            HeaderCell(listOfMembersHeaders * Component.userProfile * Component.name * title) { width(10.percent) }
                            HeaderCell(listOfMembersHeaders * Component.userProfile * Component.address * title) { width(20.percent) }
                            // Vegi
                            HeaderCell("Gemüse") { width(10.percent) }
                            HeaderCell("Status") { width(10.percent) }
                            // Eggs
                            HeaderCell("Eier") { width(10.percent) }
                            HeaderCell("Status") { width(10.percent) }

                        }
                    }
                    ListItemsIndexed(members) { index, member ->
                        ListItemWrapper({
                            listItemWrapperStyle(this, index)
                        }) {
                            DataWrapper {
                                TextCell(member.username) {
                                    width(10.percent); minWidth(10.percent); overflow("hidden")
                                }
                                TextCell(member.roles.joinToString(", ") { it.roleName }) {
                                    width(20.percent);minWidth(10.percent); overflow("hidden")
                                }

                                val userProfile = (memberProfilesMap * Get(member.memberId)).emit()
                                TextCell(userProfile.fullname()) {
                                    width(10.percent);minWidth(10.percent);overflow("hidden")
                                }

                                TextCell(userProfile.firstAddress()) {
                                    width(20.percent); minWidth(20.percent);overflow("hidden")
                                }
                                // Shares
                                val userShareSubscriptions = shareSubscriptionsMap.read()[userProfile?.userProfileId] ?: emptyList()
                                // Vegi
                                val vegiShare = userShareSubscriptions.firstOrNull{ subscription ->
                                    val shareOffer = shareOffersMap.read()[subscription.shareOfferId]!!
                                    shareOffer.shareType.key == "vegi"
                                }
                                NumberCell(vegiShare?.numberOfShares?: 0) { width(10.percent) }
                                TextCell(vegiShare?.status?.toString()?:"---") { width(10.percent) }
                                // Eggs
                                val eggsShare = userShareSubscriptions.firstOrNull{ subscription ->
                                    val shareOffer = shareOffersMap.read()[subscription.shareOfferId]!!
                                    shareOffer.shareType.key == "eggs"
                                }
                                NumberCell(eggsShare?.numberOfShares?: 0) { width(10.percent) }
                                TextCell(eggsShare?.status?.toString()?: "---") { width(10.percent) }
                            }
                            ActionsWrapper({
                                actionsWrapperStyle(this)
                            }) {
                                EditButton(
                                    Color.black,
                                    Color.white,
                                    listOfMembers * Component.actions * Component.edit * tooltip,
                                    { device.read() }
                                ) {
                                    navigate("/app/management/user/${member.username}")
                                }
                            }
                        }
                    }
                }
            }

            ListWrapper({
                defaultListStyles.listWrapper(this)
            }) {
                var open by remember { mutableStateOf(false) }
                TitleWrapper {
                    Title { H3{ Text((listOfConnectedApplications * title).emit()) }}
                    SimpleUpDown(open, {open = !open})
                }
                if(open) {
                    HeaderWrapper {
                        Header {
                            HeaderCell(listOfConnectedApplicationsHeaders * subComp("application") * title ) { width(40.percent) }
                            HeaderCell(listOfConnectedApplicationsHeaders * subComp("modules") * title) { width(40.percent) }
                        }
                    }
                    ListItemsIndexed(connectedApplications) { index, application ->
                        ListItemWrapper({
                            listItemWrapperStyle(this, index)
                        }) {
                            DataWrapper {
                                TextCell(base * application(application.name) * title) { width(40.percent) }
                                TextCell(application.modules.joinToString(", ") {
                                    (base * module(application.name, it.name) * title).emit()
                                }) { width(40.percent) }
                            }
                            ActionsWrapper({
                                actionsWrapperStyle(this)
                            }) {
                                UsersButton(
                                    Color.black,
                                    Color.white,
                                    listOfConnectedApplicationsActions * subComp("manageUserPermissions") * tooltip,
                                    { device.read() }
                                ) {
                                    navigate("/app/management/private/application/${application.id}/organization/$organizationId")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

