package org.solyton.solawi.bid.module.distribution.repository

import org.evoleq.exposedx.test.runSimpleH2Test
import org.evoleq.uuid.UUID_ZERO
import org.jetbrains.exposed.sql.Transaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.solyton.solawi.bid.DbFunctional
import org.solyton.solawi.bid.module.distribution.exception.DistributionPointException
import org.solyton.solawi.bid.module.distribution.schema.DistributionPointEntity
import org.solyton.solawi.bid.module.distribution.schema.DistributionPointsTable
import org.solyton.solawi.bid.module.permission.repository.createRight
import org.solyton.solawi.bid.module.permission.repository.createRole
import org.solyton.solawi.bid.module.permission.repository.createRootContext
import org.solyton.solawi.bid.module.permission.schema.*
import org.solyton.solawi.bid.module.user.exception.AddressException
import org.solyton.solawi.bid.module.user.exception.OrganizationException
import org.solyton.solawi.bid.module.user.schema.*
import org.solyton.solawi.bid.module.user.schema.AddressEntity
import org.solyton.solawi.bid.module.user.schema.OrganizationEntity
import org.solyton.solawi.bid.module.user.schema.UserEntity
import org.solyton.solawi.bid.module.user.schema.UserProfileEntity
import org.solyton.solawi.bid.module.user.schema.repository.createRootOrganization
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class DistributionPointRepositoryTest {

    val distributionPointTables = arrayOf(
        RolesTable,
        RightsTable,
        ContextsTable,
        UserRoleContext,
        RoleRightContexts,
        UsersTable,
        UserProfilesTable,
        OrganizationsTable,
        AddressesTable,
        DistributionPointsTable
    )

    data class Setup(
        val user: UserEntity,
        val userProfile: UserProfileEntity,
        val organization: OrganizationEntity,
        val address: AddressEntity
    )

    fun Transaction.createTestRoles(vararg names: String) {
        names.forEach {
            createRole(it, "description", UUID_ZERO)
        }
    }

    fun Transaction.createTestRights(vararg names: String) {
        names.forEach {
            createRight(it, "description", UUID_ZERO)
        }
    }

    fun Transaction.setup(): Setup {
        createTestRoles("MANAGER")
        createTestRights(
            "CREATE_ORGANIZATION",
            "READ_ORGANIZATION",
            "UPDATE_ORGANIZATION",
            "DELETE_ORGANIZATION",
            "MANAGE_USERS"
        )
        createRootContext("EMPTY")
        val organization = createRootOrganization("Test Organization", UUID_ZERO)
        val user = UserEntity.new {
            createdBy = UUID_ZERO
            username = "test-user@solyton.org"
            status = UserStatus.ACTIVE
            password = "pw"
        }
        val userProfile = UserProfileEntity.new {
            createdBy = UUID_ZERO
            this.user = user
            firstName = "fn"
            lastName = "ln"
        }
        val address = AddressEntity.new {
            createdBy = UUID_ZERO
            this.userProfile = userProfile
            countryCode = ""
            city = "city"
            postalCode = "jf"
            stateOrProvince = "stateOrProvince"
            recipientName = "recipientName"
            addressLine2 = ""
            addressLine1 = ""
        }

        return Setup(
            user,
            userProfile,
            organization,
            address
        )
    }

    @DbFunctional@Test
    fun createDistributionPoint() = runSimpleH2Test(*distributionPointTables){
        val (_, _, organization, address) = setup()
        val distributionPoint = createDistributionPoint(
            "Test DistributionPoint",
            address.id.value,
            organization.id.value,
            UUID_ZERO
        )

        val storedDistributionPoint = DistributionPointEntity.findById(distributionPoint.id.value)
        assertNotNull(storedDistributionPoint)
        assertEquals(distributionPoint, storedDistributionPoint)
    }

    @DbFunctional@Test
    fun createDistributionPointFailWithDuplicateName() = runSimpleH2Test(*distributionPointTables){
        val (_, _, organization, address) = setup()
        createDistributionPoint(
            "Test DistributionPoint 1",
            address.id.value,
            organization.id.value,
            UUID_ZERO
        )

        assertThrows<DistributionPointException.DuplicateNameInOrganization> {
            createDistributionPoint(
                "Test DistributionPoint 1",
                address.id.value,
                organization.id.value,
                UUID_ZERO
            )
        }
    }

    @DbFunctional@Test
    fun createDistributionPointFailWithNoSuchOrganization() = runSimpleH2Test(*distributionPointTables){
        val (_, _, _, address) = setup()
        assertThrows<OrganizationException.NoSuchOrganization> {
            createDistributionPoint(
                "Test DistributionPoint 1",
                address.id.value,
                UUID_ZERO,
                UUID_ZERO
            )
        }
    }

    @DbFunctional@Test
    fun readDistributionPointsByOrganizationNoOrganizations() = runSimpleH2Test(*distributionPointTables){
        val (_, _, organization, _) = setup()
        val distributionPoints = readDistributionPointsByOrganization(organization.id.value)
        assertEquals(0, distributionPoints.size)
    }

    @DbFunctional@Test
    fun readDistributionPointsByOrganization() = runSimpleH2Test(*distributionPointTables){
        val (_, _, organization, address) = setup()
        createDistributionPoint(
            "Test DP",
            address.id.value,
            organization.id.value,
            UUID_ZERO
        )
        val distributionPoints = readDistributionPointsByOrganization(organization.id.value)
        assertEquals(1, distributionPoints.size)
    }

    @DbFunctional@Test
    fun updateDistributionPoint() = runSimpleH2Test(*distributionPointTables){
        val (_, _, organization, address) = setup()
        val distributionPoint = createDistributionPoint(
            "Test Name",
            address.id.value,
            organization.id.value,
            UUID_ZERO
        )
        val modifierId = UUID.randomUUID()
        val updateDistributionPoint = updateDistributionPoint(
            distributionPoint.id.value,
            "Test Name 2",
            null,
            organization.id.value,
            modifierId
        )

        assertEquals("Test Name 2", updateDistributionPoint.name)
        assertEquals(null, updateDistributionPoint.address)
        assertEquals(organization, updateDistributionPoint.organization)
        assertEquals(modifierId, updateDistributionPoint.modifiedBy)
    }

    @DbFunctional@Test
    fun updateDistributionPointFailWithDuplicateName() = runSimpleH2Test(*distributionPointTables){
        val (_, _, organization, address) = setup()
        val distributionPoint = createDistributionPoint(
            "Test Name",
            address.id.value,
            organization.id.value,
            UUID_ZERO
        )

        val distributionPoint2 = createDistributionPoint(
            "Test Name 2",
            address.id.value,
            organization.id.value,
            UUID_ZERO
        )
        val modifierId = UUID.randomUUID()
        assertThrows<DistributionPointException.DuplicateNameInOrganization> {
            updateDistributionPoint(
                distributionPoint.id.value,
                distributionPoint2.name,
                null,
                organization.id.value,
                modifierId
            )
        }
    }

    @DbFunctional@Test
    fun updateDistributionPointFailWithNoSuchOrganization() = runSimpleH2Test(*distributionPointTables){
        val (_, _, organization, address) = setup()
        val distributionPoint = createDistributionPoint(
            "Test Name",
            address.id.value,
            organization.id.value,
            UUID_ZERO
        )

        val modifierId = UUID.randomUUID()
        assertThrows<OrganizationException.NoSuchOrganization> {
            updateDistributionPoint(
                distributionPoint.id.value,
                distributionPoint.name,
                address.id.value,
                UUID_ZERO,
                modifierId
            )
        }
    }

    @DbFunctional@Test
    fun deleteDistributionPoint() = runSimpleH2Test(*distributionPointTables){
        val (_, _, organization, address) = setup()
        val distributionPoint = createDistributionPoint(
            "Test Name",
            address.id.value,
            organization.id.value,
            UUID_ZERO
        )
        deleteDistributionPoint(distributionPoint.id.value)
        assertNull(DistributionPointEntity.findById(distributionPoint.id.value))
        assertDoesNotThrow {
            validateAddress(address.id.value)
        }
        assertNotNull(OrganizationEntity.findById(organization.id.value))
    }

    @DbFunctional@Test
    fun validateAddressNoAddress() = runSimpleH2Test(*distributionPointTables){
        assertThrows<AddressException.NoSuchAddress> {
            validateAddress(UUID_ZERO)
        }
    }

    @DbFunctional@Test
    fun validateNameInOrganization() = runSimpleH2Test(*distributionPointTables){
        val (_ ,_,  organization, _) = setup()
        assertDoesNotThrow {
            validateNameInOrganization("Test Name", organization.id.value)
        }
    }

    @DbFunctional@Test
    fun validateNameInOrganizationFail() = runSimpleH2Test(*distributionPointTables){
        val (_ ,_,  organization, _) = setup()
        createDistributionPoint(
            "Test Name",
            null,
            organization.id.value,
            UUID_ZERO
        )
        assertThrows<DistributionPointException.DuplicateNameInOrganization> {
            validateNameInOrganization("Test Name", organization.id.value)
        }
    }
}
