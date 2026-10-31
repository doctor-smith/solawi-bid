package org.solyton.solawi.bid.workflow.organization

import org.solyton.solawi.bid.TestUserContext
import org.solyton.solawi.bid.TestWorkflow
import org.solyton.solawi.bid.pageobjects.OrganizationManagementPage

data class AddMemberToOrganizationInput(
    val organizationName: String,
    val username: String,
    val firstname: String = "New",
    val lastname: String = "Member"
)

class AddMemberToOrganizationWorkflow(
    override val context: TestUserContext
) : TestWorkflow<AddMemberToOrganizationInput, Unit> {
    override fun TestUserContext.runWith(input: AddMemberToOrganizationInput) {
        val organizationManagementPage = OrganizationManagementPage(page)
        organizationManagementPage.addMember(
            organizationName = input.organizationName,
            username = input.username,
            firstname = input.firstname,
            lastname = input.lastname
        )
    }
}
