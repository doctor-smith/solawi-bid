package org.solyton.solawi.bid.workflow.shares

import org.solyton.solawi.bid.TestUserContext
import org.solyton.solawi.bid.TestWorkflow
import org.solyton.solawi.bid.module.distribution.data.api.ApiDistributionPoint
import org.solyton.solawi.bid.module.user.data.api.organization.Organization
import org.solyton.solawi.bid.module.user.data.api.userprofile.UserProfile
import org.solyton.solawi.bid.module.values.Firstname
import org.solyton.solawi.bid.module.values.Lastname
import org.solyton.solawi.bid.run
import org.solyton.solawi.bid.withInput
import org.solyton.solawi.bid.workflow.banking.*
import org.solyton.solawi.bid.workflow.distribution.SetupDistributionInput
import org.solyton.solawi.bid.workflow.distribution.SetupDistributionWorkflow
import org.solyton.solawi.bid.workflow.organization.CreateOrganizationInput
import org.solyton.solawi.bid.workflow.organization.CreateOrganizationWorkflow
import org.solyton.solawi.bid.workflow.user.CreateProfileInput
import org.solyton.solawi.bid.workflow.user.CreateProfileWorkflow
import org.solyton.solawi.bid.workflow.user.UserLoginInput
import org.solyton.solawi.bid.workflow.user.UserLoginWorkflow

data class SetupShareEnvironmentInput(
    val organizationName: String,
    val firstname: Firstname = Firstname("Jane"),
    val lastname: Lastname = Lastname("Doe"),
    val fiscalYears: List<FiscalYearPeriod>,
    val distributionPointName: String,
    val shareTypeName: String,
    val shareTypeKey: String,
    val shareTypeDescription: String = "E2E fixture share type",
    val offers: List<ShareOfferConfig>,
    val creditorBankAccount: CreditorBankAccountInput? = CreditorBankAccountInput(),
    val debtorBankAccount: DebtorBankAccountInput? = DebtorBankAccountInput()
)

data class ShareEnvironmentSetupResult(
    val userProfile: UserProfile,
    val organization: Organization,
    val banking: BankingSetupResult,
    val distributionPoint: ApiDistributionPoint,
    val shares: SharesSetupResult
)

class SetupShareEnvironmentWorkflow(
    override val context: TestUserContext
) : TestWorkflow<SetupShareEnvironmentInput, ShareEnvironmentSetupResult> {
    override fun TestUserContext.runWith(input: SetupShareEnvironmentInput): ShareEnvironmentSetupResult {
        run(UserLoginWorkflow(this)) withInput UserLoginInput()

        val memberName = "${input.firstname.value} ${input.lastname.value}"
        val userProfile = run(CreateProfileWorkflow(this)) withInput CreateProfileInput(
            firstname = input.firstname,
            lastname = input.lastname,
            recipientName = memberName,
            organizationName = input.organizationName
        )

        val organization = run(CreateOrganizationWorkflow(this)) withInput CreateOrganizationInput(
            organizationName = input.organizationName
        )

        val banking = run(SetupBankingWorkflow(this)) withInput SetupBankingInput(
            organizationName = input.organizationName,
            fiscalYears = input.fiscalYears,
            creditorBankAccount = input.creditorBankAccount,
            debtorBankAccount = input.debtorBankAccount,
            debtorName = memberName
        )

        val distributionPoint = run(SetupDistributionWorkflow(this)) withInput SetupDistributionInput(
            organizationName = input.organizationName,
            distributionPointName = input.distributionPointName
        )

        val shares = run(SetupSharesWorkflow(this)) withInput SetupSharesInput(
            organizationName = input.organizationName,
            shareTypeName = input.shareTypeName,
            shareTypeKey = input.shareTypeKey,
            shareTypeDescription = input.shareTypeDescription,
            offers = input.offers
        )

        return ShareEnvironmentSetupResult(
            userProfile = userProfile,
            organization = organization,
            banking = banking,
            distributionPoint = distributionPoint,
            shares = shares
        )
    }
}
