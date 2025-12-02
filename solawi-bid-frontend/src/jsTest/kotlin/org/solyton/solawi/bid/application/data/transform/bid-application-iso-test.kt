package org.solyton.solawi.bid.application.data.transform

import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.env.Environment
import org.solyton.solawi.bid.application.data.transform.bid.bidApplicationIso
import org.solyton.solawi.bid.module.user.data.organization.Organization
import org.solyton.solawi.bid.module.user.data.user.User
import kotlin.test.Test
import kotlin.test.assertEquals

class BidApplicationIsoTest {
    @Test fun getterTest() {
        val organizations = listOf(
            Organization(
                "id",
                "orga_1",
                "context_1",
                listOf()
            )
        )

        val application = Application(
            environment = Environment(),
            userData = User(
                organizations = organizations
            )
        )

        val bidApplication = bidApplicationIso.get (application)
        assertEquals(organizations, bidApplication.user.organizations)
    }
}
