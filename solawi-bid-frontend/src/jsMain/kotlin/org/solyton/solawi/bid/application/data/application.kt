// This file has been partially auto generated. 
// Please don't make any changes to the lenses.
// Feel free to add or remove annotated properties from
// the generator data class. The corresponding lenses 
// will be removed or added on the next run of the 
// lens generator. See below for more details.
package org.solyton.solawi.bid.application.data

import kotlinx.coroutines.flow.MutableSharedFlow
import org.evoleq.axioms.definition.Lensify
import org.evoleq.axioms.definition.ReadOnly
import org.evoleq.axioms.definition.ReadWrite
import org.evoleq.compose.modal.Modals
import org.evoleq.device.data.Device
import org.evoleq.ktorx.api.Api
import org.evoleq.optics.storage.MutableSharedFlowActionDispatcher
import org.solyton.solawi.bid.application.api.solawiApi
import org.solyton.solawi.bid.application.data.env.Environment
import org.solyton.solawi.bid.application.data.ui.UiStates
import org.solyton.solawi.bid.module.application.data.organizationrelation.ApplicationOrganizationRelation
import org.solyton.solawi.bid.module.application.data.userapplication.UserApplications
import org.solyton.solawi.bid.module.banking.data.bankaccount.BankAccount
import org.solyton.solawi.bid.module.banking.data.creditor.identifier.CreditorIdentifier
import org.solyton.solawi.bid.module.banking.data.fiscalyear.FiscalYear
import org.solyton.solawi.bid.module.banking.data.legalentity.LegalEntity
import org.solyton.solawi.bid.module.banking.data.sepa.SepaModule
import org.solyton.solawi.bid.module.bid.data.auction.Auction
import org.solyton.solawi.bid.module.bid.data.bidder.BidderMails
import org.solyton.solawi.bid.module.bid.data.bidround.BidRound
import org.solyton.solawi.bid.module.context.data.Context
import org.solyton.solawi.bid.module.cookie.data.CookieDisclaimer
import org.solyton.solawi.bid.module.distribution.data.distributionpoint.DistributionPoint
import org.solyton.solawi.bid.module.i18n.data.I18N
import org.solyton.solawi.bid.module.permissions.data.Permissions
import org.solyton.solawi.bid.module.permissions.data.relations.ContextRelation
import org.solyton.solawi.bid.module.process.data.processes.ProcessManager
import org.solyton.solawi.bid.module.process.data.processes.Processes
import org.solyton.solawi.bid.module.shares.data.offers.ShareOffer
import org.solyton.solawi.bid.module.shares.data.subscriptions.ShareSubscription
import org.solyton.solawi.bid.module.shares.data.types.ShareType
import org.solyton.solawi.bid.module.user.data.managed.ManagedUser
import org.solyton.solawi.bid.module.user.data.user.User

/**
 * Generator class.
 * Feel free to add or remove annotated properties from
 * the class. Make sure that they are annotated with
 * - @ReadOnly
 * - @ReadWrite
 * If you want that a property-lens will be generated
 * on the next run of the lens generator.
 * If not, just omit the annotation or annotate it with @Ignore.
 */
@Lensify data class Application (
    @ReadOnly val environment: Environment,
    @ReadOnly val api: Api = solawiApi,
    @ReadOnly override val actions: MutableSharedFlowActionDispatcher<Application> = MutableSharedFlowActionDispatcher(MutableSharedFlow()),
    @ReadWrite override val processes: Processes = Processes(),
    @ReadWrite val deviceData: Device = Device(),
    @ReadWrite val modals: Modals<Int> = mapOf(),
    @ReadWrite val i18N: I18N = I18N(),
    @ReadWrite val context: Context = Context(),
    // UI States
    @ReadWrite val uiStates: UiStates = UiStates(),
    // Cookie
    @ReadWrite val cookieDisclaimer: CookieDisclaimer = CookieDisclaimer(),
    // Suctions
    @ReadWrite val auctions: List<Auction> = listOf(),
    @ReadWrite val bidRounds: List<BidRound> = listOf(),
    // ShareManagement
    @ReadWrite val bidderMailAddresses:  BidderMails = BidderMails(),
    @ReadWrite val shareSubscriptions: List<ShareSubscription> = emptyList(),
    @ReadWrite val shareOffers: List<ShareOffer> = emptyList(),
    @ReadWrite val shareTypes: List<ShareType> = emptyList(),
    // Distribution
    @ReadWrite val distributionPoints: List<DistributionPoint> = emptyList(),

    // Banking
    @ReadWrite val bankAccounts: List<BankAccount> = emptyList(),
    @ReadWrite val fiscalYears: List<FiscalYear> = emptyList(),
    @ReadWrite val legalEntity: LegalEntity = LegalEntity.default,
    @ReadWrite val creditorIdentifier: CreditorIdentifier? = null,
    @ReadWrite val sepaModule: SepaModule = SepaModule(),
    // User
    @ReadWrite val userData: User = User(),
    @ReadWrite val managedUsers: List<ManagedUser> = listOf(),
    // Permissions
    @ReadWrite val availablePermissions: Permissions = Permissions(),
    @ReadWrite val availableApplications: List<org.solyton.solawi.bid.module.application.data.application.Application> = listOf(),

    // Applications and Modules
    @ReadWrite val personalApplications: List<org.solyton.solawi.bid.module.application.data.application.Application> = listOf(),
    @ReadWrite val personalApplicationContextRelations: List<ContextRelation> = listOf(),
    @ReadWrite val personalModuleContextRelations: List<ContextRelation> = listOf(),
    @ReadWrite val userApplications: List<UserApplications> = listOf(),
    @ReadWrite val applicationOrganizationRelations: List<ApplicationOrganizationRelation> = listOf(),
): ProcessManager<Application> {
    override fun withProcesses(processes: Processes): Application
        = copy(processes = processes)
}
