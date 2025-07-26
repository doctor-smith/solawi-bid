package org.solyton.solawi.bid.module.usermanagement

import org.evoleq.exposedx.test.runSimpleH2Test
import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.DbFunctional
import org.solyton.solawi.bid.module.db.migrations.setupBasicRolesAndRights
import org.solyton.solawi.bid.module.db.repository.*
import org.solyton.solawi.bid.module.db.schema.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OrganizationTests {
    private val organizationName = "TEST_ORGANIZATION"

    private val neededTables = arrayOf(
        OrganizationsTable,
        UsersTable,
        UserOrganization,
        Roles,
        Rights,
        Contexts,
        RoleRightContexts
    )



    @DbFunctional@Test fun createOrganization() = runSimpleH2Test(*neededTables){
        setupBasicRolesAndRights()

        val organization = createRootOrganization(organizationName)


        assertEquals(0, organization.left)
        assertEquals(1, organization.right)

        val result = getOrganizationByName(organizationName)
        assertEquals(organizationName, result.name)
    }

    @DbFunctional@Test fun addUser() = runSimpleH2Test(*neededTables){
        setupBasicRolesAndRights()
        val user = UserEntity.new {
            password = "password"
            username = "username"
        }
        val organization = createRootOrganization(organizationName)

        organization.addUser(user)

        val result = getOrganizationByName(organizationName)
        assertFalse{ result.users.empty() }
        assertTrue{ result.users.contains(user) }
    }

    @DbFunctional@Test fun createChild() = runSimpleH2Test(*neededTables){
        setupBasicRolesAndRights()

        var organization = createRootOrganization(organizationName)

        val childOrganization = "TEST_CHILD_ORGANIZATION"
        val child = organization.createChild(childOrganization)

        organization = getOrganizationByName(organizationName)



        assertEquals(childOrganization, child.name)

        // organization (interesting)
        assertEquals(0, organization.left, "organization has wrong left value")
        assertEquals(3, organization.right, "organization has wrong right value")
        assertEquals(0, organization.level, "organization has wrong level value")
        // child
        assertEquals(organization, child.root)
        assertEquals(1, child.level, "child has wrong level")
        assertEquals(1, child.left, "child has wrong left value")
        assertEquals(2, child.right,"child has wrong right value")

        val level2Name = "TEST_LEVEL_TWO_CHILD"
        val level2Child = child.createChild(level2Name)
        organization = getOrganizationByName(organizationName)
        // organization
        assertEquals(0, organization.left, "organization has wrong left value")
        assertEquals(5, organization.right, "organization has wrong right value")
        assertEquals(0, organization.level, "organization has wrong level value")
        // child
        assertEquals(organization, child.root)
        assertEquals(1, child.level, "child has wrong level")
        assertEquals(1, child.left, "child has wrong left value")
        assertEquals(4, child.right,"child has wrong right value")

        assertEquals(organization, level2Child.root)
        assertEquals(2, level2Child.level, "child has wrong level")
        assertEquals(2, level2Child.left, "child has wrong left value")
        assertEquals(3, level2Child.right,"child has wrong right value")

    }

    @DbFunctional@Test fun ancestors() = runSimpleH2Test(*neededTables){
        setupBasicRolesAndRights()

        val organization = createRootOrganization(organizationName)
        val childOrganization = "TEST_CHILD_ORGANIZATION"
        val child = organization.createChild(childOrganization)
        val ancestors = child.ancestors().map{it.name}
        println(ancestors)
        assertTrue { ancestors.contains(organizationName) }
    }


    @DbFunctional@Test fun removeChild() = runSimpleH2Test(*neededTables){
        setupBasicRolesAndRights()

        val organization = createRootOrganization(organizationName)
        val childOrganization = "TEST_CHILD_ORGANIZATION"
        val child = organization.createChild(childOrganization)

        assertTrue { organization.getChildren().contains(child) }

        val result = organization.removeChild(child.id.value)

        assertFalse { organization.getChildren().contains(child) }
        assertEquals(0,result.left)
        assertEquals(1,result.right)
    }
}
