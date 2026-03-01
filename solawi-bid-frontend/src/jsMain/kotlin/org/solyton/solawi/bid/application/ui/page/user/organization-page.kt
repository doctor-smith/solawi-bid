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
import org.evoleq.optics.lens.FirstBy
import org.evoleq.optics.storage.ActionEnvelope
import org.evoleq.optics.storage.Read
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.dispatch
import org.evoleq.optics.storage.filter
import org.evoleq.optics.storage.times
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.env.i18nEnvironment
import org.solyton.solawi.bid.application.data.i18N
import org.solyton.solawi.bid.application.data.mainActions
import org.solyton.solawi.bid.application.data.mainModales
import org.solyton.solawi.bid.application.data.modals
import org.solyton.solawi.bid.application.data.transform.application.management.applicationManagementModule
import org.solyton.solawi.bid.application.data.transform.banking.bankingApplicationIso
import org.solyton.solawi.bid.application.data.transform.distribution.distributionManagementIso
import org.solyton.solawi.bid.application.data.transform.shares.shareManagementIso
import org.solyton.solawi.bid.application.data.transform.user.userIso
import org.solyton.solawi.bid.application.service.organization.importMembersFromCsv
import org.solyton.solawi.bid.application.ui.component.organization.showUpdateMembersOfOrganizationModal
import org.solyton.solawi.bid.application.ui.effect.LaunchComponentLookup
import org.solyton.solawi.bid.application.ui.page.application.i18n.ApplicationLangComponent
import org.solyton.solawi.bid.application.ui.page.user.action.Change
import org.solyton.solawi.bid.application.ui.page.user.action.memberCreateAction
import org.solyton.solawi.bid.application.ui.page.user.action.memberUpdateAction
import org.solyton.solawi.bid.application.ui.page.user.i18n.CountryLangComponent
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
import org.solyton.solawi.bid.module.banking.action.READ_BANK_ACCOUNTS
import org.solyton.solawi.bid.module.banking.action.readBankAccounts
import org.solyton.solawi.bid.module.banking.data.application.BankingApplication
import org.solyton.solawi.bid.module.banking.data.bankaccount.BankAccount
import org.solyton.solawi.bid.module.banking.data.mappings.BankingMappings
import org.solyton.solawi.bid.module.control.button.*
import org.solyton.solawi.bid.module.distribution.action.READ_DISTRIBUTION_POINTS
import org.solyton.solawi.bid.module.distribution.action.readDistributionPoints
import org.solyton.solawi.bid.module.distribution.data.distributionpoint.DistributionPoint
import org.solyton.solawi.bid.module.distribution.data.management.distributionPoints
import org.solyton.solawi.bid.module.i18n.data.language
import org.solyton.solawi.bid.module.i18n.guard.onMissing
import org.solyton.solawi.bid.module.list.component.*
import org.solyton.solawi.bid.module.list.style.defaultListStyles
import org.solyton.solawi.bid.module.loading.component.Loading
import org.solyton.solawi.bid.module.page.component.Page
import org.solyton.solawi.bid.module.pagination.component.Pagination
import org.solyton.solawi.bid.module.pagination.data.PaginationData
import org.solyton.solawi.bid.module.pagination.service.paginate
import org.solyton.solawi.bid.module.process.service.process.next
import org.solyton.solawi.bid.module.process.service.process.runProcesses
import org.solyton.solawi.bid.module.process.service.process.sequence
import org.solyton.solawi.bid.module.search.component.SearchInput
import org.solyton.solawi.bid.module.search.component.SearchInputStyles
import org.solyton.solawi.bid.module.shares.action.*
import org.solyton.solawi.bid.module.shares.data.internal.ChangedBy
import org.solyton.solawi.bid.module.shares.data.management.ShareManagement
import org.solyton.solawi.bid.module.shares.data.management.shareOffers
import org.solyton.solawi.bid.module.shares.data.management.shareSubscriptions
import org.solyton.solawi.bid.module.shares.data.mappings.ShareManagementMappings
import org.solyton.solawi.bid.module.shares.data.offers.ShareOffer
import org.solyton.solawi.bid.module.shares.data.shareManagementActions
import org.solyton.solawi.bid.module.shares.data.subscriptions.ShareSubscription
import org.solyton.solawi.bid.module.shares.data.subscriptions.ShareSubscriptions
import org.solyton.solawi.bid.module.shares.i18n.ShareManagementLangComponent
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
import org.solyton.solawi.bid.module.user.data.api.userprofile.UserProfileToImport
import org.solyton.solawi.bid.module.user.data.managed.ManagedUser
import org.solyton.solawi.bid.module.user.data.member.Member
import org.solyton.solawi.bid.module.user.data.organization.members
import org.solyton.solawi.bid.module.user.data.organization.name
import org.solyton.solawi.bid.module.user.data.profile.UserProfile
import org.solyton.solawi.bid.module.user.data.user.organizations
import org.solyton.solawi.bid.module.user.data.user.username
import org.solyton.solawi.bid.module.user.i18n.Component
import org.solyton.solawi.bid.module.user.service.profile.firstAddress
import org.solyton.solawi.bid.module.user.service.profile.fullname
import org.solyton.solawi.bid.module.values.LegalEntityId
import org.solyton.solawi.bid.module.values.ProviderId
import org.solyton.solawi.bid.module.values.UserId
import org.solyton.solawi.bid.module.values.Username
import kotlin.collections.List
import kotlin.collections.any
import kotlin.collections.associate
import kotlin.collections.associateBy
import kotlin.collections.contains
import kotlin.collections.distinct
import kotlin.collections.emptyList
import kotlin.collections.firstOrNull
import kotlin.collections.flatten
import kotlin.collections.get
import kotlin.collections.groupBy
import kotlin.collections.joinToString
import kotlin.collections.map
import kotlin.collections.toBooleanArray
import kotlin.collections.toMap
import kotlin.text.contains
import kotlin.text.isNotBlank
import kotlin.text.trim
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
            onMissing(
                CountryLangComponent.Countries,
                applicationStorage * userIso * i18n.get
            ) {
                LaunchComponentLookup(
                    langComponent = CountryLangComponent.Countries,
                    environment = applicationStorage * userIso * environment.get,
                    i18n = (applicationStorage * userIso * i18n)
                )
            },
            onMissing(
                ShareManagementLangComponent.Base,
                applicationStorage * userIso * i18n.get
            ) {
                LaunchComponentLookup(
                    langComponent = ShareManagementLangComponent.Base,
                    environment = applicationStorage * userIso * environment.get,
                    i18n = (applicationStorage * userIso * i18n)
                )
            },
            *arrayOf("DE", "AT", "CH").map { countryCode ->
                onMissing(
                    CountryLangComponent.StateOrProvince(countryCode),
                    applicationStorage * userIso * i18n.get
                ) {
                    LaunchComponentLookup(
                        langComponent = CountryLangComponent.StateOrProvince(countryCode),
                        environment = applicationStorage * userIso * environment.get,
                        i18n = (applicationStorage * userIso * i18n)
                    )
                }
            }.toBooleanArray(),
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
                ActionEnvelope(
                    userIso * readOrganizations(),
                    READ_ORGANIZATIONS,
                ),
                ActionEnvelope(
                    bankingApplicationIso * readBankAccounts(LegalEntityId(organizationId)),
                    READ_BANK_ACCOUNTS,
                ),
                sequence(
                ActionEnvelope(
                        userIso * getUsers(),
                        GET_USERS,
                    ),
                    ActionEnvelope(
                        userIso * readUserProfiles(emptyList()),
                        READ_USER_PROFILES,
                    ).next(
                        ActionEnvelope(
                            shareManagementIso * readShareOffers(organizationId),
                            READ_SHARE_OFFERS,
                        ),
                        ActionEnvelope(
                            shareManagementIso * readShareSubscriptions(organizationId),
                            READ_SHARE_SUBSCRIPTIONS,
                        ),
                        ActionEnvelope(
                            shareManagementIso * readShareTypes(organizationId),
                            READ_SHARE_TYPES,
                        ),
                        ActionEnvelope(
                            distributionManagementIso * readDistributionPoints(organizationId),
                            READ_DISTRIBUTION_POINTS,
                        ),
                    )
                ),
                ActionEnvelope(
                    applicationManagementModule * readApplications,
                    READ_APPLICATIONS,
                ),
                ActionEnvelope(
                    applicationManagementModule * readPersonalApplicationOrganizationContextRelations(),
                    READ_PERSONAL_APPLICATION_ORGANIZATION_CONTEXT_RELATIONS,
                )
            ),
        ),
        onLoading = { Loading() }
    ) {



        val userModuleStorage = applicationStorage * userIso
        val device = userModuleStorage * deviceData * mediaType

        val currentUsername = userModuleStorage * user * username.get
        val currentUser = Read(userModuleStorage * managedUsers * FirstBy {
            it.username == currentUsername.read()
        })

        val organization = userModuleStorage * user * organizations * DeepSearch { it.organizationId == organizationId }
        val members = organization * members
        val memberProfilesMap = (userModuleStorage * managedUsers.get) map { users: List<ManagedUser> ->
            users.associateBy({ it.id }) { it.profile }
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
                    fiscalYears = shareManagement.shareOffers.map { it.fiscalYear }.distinct(),
                    shareOffers = shareManagement.shareOffers,
                    distributionPoints = distributionPointsMap.read()
                )
            }
        }
        val bankingApplicationStorage = applicationStorage * bankingApplicationIso
        val bankingMappings: Reader<BankingApplication, BankingMappings> = Reader {
            bankingApplication ->
            BankingMappings(
                override = false,
                LegalEntityId(organizationId),
                bankingApplication.bankAccounts.associateBy { it.userId }
            )
        }
        // texts
        val base = applicationStorage * i18N * language * ApplicationComponent.base
        val texts = userModuleStorage * i18n * language * component(OrganizationLangComponent.OrganizationPage)
        val dialogs = texts * subComp("dialogs")
        val importMembersToOrganization = dialogs * subComp("importMembersToOrganization")
        val updateMemberOfOrganization = dialogs * subComp("updateMemberOfOrganization")
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

            var memberFilter by remember { mutableStateOf<(Member) -> Boolean>({true}) }
            var paginationState by remember { mutableStateOf(
                PaginationData(
                    members.read().size,
                    1,
                    20,
                    10,
                    10
                )
            ) }

            ListWrapper({
                defaultListStyles.listWrapper(this)
                overflowX("auto")
            }) {
                var open by remember { mutableStateOf(false) }
                TitleWrapper {
                    Title { H3{ Text((listOfMembers * title).emit()) }}
                    SimpleUpDown(open, {open = !open})
                    ActionsWrapper({
                        defaultListStyles.actionsWrapper(this)
                        width(70.percent)
                        alignSelf(AlignSelf.FlexEnd)
                    }) {
                        Pagination(
                            data = paginationState,
                            setNumberOfItemsPerPage = {
                                paginationState = paginationState.copy(
                                    itemsPerPage = it,
                                )
                            }
                        ) {
                                newPage  ->
                            paginationState = paginationState.copy(
                                page = newPage,
                            )
                        }
                        // Search by names of user profiles and co-subscribers
                        val searchMemberProfiles = memberProfilesMap.read()
                        val searchCoSubscribers = searchMemberProfiles.map { entry ->
                            entry.key to shareSubscriptionsMap.read()[entry.value?.userProfileId]
                                ?.map { it.coSubscribers }
                                ?.flatten()
                                ?.distinct()
                                ?.joinToString(", ") { it }
                        }.toMap()


                        SearchInput(
                            "",
                            SearchInputStyles()
                        ) {
                            val newSearchInputState = it.trim()
                            memberFilter = if (newSearchInputState.isNotBlank()){
                                { member ->
                                    member.username.contains(newSearchInputState)
                                            || searchMemberProfiles[member.memberId]?.fullname()?.contains(newSearchInputState) ?: false
                                            || searchCoSubscribers.contains(newSearchInputState)
                                }
                            } else {
                                { true }
                            }
                        }

                        // Import csv of members
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
                                    (shareManagementStorage * shareManagementMappings).emit(),
                                    (bankingApplicationStorage * bankingMappings).emit()
                                )
                            }
                        }
                        val shareOffers = (shareManagementStorage * shareOffers.get)
                        var usernameState by remember { mutableStateOf<Username?>(null) }
                        var userProfileState by remember { mutableStateOf<UserProfile?>(null) }
                        var importUserProfileState by remember { mutableStateOf<UserProfileToImport?>(null) }
                        var bankAccountState by remember { mutableStateOf<BankAccount?>(null) }
                        var shareSubscriptionsState by remember { mutableStateOf<ShareSubscriptions?>(null) }
                        // todo:dev sync module state when new user has been stored
                        var userId by remember { mutableStateOf<UserId?>(null) }
                        LaunchedEffect(importUserProfileState) {
                            importUserProfileState?.let { profile ->
                                val user = (userModuleStorage * managedUsers).read().firstOrNull {
                                    it.username == profile.username
                                }
                                userId = user?.id?.let { UserId(it) }
                            }
                        }
                        /*
                        val userId by produceState<UserId?>(initialValue = null) {
                            importUserProfileState?.let { profile ->
                                val user = (userModuleStorage * managedUsers).read().firstOrNull {
                                    it.username == profile.username
                                }
                                value = user?.id?.let { UserId(it) }
                            }
                        }
                         */

                        PlusButton(
                            color = Color.black,
                            bgColor = Color.white,
                            texts = listOfMembers * Component.actions * Component.create * tooltip,
                            deviceType = { device.read() }
                        ) {

                            (applicationStorage * modals).showUpdateMembersOfOrganizationModal(
                                texts = updateMemberOfOrganization,
                                device = {device.read ()},
                                actions = (applicationStorage * mainActions).read(),
                                changesDoneBy = ChangedBy.PROVIDER,
                                currentUser = currentUser.emit(),
                                organizationId = ProviderId(organizationId),
                                username = importUserProfileState?.username?.let { Username(it) },
                                setUsername = {usernameState = it},
                                userProfile = userId?.let { userProfileState },
                                setUserProfile = {userProfileState = it},
                                importUserProfile = { userProfileToImport ->
                                    usernameState = Username(userProfileToImport.username)
                                    importUserProfileState = userProfileToImport

                                    val actions = memberCreateAction(
                                        providerId= ProviderId(organizationId),
                                        username = usernameState!!,
                                        userProfileChange = Change(null, userProfileState),
                                        bankAccountChange = Change(null, null),
                                        shareSubscriptionsChange = Change(null, null)
                                    )
                                    applicationStorage.runProcesses(
                                        scope,
                                        *actions
                                    )
                                },
                                bankAccount = null,
                                setBankAccount = {bankAccountState = it},
                                distributionPoints = distributionPoints.read(),
                                shareOffers = shareOffers.emit(),
                                shareSubscriptions = null,
                                setShareSubscriptions = {shareSubscriptionsState = it},
                                updateShareStatus = {
                                        data -> shareManagementStorage * shareManagementActions dispatch updateShareStatus(data)
                                },
                                isOkButtonDisabled = {false}
                            ) {
                                val actions = memberCreateAction(
                                    providerId= ProviderId(organizationId),
                                    username = usernameState!!,
                                    userProfileChange = Change(null, userProfileState),
                                    bankAccountChange = Change(null, bankAccountState),
                                    shareSubscriptionsChange = Change(null, shareSubscriptionsState)
                                )
                                applicationStorage.runProcesses(
                                    scope,
                                    *actions
                                )
                            }
                        }
                    }
                }
                if(open) {
                    /*
                    HeaderWrapper {

                    }

                     */
                    HeaderWrapper {
                        Header {
                            HeaderCell(listOfMembersHeaders * Component.standard * title) { width(30.percent) }
                            HeaderCell(listOfMembersHeaders * Component.userProfile * title) { width(30.percent) }
                            HeaderCell("Solawi Anteile | Status") { width(40.percent) }
                        }
                        /*
                        ActionsWrapper({
                            defaultListStyles.actionsWrapper(this)
                            alignSelf(AlignSelf.FlexEnd)
                        }){
                            // fjdlkajfkldajflda
                        }

                         */
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
                    ListItemsIndexed(
                        members
                            .filter(memberFilter)
                            .paginate(paginationState.itemsPerPage, paginationState.page)
                    ) { index, member ->
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
                                val userProfile = (memberProfilesMap * Get(member.memberId)).emit()
                                var usernameState by remember { mutableStateOf(Username(member.username)) }
                                var userProfileState by remember { mutableStateOf(userProfile) }

                                val bankAccount = (bankingApplicationStorage * bankingMappings).read().bankAccounts[UserId(member.memberId)]
                                var bankAccountState by remember { mutableStateOf(bankAccount) }

                                val shareOffers = (shareManagementStorage * shareOffers.get).emit()
                                val shareSubscriptions = shareSubscriptionsMap.read()[userProfile?.userProfileId].wrapOrNull {
                                        list -> ShareSubscriptions(list)
                                }
                                var shareSubscriptionsState by remember { mutableStateOf(shareSubscriptions) }

                                EditButton(
                                    Color.black,
                                    Color.white,
                                    listOfMembers * Component.actions * Component.edit * tooltip,
                                    { device.read() }
                                ) {
                                    (applicationStorage * mainModales).showUpdateMembersOfOrganizationModal(
                                        texts = updateMemberOfOrganization,
                                        device = {device.read ()},
                                        actions = (applicationStorage * mainActions).read(),
                                        changesDoneBy = ChangedBy.PROVIDER,
                                        currentUser = currentUser.emit(),
                                        organizationId = ProviderId(organizationId),
                                        username = Username(member.username),
                                        setUsername = {usernameState = it},
                                        userProfile = userProfile,
                                        setUserProfile = {userProfileState = it},
                                        importUserProfile = {},
                                        bankAccount = bankAccount,
                                        setBankAccount = {bankAccountState = it},
                                        distributionPoints = distributionPoints.read(),
                                        shareOffers = shareOffers,
                                        shareSubscriptions = shareSubscriptions,
                                        setShareSubscriptions = {shareSubscriptionsState = it},updateShareStatus = {
                                            data -> shareManagementStorage * shareManagementActions dispatch updateShareStatus(data)
                                        },
                                        isOkButtonDisabled = {false}
                                    ) {
                                        val actions = applicationStorage.memberUpdateAction(
                                            member = {member},
                                            usernameChange = Change(Username(member.username), usernameState),
                                            userProfileChange = Change(userProfile, userProfileState),
                                            bankAccountChange = Change(bankAccount, bankAccountState),
                                            shareSubscriptionsChange = Change(shareSubscriptions, shareSubscriptionsState)
                                        )
                                        applicationStorage.runProcesses(
                                            scope,
                                            *actions
                                        )
                                    }
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

