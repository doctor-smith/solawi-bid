package org.solyton.solawi.bid.module.user.action.organization

import org.evoleq.math.dispatch
import org.evoleq.math.emit
import org.evoleq.optics.lens.FirstBy
import org.evoleq.optics.lens.Lens
import org.evoleq.optics.lens.times
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.application.data.transform.user.userIso
import org.solyton.solawi.bid.module.permission.data.api.ApiRole
import org.solyton.solawi.bid.module.user.data.Application
import org.solyton.solawi.bid.module.user.data.api.organization.*
import org.solyton.solawi.bid.module.user.data.organization.Organization
import org.solyton.solawi.bid.module.user.data.transform.toDomainType
import org.solyton.solawi.bid.module.user.data.user
import org.solyton.solawi.bid.module.user.data.user.organizations
import org.solyton.solawi.bid.test.storage.SimpleTestStorage
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

    @Test
    fun readOrganizationsTest() {
        val readOrganizations = readOrganizations()
        val storage = SimpleTestStorage()

        assertIs<ReadOrganizations>((storage * userIso * readOrganizations.reader).emit())

        (storage * userIso * readOrganizations.writer).dispatch(apiOrganizations)
        val storedOrganizations = (storage * userIso * user * organizations).read()
        assertEquals(2, storedOrganizations.size)
        val expected = listOf(
            Organization("1", "1", "1"),
            Organization("2", "2", "2")
        )
        assertEquals(expected, storedOrganizations)
    }

    @Test
    fun createOrganizationTest() {
        val organizationName = "0"
        val createOrganization = createOrganization(organizationName)
        val storage = SimpleTestStorage()

        assertIs<CreateOrganization>((storage * userIso * createOrganization.reader).emit())

        (storage * userIso * readOrganizations().writer).dispatch(apiOrganizations)

        (storage * userIso * createOrganization.writer).dispatch(ApiOrganization("0", "ß", "0", listOf(), listOf()))
        val expected = listOf(
            Organization("1", "1", "1"),
            Organization("2", "2", "2"),
            Organization("0", "ß", "0"),
        )
        val result = (storage * userIso * user * organizations).read()
        assertEquals(expected, result)
    }

    @Test
    fun createChildOrganizationsTest() {
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
        val storage = SimpleTestStorage()

        (storage * userIso * createOrganization("parent").writer).dispatch(parentOrganization)
        assertIs<CreateChildOrganization>((storage * userIso * createChildOrganization.reader).emit())

        val newOrganization = ApiOrganization("0", "0", "0", listOf(), listOf())
        (storage * userIso * createChildOrganization.writer).dispatch(newOrganization)
        val storedOrganizations = (storage * userIso * user * organizations).read()
        assertEquals(1, storedOrganizations.size)
        assertEquals(parentOrganization.copy(subOrganizations = listOf(newOrganization)).toDomainType(), storedOrganizations.first())
    }

    @Test
    fun updateOrganizationsTest() {
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
        val storage = SimpleTestStorage()

        (storage * userIso * createOrganization("organization").writer).dispatch(myOrganization)
        assertIs<UpdateOrganization>((storage * userIso * updateOrganization.reader).emit())

        val updatedOrganization = myOrganization.copy(name = "new-organization")
        (storage * userIso * updateOrganization.writer).dispatch(updatedOrganization)

        val storedOrganization = (storage * userIso * organizationLens.get).emit()
        val expected = updatedOrganization.toDomainType()
        assertEquals(expected, storedOrganization)
    }

    @Test
    fun addMemberTest() {
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
                    "username",
                    listOf(
                        ApiRole("0", "0", "0", listOf())
                    )
                )
            )
        )
        val organizationLens: Lens<Application, Organization> = user * organizations * FirstBy {
                organization: Organization -> organization.organizationId == myOrganization.id
        }

        val action = addMember("0", listOf("0"), organizationLens)
        val storage = SimpleTestStorage()

        (storage * userIso * createOrganization("organization").writer).dispatch(myOrganization)
        assertIs<AddMember>((storage * userIso * action.reader).emit())

        (storage * userIso * action.writer).dispatch(updatedOrganization)
        val storedOrganization = (storage * userIso * organizationLens).read()
        assertEquals(updatedOrganization.toDomainType(), storedOrganization)
    }

    @Test
    fun updateMemberTest() {
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
                    "username",
                    listOf(
                        ApiRole("0", "0", "0", listOf())
                    )
                )
            )
        )
        val organizationLens: Lens<Application, Organization> = user * organizations * FirstBy {
                organization: Organization -> organization.organizationId == myOrganization.id
        }

        val action = updateMember("0", listOf("0"), organizationLens)
        val storage = SimpleTestStorage()

        (storage * userIso * createOrganization("organization").writer).dispatch(myOrganization)
        assertIs<UpdateMember>((storage * userIso * action.reader).emit())

        (storage * userIso * action.writer).dispatch(updatedOrganization)
        val storedOrganization = (storage * userIso * organizationLens).read()
        assertEquals(updatedOrganization.toDomainType(), storedOrganization)
    }

    @Test
    fun removeMemberTest() {
        val myOrganization = ApiOrganization(
            "organization_id",
            "organization",
            "context_id",
            listOf(),
            listOf(ApiMember(
                "0",
                "username",
                listOf(
                    ApiRole("0", "0", "0", listOf())
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
        val storage = SimpleTestStorage()

        (storage * userIso * createOrganization("organization").writer).dispatch(myOrganization)
        assertIs<RemoveMember>((storage * userIso * action.reader).emit())

        (storage * userIso * action.writer).dispatch(updatedOrganization)
        val storedOrganization = (storage * userIso * organizationLens).read()
        assertEquals(updatedOrganization.toDomainType(), storedOrganization)
    }

    @Test
    fun deleteOrganizationTest() {
        val myOrganization = ApiOrganization(
            "organization_id",
            "organization",
            "context_id",
            listOf(),
            listOf(ApiMember(
                "0",
                "username",
                listOf(
                    ApiRole("0", "0", "0", listOf())
                )
            ))
        )

        val apiOrganizations = ApiOrganizations(listOf(myOrganization))
        val action = deleteOrganization("0")
        val storage = SimpleTestStorage()

        assertIs<DeleteOrganization>((storage * userIso * action.reader).emit())

        (storage * userIso * action.writer).dispatch(apiOrganizations)
        val storedOrganization = (storage * userIso * user * organizations).read()
        assertEquals(apiOrganizations.toDomainType(), storedOrganization)
    }
}
