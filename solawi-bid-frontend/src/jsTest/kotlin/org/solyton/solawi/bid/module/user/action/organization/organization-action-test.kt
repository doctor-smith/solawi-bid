package org.solyton.solawi.bid.module.user.action.organization

import org.evoleq.math.dispatch
import org.evoleq.math.emit
import org.evoleq.optics.lens.FirstBy
import org.evoleq.optics.lens.Lens
import org.evoleq.optics.lens.times
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.testutils.ComposeWebExperimentalTestsApi
import org.jetbrains.compose.web.testutils.runTest
import org.solyton.solawi.bid.application.data.transform.user.userIso
import org.solyton.solawi.bid.module.permission.data.api.ApiRole
import org.solyton.solawi.bid.module.user.data.Application
import org.solyton.solawi.bid.module.user.data.api.organization.*
import org.solyton.solawi.bid.module.user.data.organization.Organization
import org.solyton.solawi.bid.module.user.data.transform.toDomainType
import org.solyton.solawi.bid.module.user.data.user
import org.solyton.solawi.bid.module.user.data.user.organizations
import org.solyton.solawi.bid.test.storage.TestStorage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class OrganizationActionTest {
    val apiOrganizations: ApiOrganizations = ApiOrganizations(
        listOf(
            ApiOrganization(
                "1", "1", "1", listOf(), listOf()
            ),
            ApiOrganization(
                "2", "2", "2", listOf(), listOf()
            )
        )
    )


    @OptIn(ComposeWebExperimentalTestsApi::class)
    @Test fun readOrganizationsTest() = runTest {

        val readOrganizations = readOrganizations()

        composition {
            val storage = TestStorage()
            assertIs<ReadOrganizations>((storage * userIso * readOrganizations.reader).emit())

            (storage * userIso * readOrganizations.writer).dispatch(apiOrganizations)
            val storedOrganizations = (storage * userIso * user * organizations).read()
            assertEquals(2, storedOrganizations.size)
            val expected = listOf(
                Organization("1","1", "1"),
                Organization("2","2", "2")
            )
            assertEquals(expected, storedOrganizations)
        }
    }

    @OptIn(ComposeWebExperimentalTestsApi::class)
    @Test fun createOrganizationTest() = runTest {
        val organizationName = "0"
        val createOrganization = createOrganization(organizationName)

        composition {
            val storage = TestStorage()
            assertIs<CreateOrganization>((storage * userIso * createOrganization.reader).emit())

            (storage * userIso * readOrganizations().writer).dispatch(apiOrganizations)

            (storage * userIso * createOrganization.writer).dispatch(ApiOrganization("0", "ß", "0", listOf(), listOf()))
            val expected = listOf(
                Organization("0","ß", "0"),
                Organization("1","1", "1"),
                Organization("2","2", "2")
            )
            val result = (storage * userIso * user * organizations).read()
            assertEquals(expected, result)
        }
    }

    @OptIn(ComposeWebExperimentalTestsApi::class)
    @Test fun createChildOrganizationsTest() = runTest {
        val parentOrganization = ApiOrganization(
            "parent_id",
            "parent",
            "context_id",
            listOf(),
            listOf()
        )

        val organizationLens: Lens<Application, Organization> = user * organizations * FirstBy {
            organization: Organization -> organization.organizationId == parentOrganization.id
        }

        val createChildOrganization = createChildOrganization("child", organizationLens)

        composition {
            val storage = TestStorage()
            (storage * userIso * createOrganization("parent").writer).dispatch(parentOrganization)
            assertIs<CreateChildOrganization>((storage * userIso * createChildOrganization.reader).emit())

            val newOrganization = ApiOrganization("0","0","0", listOf(), listOf())
            (storage * userIso * createChildOrganization.writer).dispatch(newOrganization)
            val storedOrganizations = (storage * userIso * user * organizations).read()
            assertEquals(1, storedOrganizations.size)
            assertEquals(parentOrganization.copy(subOrganizations = listOf(newOrganization)).toDomainType(), storedOrganizations.first())
        }
    }

    @OptIn(ComposeWebExperimentalTestsApi::class)
    @Test fun updateOrganizationsTest() = runTest {
        val myOrganization = ApiOrganization(
            "organization_id",
            "organization",
            "context_id",
            listOf(),
            listOf()
        )

        val organizationLens: Lens<Application, Organization> = user * organizations * FirstBy {
                organization: Organization -> organization.organizationId == myOrganization.id
        }

        val updateOrganization = updateOrganization("organization", organizationLens)

        composition {
            val storage = TestStorage()
            (storage * userIso * createOrganization("organization").writer).dispatch(myOrganization)
            assertIs<UpdateOrganization>((storage * userIso * updateOrganization.reader).emit())

            val updatedOrganization = myOrganization.copy(name= "new-organization")
            (storage * userIso * updateOrganization.writer).dispatch(updatedOrganization)

            val storedOrganization = (storage * userIso * organizationLens.get).emit()
            val expected = updatedOrganization.toDomainType()
            assertEquals(expected, storedOrganization)
        }
    }


    @OptIn(ComposeWebExperimentalTestsApi::class)
    @Test fun addMemberTest() = runTest {
        val myOrganization = ApiOrganization(
            "organization_id",
            "organization",
            "context_id",
            listOf(),
            listOf()
        )

        val updatedOrganization = myOrganization.copy(
            members = listOf(
                ApiMember(
                    "0",
                    listOf(
                        ApiRole( "0", "0","0",listOf())
                    )
                )
            )
        )
        val organizationLens: Lens<Application, Organization> = user * organizations * FirstBy {
                organization: Organization -> organization.organizationId == myOrganization.id
        }

        val action = addMember("0",listOf("0"), organizationLens)

        composition {
            val storage = TestStorage()
            (storage * userIso * createOrganization("organization").writer).dispatch(myOrganization)
            assertIs<AddMember>((storage * userIso * action.reader).emit())

            (storage * userIso * action.writer).dispatch(updatedOrganization)
            val storedOrganization = (storage * userIso * organizationLens).read()
            assertEquals(updatedOrganization.toDomainType(), storedOrganization)
        }
    }

    @OptIn(ComposeWebExperimentalTestsApi::class)
    @Test fun updateMemberTest() = runTest {
        val myOrganization = ApiOrganization(
            "organization_id",
            "organization",
            "context_id",
            listOf(),
            listOf()
        )

        val updatedOrganization = myOrganization.copy(
            members = listOf(
                ApiMember(
                    "0",
                    listOf(
                        ApiRole( "0", "0","0",listOf())
                    )
                )
            )
        )
        val organizationLens: Lens<Application, Organization> = user * organizations * FirstBy {
                organization: Organization -> organization.organizationId == myOrganization.id
        }

        val action = updateMember("0",listOf("0"), organizationLens)

        composition {
            val storage = TestStorage()
            (storage * userIso * createOrganization("organization").writer).dispatch(myOrganization)
            assertIs<UpdateMember>((storage * userIso * action.reader).emit())

            (storage * userIso * action.writer).dispatch(updatedOrganization)
            val storedOrganization = (storage * userIso * organizationLens).read()
            assertEquals(updatedOrganization.toDomainType(), storedOrganization)
        }
    }

    @OptIn(ComposeWebExperimentalTestsApi::class)
    @Test fun removeMemberTest() = runTest {
        val myOrganization = ApiOrganization(
            "organization_id",
            "organization",
            "context_id",
            listOf(),
            listOf(ApiMember(
                "0",
                listOf(
                    ApiRole( "0", "0","0",listOf())
                )
            ))
        )

        val updatedOrganization = myOrganization.copy(
            members = listOf()
        )
        val organizationLens: Lens<Application, Organization> = user * organizations * FirstBy {
                organization: Organization -> organization.organizationId == myOrganization.id
        }

        val action = removeMember("0", organizationLens)

        composition {
            val storage = TestStorage()
            (storage * userIso * createOrganization("organization").writer).dispatch(myOrganization)
            assertIs<RemoveMember>((storage * userIso * action.reader).emit())

            (storage * userIso * action.writer).dispatch(updatedOrganization)
            val storedOrganization = (storage * userIso * organizationLens).read()
            assertEquals(updatedOrganization.toDomainType(), storedOrganization)
        }
    }
}
