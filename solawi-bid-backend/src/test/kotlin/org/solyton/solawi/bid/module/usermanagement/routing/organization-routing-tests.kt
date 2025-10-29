package org.solyton.solawi.bid.module.usermanagement.routing

import com.typesafe.config.ConfigFactory
import io.ktor.http.HttpStatusCode
import io.ktor.server.config.HoconApplicationConfig
import io.ktor.server.testing.testApplication
import kotlinx.coroutines.runBlocking
import org.evoleq.exposedx.migrations.isNotNull
import org.evoleq.ktorx.result.Result
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.Api
import org.solyton.solawi.bid.module.testFramework.contextExists
import org.solyton.solawi.bid.module.testFramework.getTestContextIdByName
import org.solyton.solawi.bid.module.testFramework.getTestRoleIdByName
import org.solyton.solawi.bid.module.testFramework.getTestToken
import org.solyton.solawi.bid.module.testFramework.getUserId
import org.solyton.solawi.bid.module.user.data.api.organization.AddMember
import org.solyton.solawi.bid.module.user.data.api.organization.ApiOrganization
import org.solyton.solawi.bid.module.user.data.api.organization.ApiOrganizations
import org.solyton.solawi.bid.module.user.data.api.organization.CreateChildOrganization
import org.solyton.solawi.bid.module.user.data.api.organization.CreateOrganization
import org.solyton.solawi.bid.module.user.data.api.organization.RemoveMember
import org.solyton.solawi.bid.module.user.data.api.organization.UpdateMember
import org.solyton.solawi.bid.module.user.data.api.organization.UpdateOrganization
import org.solyton.solawi.bid.module.usermanagement.routing.util.addMemberToOrganization
import org.solyton.solawi.bid.module.usermanagement.routing.util.createChildOrganization
import org.solyton.solawi.bid.module.usermanagement.routing.util.createOrganization
import org.solyton.solawi.bid.module.usermanagement.routing.util.readOrganizations
import org.solyton.solawi.bid.module.usermanagement.routing.util.removeMemberFromOrganization
import org.solyton.solawi.bid.module.usermanagement.routing.util.updateMemberOfOrganization
import org.solyton.solawi.bid.module.usermanagement.routing.util.updateOrganization
import java.io.File
import java.util.UUID
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@Suppress("LargeClass")
class OrganizationRoutingTests {
    @Api@Test fun createRootOrganization() = runBlocking {
        testApplication {
            environment {
                // Load the HOCON file explicitly with the file path
                val configFile = File("src/test/resources/usermanagement.api.test.conf")
                config = HoconApplicationConfig(ConfigFactory.parseFile(configFile))

            }
            application {

            }

            // data
            val organizationName = "name"

            // get token and context
            val token = client.getTestToken("developer@alpha-structure.com")
            val applicationContextId = client.getTestContextIdByName("APPLICATION")

            val organizationResult: Result<ApiOrganization> = client.createOrganization(
                CreateOrganization(organizationName),
                token,
                applicationContextId
            ) {
                assertEquals(HttpStatusCode.OK, this.status ){ "Status not OK!"}
            }
            assertIs<Result.Success<ApiOrganization>>(organizationResult)

            val apiOrganization = organizationResult.data
            assertEquals(organizationName, apiOrganization.name)

            val contextExists = client.contextExists(UUID.fromString(apiOrganization.contextId))
            assertTrue(contextExists)

        }
    }

    @Api@Test fun readOrganizations() = runBlocking {
        testApplication {
            environment {
                // Load the HOCON file explicitly with the file path
                val configFile = File("src/test/resources/usermanagement.api.test.conf")
                config = HoconApplicationConfig(ConfigFactory.parseFile(configFile))

            }
            application {

            }

            // data
            val organizationName = "name-0"

            // get token and context
            val token = client.getTestToken("developer@alpha-structure.com")
            val applicationContextId = client.getTestContextIdByName("APPLICATION")

            val organizationResult: Result<ApiOrganization> = client.createOrganization(
                CreateOrganization(organizationName),
                token,
                applicationContextId
            ) {
                assertEquals(HttpStatusCode.OK, this.status ){ "Status not OK!"}
            }
            assertIs<Result.Success<ApiOrganization>>(organizationResult)

            val apiOrganization = organizationResult.data
            assertEquals(organizationName, apiOrganization.name)

            val contextExists = client.contextExists(UUID.fromString(apiOrganization.contextId))
            assertTrue(contextExists)


            // read organization
            val readOrganizationResult = client.readOrganizations(token, applicationContextId)
            assertIs<Result.Success<ApiOrganizations>>(readOrganizationResult)
            val organizations = readOrganizationResult.data.all
            assertTrue { organizations.isNotEmpty() }
            assertTrue { organizations.first { organization -> organization.name == organizationName }.isNotNull() }

        }
    }

    @Api@Test fun readOrganizationsWithoutPermission() = runBlocking {
        testApplication {
            environment {
                // Load the HOCON file explicitly with the file path
                val configFile = File("src/test/resources/usermanagement.api.test.conf")
                config = HoconApplicationConfig(ConfigFactory.parseFile(configFile))

            }
            application {

            }

            // data
            val organizationName = "name-0-wp"

            // get token and context
            val token = client.getTestToken("developer@alpha-structure.com")
            val unAuthorizedToken = client.getTestToken("unautorized@solyton.org")
            val applicationContextId = client.getTestContextIdByName("APPLICATION")

            val organizationResult: Result<ApiOrganization> = client.createOrganization(
                CreateOrganization(organizationName),
                token,
                applicationContextId
            ) {
                assertEquals(HttpStatusCode.OK, this.status ){ "Status not OK!"}
            }
            assertIs<Result.Success<ApiOrganization>>(organizationResult)

            val apiOrganization = organizationResult.data
            assertEquals(organizationName, apiOrganization.name)

            val contextExists = client.contextExists(UUID.fromString(apiOrganization.contextId))
            assertTrue(contextExists)


            // read organization
            val readOrganizationResult = client.readOrganizations(token, applicationContextId)
            assertIs<Result.Success<ApiOrganizations>>(readOrganizationResult)
            val nonEmptyList = readOrganizationResult.data
            assertTrue { nonEmptyList.all.isNotEmpty() }

            val unAuthorizedResult = client.readOrganizations(unAuthorizedToken, applicationContextId)
            assertIs<Result.Success<ApiOrganizations>>(unAuthorizedResult)
            val organizations = unAuthorizedResult.data

            assertTrue{ organizations.all.isEmpty() }
        }
    }

    @Api@Test fun createChildOrganization() = runBlocking {
        testApplication {
            environment {
                // Load the HOCON file explicitly with the file path
                val configFile = File("src/test/resources/usermanagement.api.test.conf")
                config = HoconApplicationConfig(ConfigFactory.parseFile(configFile))

            }
            application {

            }


            // get token and context
            val token = client.getTestToken("developer@alpha-structure.com")
            val applicationContextId = client.getTestContextIdByName("APPLICATION")

            // prepare
            val organizationName = "root-organization"

            val rootOrganizationResult: Result<ApiOrganization> = client.createOrganization(
                CreateOrganization(organizationName),
                token,
                applicationContextId
            ) {
                assertEquals(HttpStatusCode.OK, this.status ){ "Status not OK!"}
            }
            assertIs<Result.Success<ApiOrganization>>(rootOrganizationResult)

            val rootOrganization = rootOrganizationResult.data
            assertEquals(organizationName, rootOrganization.name)

            val rootOrganizationContextExists = client.contextExists(UUID.fromString(rootOrganization.contextId))
            assertTrue(rootOrganizationContextExists)

            // go testing
            val childOrganizationName = "child-organization"
            // following result has to carry the root organization
            val childOrganizationResult: Result<ApiOrganization> = client.createChildOrganization(
                CreateChildOrganization(rootOrganization.id, childOrganizationName),
                token,
                //rootOrganization.contextId
                applicationContextId
            ) {
                assertEquals(HttpStatusCode.OK, this.status ){ "Status not OK!"}
            }
            assertIs<Result.Success<ApiOrganization>>(childOrganizationResult)
            val returnedOrganization = childOrganizationResult.data
            assertEquals(childOrganizationName, returnedOrganization.name)

            val childOrganizationContextExists = client.contextExists(UUID.fromString(returnedOrganization.contextId))
            assertTrue(childOrganizationContextExists)

        }
    }

    @Api@Test fun updateOrganization() = runBlocking {
        testApplication {
            environment {
                // Load the HOCON file explicitly with the file path
                val configFile = File("src/test/resources/usermanagement.api.test.conf")
                config = HoconApplicationConfig(ConfigFactory.parseFile(configFile))

            }
            application {

            }

            // data
            val organizationName = "name-7"

            // get token and context
            val token = client.getTestToken("developer@alpha-structure.com")
            val applicationContextId = client.getTestContextIdByName("APPLICATION")

            val organizationResult: Result<ApiOrganization> = client.createOrganization(
                CreateOrganization(organizationName),
                token,
                applicationContextId
            ) {
                assertEquals(HttpStatusCode.OK, this.status ){ "Status not OK!"}
            }
            assertIs<Result.Success<ApiOrganization>>(organizationResult)

            val apiOrganization = organizationResult.data
            assertEquals(organizationName, apiOrganization.name)

            val contextExists = client.contextExists(UUID.fromString(apiOrganization.contextId))
            assertTrue(contextExists)

            val newName = "new-name"
            val updatedOrganizationResult = client.updateOrganization(
                UpdateOrganization(
                    apiOrganization.id,
                    newName
                ),
                token,
                apiOrganization.contextId
                // applicationContextId
            )
            assertIs<Result.Success<ApiOrganization>>(updatedOrganizationResult)
            val updatedOrganization = updatedOrganizationResult.data

            assertEquals(newName, updatedOrganization.name)
        }
    }

    @Api@Test fun cannotUpdateOrganizationWithoutPermission() = runBlocking {
        testApplication {
            environment {
                // Load the HOCON file explicitly with the file path
                val configFile = File("src/test/resources/usermanagement.api.test.conf")
                config = HoconApplicationConfig(ConfigFactory.parseFile(configFile))

            }
            application {

            }

            // data
            val organizationName = "name-7-wp"

            // get token and context
            val token = client.getTestToken("developer@alpha-structure.com")
            val unAuthorizedToken = client.getTestToken("unautorized@solyton.org")
            val applicationContextId = client.getTestContextIdByName("APPLICATION")

            val organizationResult: Result<ApiOrganization> = client.createOrganization(
                CreateOrganization(organizationName),
                token,
                applicationContextId
            ) {
                assertEquals(HttpStatusCode.OK, this.status ){ "Status not OK!"}
            }
            assertIs<Result.Success<ApiOrganization>>(organizationResult)

            val apiOrganization = organizationResult.data
            assertEquals(organizationName, apiOrganization.name)

            val contextExists = client.contextExists(UUID.fromString(apiOrganization.contextId))
            assertTrue(contextExists)

            val newName = "new-name-wp"
            // val updatedOrganizationResult =
            client.updateOrganization(
                UpdateOrganization(
                    apiOrganization.id,
                    newName
                ),
                unAuthorizedToken,
                apiOrganization.contextId
                // applicationContextId
            ) {
                assertEquals(HttpStatusCode.Forbidden, status)
            }
        }
    }

    @Api@Test fun addMemberToOrganizationTest() = runBlocking {
        testApplication {
            environment {
                // Load the HOCON file explicitly with the file path
                val configFile = File("src/test/resources/usermanagement.api.test.conf")
                config = HoconApplicationConfig(ConfigFactory.parseFile(configFile))

            }
            application {

            }

            // data
            val organizationName = "add-member-to-organization-test"

            // get token and context
            val token = client.getTestToken("developer@alpha-structure.com")
            val unAuthorizedToken = client.getTestToken("unautorized@solyton.org")
            val applicationContextId = client.getTestContextIdByName("APPLICATION")

            val organizationResult: Result<ApiOrganization> = client.createOrganization(
                CreateOrganization(organizationName),
                token,
                applicationContextId
            ) {
                assertEquals(HttpStatusCode.OK, this.status) { "Status not OK!" }
            }
            assertIs<Result.Success<ApiOrganization>>(organizationResult)

            val apiOrganization = organizationResult.data
            assertEquals(organizationName, apiOrganization.name)

            val contextExists = client.contextExists(UUID.fromString(apiOrganization.contextId))
            assertTrue(contextExists)

            // Add member
            val organizationContextId = apiOrganization.contextId
            val memberId = client.getUserId("unautorized@solyton.org")
            val userRoleId = client.getTestRoleIdByName("USER")
            val addMemberResult = client.addMemberToOrganization(
                AddMember(
                    apiOrganization.id,
                    memberId,
                    listOf(userRoleId)
                ),
                token,
                organizationContextId
            ) {
                assertEquals(HttpStatusCode.OK, status)
            }
            assertIs<Result.Success<ApiOrganization>>(addMemberResult)
            val organizationWithMember = addMemberResult.data
            assertTrue { organizationWithMember.members.isNotEmpty() }
            assertEquals(2, organizationWithMember.members.size)
            val member = organizationWithMember.members.find{it.userId == memberId}
            assertNotNull(member)
            assertEquals(memberId, member.userId)
            assertEquals(1, member.roles.size)
            assertEquals(userRoleId, member.roles.first().id)
            assertEquals("USER", member.roles.first().name)
        }
    }

    @Api@Test fun cannotAddMemberToOrganizationTwiceTest() = runBlocking {
        testApplication {
            environment {
                // Load the HOCON file explicitly with the file path
                val configFile = File("src/test/resources/usermanagement.api.test.conf")
                config = HoconApplicationConfig(ConfigFactory.parseFile(configFile))

            }
            application {

            }

            // data
            val organizationName = "cannot-add-member-to-organization-twice-test"

            // get token and context
            val token = client.getTestToken("developer@alpha-structure.com")
            val unAuthorizedToken = client.getTestToken("unautorized@solyton.org")
            val applicationContextId = client.getTestContextIdByName("APPLICATION")

            val organizationResult: Result<ApiOrganization> = client.createOrganization(
                CreateOrganization(organizationName),
                token,
                applicationContextId
            ) {
                assertEquals(HttpStatusCode.OK, this.status) { "Status not OK!" }
            }
            assertIs<Result.Success<ApiOrganization>>(organizationResult)

            val apiOrganization = organizationResult.data
            assertEquals(organizationName, apiOrganization.name)

            val contextExists = client.contextExists(UUID.fromString(apiOrganization.contextId))
            assertTrue(contextExists)

            // Add member
            val organizationContextId = apiOrganization.contextId
            val memberId = client.getUserId("unautorized@solyton.org")
            val userRoleId = client.getTestRoleIdByName("USER")
            val addMemberResult = client.addMemberToOrganization(
                AddMember(
                    apiOrganization.id,
                    memberId,
                    listOf(userRoleId)
                ),
                token,
                organizationContextId
            ) {
                assertEquals(HttpStatusCode.OK, status)
            }
            assertIs<Result.Success<ApiOrganization>>(addMemberResult)
            val organizationWithMember = addMemberResult.data
            assertTrue { organizationWithMember.members.isNotEmpty() }
            assertEquals(2, organizationWithMember.members.size)
            val member = organizationWithMember.members.find{it.userId == memberId}
            assertNotNull(member)
            assertEquals(memberId, member.userId)
            assertEquals(1, member.roles.size)
            assertEquals(userRoleId, member.roles.first().id)
            assertEquals("USER", member.roles.first().name)


            // try to add it twice
            client.addMemberToOrganization(
                AddMember(
                    apiOrganization.id,
                    memberId,
                    listOf(userRoleId)
                ),
                token,
                organizationContextId
            ) {
                assertEquals(HttpStatusCode.Conflict, status)
            }
        }
    }

    @Api@Test fun cannotAddMemberToOrganizationWithoutPermissionTest() = runBlocking {
        testApplication {
            environment {
                // Load the HOCON file explicitly with the file path
                val configFile = File("src/test/resources/usermanagement.api.test.conf")
                config = HoconApplicationConfig(ConfigFactory.parseFile(configFile))

            }
            application {

            }

            // data
            val organizationName = "cannot-add-member-to-organization-without-permission-test"

            // get token and context
            val token = client.getTestToken("developer@alpha-structure.com")
            val unAuthorizedToken = client.getTestToken("unautorized@solyton.org")
            val applicationContextId = client.getTestContextIdByName("APPLICATION")

            val organizationResult: Result<ApiOrganization> = client.createOrganization(
                CreateOrganization(organizationName),
                token,
                applicationContextId
            ) {
                assertEquals(HttpStatusCode.OK, this.status) { "Status not OK!" }
            }
            assertIs<Result.Success<ApiOrganization>>(organizationResult)

            val apiOrganization = organizationResult.data
            assertEquals(organizationName, apiOrganization.name)

            val contextExists = client.contextExists(UUID.fromString(apiOrganization.contextId))
            assertTrue(contextExists)

            // Add member
            val organizationContextId = apiOrganization.contextId
            val memberId = client.getUserId("unautorized@solyton.org")
            val userRoleId = client.getTestRoleIdByName("USER")

            client.addMemberToOrganization(
                AddMember(
                    apiOrganization.id,
                    memberId,
                    listOf(userRoleId)
                ),
                unAuthorizedToken,
                organizationContextId
            ) {
                assertEquals(HttpStatusCode.Forbidden, status)
            }

        }
    }

    @Api@Test fun cannotAddMemberToOrganizationInWrongContextTest() = runBlocking {
        testApplication {
            environment {
                // Load the HOCON file explicitly with the file path
                val configFile = File("src/test/resources/usermanagement.api.test.conf")
                config = HoconApplicationConfig(ConfigFactory.parseFile(configFile))

            }
            application {

            }

            // data
            val organizationName = "cannot-add-member-to-organization-in-wrong.context-test"

            // get token and context
            val token = client.getTestToken("developer@alpha-structure.com")
            val unAuthorizedToken = client.getTestToken("unautorized@solyton.org")
            val applicationContextId = client.getTestContextIdByName("APPLICATION")

            val organizationResult: Result<ApiOrganization> = client.createOrganization(
                CreateOrganization(organizationName),
                token,
                applicationContextId
            ) {
                assertEquals(HttpStatusCode.OK, this.status) { "Status not OK!" }
            }
            assertIs<Result.Success<ApiOrganization>>(organizationResult)

            val apiOrganization = organizationResult.data
            assertEquals(organizationName, apiOrganization.name)

            val contextExists = client.contextExists(UUID.fromString(apiOrganization.contextId))
            assertTrue(contextExists)

            // Add member
            // val organizationContextId = apiOrganization.contextId
            val memberId = client.getUserId("unautorized@solyton.org")
            val userRoleId = client.getTestRoleIdByName("USER")

            client.addMemberToOrganization(
                AddMember(
                    apiOrganization.id,
                    memberId,
                    listOf(userRoleId)
                ),
                token,
                applicationContextId
            ) {
                assertEquals(HttpStatusCode.Forbidden, status)
            }

        }
    }


    @Api@Test fun updateMemberOfOrganizationTest() = runBlocking {
        testApplication {
            environment {
                // Load the HOCON file explicitly with the file path
                val configFile = File("src/test/resources/usermanagement.api.test.conf")
                config = HoconApplicationConfig(ConfigFactory.parseFile(configFile))

            }
            application {

            }

            // data
            val organizationName = "update-member-of-organization-test"

            // get token and context
            val token = client.getTestToken("developer@alpha-structure.com")
            //val unAuthorizedToken = client.getTestToken("unautorized@solyton.org")
            val applicationContextId = client.getTestContextIdByName("APPLICATION")

            val organizationResult: Result<ApiOrganization> = client.createOrganization(
                CreateOrganization(organizationName),
                token,
                applicationContextId
            ) {
                assertEquals(HttpStatusCode.OK, this.status) { "Status not OK!" }
            }
            assertIs<Result.Success<ApiOrganization>>(organizationResult)

            val apiOrganization = organizationResult.data
            assertEquals(organizationName, apiOrganization.name)

            val contextExists = client.contextExists(UUID.fromString(apiOrganization.contextId))
            assertTrue(contextExists)

            // Add member
            val organizationContextId = apiOrganization.contextId
            val memberId = client.getUserId("unautorized@solyton.org")
            val userRoleId = client.getTestRoleIdByName("USER")
            val addMemberResult = client.addMemberToOrganization(
                AddMember(
                    apiOrganization.id,
                    memberId,
                    listOf(userRoleId)
                ),
                token,
                organizationContextId
            ) {
                assertEquals(HttpStatusCode.OK, status)
            }
            assertIs<Result.Success<ApiOrganization>>(addMemberResult)
            val organizationWithMember = addMemberResult.data
            assertTrue { organizationWithMember.members.isNotEmpty() }
            assertEquals(2, organizationWithMember.members.size)
            val member = organizationWithMember.members.find{it.userId == memberId}
            assertNotNull(member)
            assertEquals(memberId, member.userId)
            assertEquals(1, member.roles.size)
            assertEquals(userRoleId, member.roles.first().id)
            assertEquals("USER", member.roles.first().name)

            val managerRoleId = client.getTestRoleIdByName("MANAGER")
            val updateMemberResult = client.updateMemberOfOrganization(
                UpdateMember(
                    apiOrganization.id,
                    memberId,
                    listOf(managerRoleId)
                ),
                token,
                organizationContextId
            ){
                assertEquals(HttpStatusCode.OK, status)
            }
            assertIs<Result.Success<ApiOrganization>>(updateMemberResult)
            val updatedOrganizationWithMember = updateMemberResult.data
            assertTrue { updatedOrganizationWithMember.members.isNotEmpty() }
            assertEquals(2, updatedOrganizationWithMember.members.size)
            val updatedMember = updatedOrganizationWithMember.members.find{it.userId == memberId}
            assertNotNull(updatedMember)

            assertEquals(memberId, updatedMember.userId)
            assertEquals(1, updatedMember.roles.size)
            assertEquals(managerRoleId, updatedMember.roles.first().id, "Wrong role id")
            assertEquals("MANAGER", updatedMember.roles.first().name)
        }
    }

    @Api@Test fun cannotUpdateMemberOfOrganizationWithoutPermissionTest() = runBlocking {
        testApplication {
            environment {
                // Load the HOCON file explicitly with the file path
                val configFile = File("src/test/resources/usermanagement.api.test.conf")
                config = HoconApplicationConfig(ConfigFactory.parseFile(configFile))

            }
            application {

            }

            // data
            val organizationName = "cannot-update-member-of-organization-without-permission-test"

            // get token and context
            val token = client.getTestToken("developer@alpha-structure.com")
            val unAuthorizedToken = client.getTestToken("unautorized@solyton.org")
            val applicationContextId = client.getTestContextIdByName("APPLICATION")

            val organizationResult: Result<ApiOrganization> = client.createOrganization(
                CreateOrganization(organizationName),
                token,
                applicationContextId
            ) {
                assertEquals(HttpStatusCode.OK, this.status) { "Status not OK!" }
            }
            assertIs<Result.Success<ApiOrganization>>(organizationResult)

            val apiOrganization = organizationResult.data
            assertEquals(organizationName, apiOrganization.name)

            val contextExists = client.contextExists(UUID.fromString(apiOrganization.contextId))
            assertTrue(contextExists)

            // Add member
            val organizationContextId = apiOrganization.contextId
            val memberId = client.getUserId("unautorized@solyton.org")
            val userRoleId = client.getTestRoleIdByName("USER")
            val addMemberResult = client.addMemberToOrganization(
                AddMember(
                    apiOrganization.id,
                    memberId,
                    listOf(userRoleId)
                ),
                token,
                organizationContextId
            ) {
                assertEquals(HttpStatusCode.OK, status)
            }
            assertIs<Result.Success<ApiOrganization>>(addMemberResult)
            val organizationWithMember = addMemberResult.data
            assertTrue { organizationWithMember.members.isNotEmpty() }
            assertEquals(2, organizationWithMember.members.size)
            val member = organizationWithMember.members.find{it.userId == memberId}
            assertNotNull(member)
            assertEquals(memberId, member.userId)
            assertEquals(1, member.roles.size)
            assertEquals(userRoleId, member.roles.first().id)
            assertEquals("USER", member.roles.first().name)

            val managerRoleId = client.getTestRoleIdByName("MANAGER")
            client.updateMemberOfOrganization(
                UpdateMember(
                    apiOrganization.id,
                    memberId,
                    listOf(managerRoleId)
                ),
                unAuthorizedToken,
                organizationContextId
            ){
                assertEquals(HttpStatusCode.Forbidden, status)
            }
        }
    }

    @Api@Test fun cannotUpdateMemberOfOrganizationInWrongContextTest() = runBlocking {
        testApplication {
            environment {
                // Load the HOCON file explicitly with the file path
                val configFile = File("src/test/resources/usermanagement.api.test.conf")
                config = HoconApplicationConfig(ConfigFactory.parseFile(configFile))

            }
            application {

            }

            // data
            val organizationName = "cannot-update-member-of-organization-in-wrong-context-test"

            // get token and context
            val token = client.getTestToken("developer@alpha-structure.com")
            val applicationContextId = client.getTestContextIdByName("APPLICATION")

            val organizationResult: Result<ApiOrganization> = client.createOrganization(
                CreateOrganization(organizationName),
                token,
                applicationContextId
            ) {
                assertEquals(HttpStatusCode.OK, this.status) { "Status not OK!" }
            }
            assertIs<Result.Success<ApiOrganization>>(organizationResult)

            val apiOrganization = organizationResult.data
            assertEquals(organizationName, apiOrganization.name)

            val contextExists = client.contextExists(UUID.fromString(apiOrganization.contextId))
            assertTrue(contextExists)

            // Add member
            val organizationContextId = apiOrganization.contextId
            val memberId = client.getUserId("unautorized@solyton.org")
            val userRoleId = client.getTestRoleIdByName("USER")
            val addMemberResult = client.addMemberToOrganization(
                AddMember(
                    apiOrganization.id,
                    memberId,
                    listOf(userRoleId)
                ),
                token,
                organizationContextId
            ) {
                assertEquals(HttpStatusCode.OK, status)
            }
            assertIs<Result.Success<ApiOrganization>>(addMemberResult)
            val organizationWithMember = addMemberResult.data
            assertTrue { organizationWithMember.members.isNotEmpty() }
            assertEquals(2, organizationWithMember.members.size)
            val member = organizationWithMember.members.find{it.userId == memberId}
            assertNotNull(member)
            assertEquals(memberId, member.userId)
            assertEquals(1, member.roles.size)
            assertEquals(userRoleId, member.roles.first().id)
            assertEquals("USER", member.roles.first().name)

            val managerRoleId = client.getTestRoleIdByName("MANAGER")
            client.updateMemberOfOrganization(
                UpdateMember(
                    apiOrganization.id,
                    memberId,
                    listOf(managerRoleId)
                ),
                token,
                applicationContextId
            ){
                assertEquals(HttpStatusCode.Forbidden, status)
            }
        }
    }


    @Api@Test fun removeMemberFromOrganizationTest() = runBlocking {
        testApplication {
            environment {
                // Load the HOCON file explicitly with the file path
                val configFile = File("src/test/resources/usermanagement.api.test.conf")
                config = HoconApplicationConfig(ConfigFactory.parseFile(configFile))

            }
            application {

            }

            // data
            val organizationName = "remove-member-from-organization-test"

            // get token and context
            val token = client.getTestToken("developer@alpha-structure.com")
            // val unAuthorizedToken = client.getTestToken("unautorized@solyton.org")
            val applicationContextId = client.getTestContextIdByName("APPLICATION")

            val organizationResult: Result<ApiOrganization> = client.createOrganization(
                CreateOrganization(organizationName),
                token,
                applicationContextId
            ) {
                assertEquals(HttpStatusCode.OK, this.status) { "Status not OK!" }
            }
            assertIs<Result.Success<ApiOrganization>>(organizationResult)

            val apiOrganization = organizationResult.data
            assertEquals(organizationName, apiOrganization.name)

            val contextExists = client.contextExists(UUID.fromString(apiOrganization.contextId))
            assertTrue(contextExists)

            // Add member
            val organizationContextId = apiOrganization.contextId
            val memberId = client.getUserId("unautorized@solyton.org")
            val userRoleId = client.getTestRoleIdByName("USER")
            val addMemberResult = client.addMemberToOrganization(
                AddMember(
                    apiOrganization.id,
                    memberId,
                    listOf(userRoleId)
                ),
                token,
                organizationContextId
            ) {
                assertEquals(HttpStatusCode.OK, status)
            }
            assertIs<Result.Success<ApiOrganization>>(addMemberResult)
            val organizationWithMember = addMemberResult.data
            assertTrue { organizationWithMember.members.isNotEmpty() }
            assertEquals(2, organizationWithMember.members.size)
            val member = organizationWithMember.members.find{it.userId == memberId}
            assertNotNull(member)
            assertEquals(memberId, member.userId)
            assertEquals(1, member.roles.size)
            assertEquals(userRoleId, member.roles.first().id)
            assertEquals("USER", member.roles.first().name)

            val removeMemberResult = client.removeMemberFromOrganization(
                RemoveMember(
                    apiOrganization.id,
                    memberId
                ),
                token,
                organizationContextId
            ) {
                assertEquals(HttpStatusCode.OK, status)
            }
            assertIs<Result.Success<ApiOrganization>>(removeMemberResult)
            val resultOrganization = removeMemberResult.data
            assertFalse{ resultOrganization.members.map{it.userId}.contains(memberId) }
            assertEquals(addMemberResult.data.members.size - 1, resultOrganization.members.size )
        }
    }

    @Api@Test fun cannotRemoveMemberFromOrganizationWithoutPermissionTest() = runBlocking {
        testApplication {
            environment {
                // Load the HOCON file explicitly with the file path
                val configFile = File("src/test/resources/usermanagement.api.test.conf")
                config = HoconApplicationConfig(ConfigFactory.parseFile(configFile))

            }
            application {

            }

            // data
            val organizationName = "cannot-remove-member-from-organization-without-permission-test"

            // get token and context
            val token = client.getTestToken("developer@alpha-structure.com")
            val unAuthorizedToken = client.getTestToken("unautorized@solyton.org")
            val applicationContextId = client.getTestContextIdByName("APPLICATION")

            val organizationResult: Result<ApiOrganization> = client.createOrganization(
                CreateOrganization(organizationName),
                token,
                applicationContextId
            ) {
                assertEquals(HttpStatusCode.OK, this.status) { "Status not OK!" }
            }
            assertIs<Result.Success<ApiOrganization>>(organizationResult)

            val apiOrganization = organizationResult.data
            assertEquals(organizationName, apiOrganization.name)

            val contextExists = client.contextExists(UUID.fromString(apiOrganization.contextId))
            assertTrue(contextExists)

            // Add member
            val organizationContextId = apiOrganization.contextId
            val memberId = client.getUserId("unautorized@solyton.org")
            val userRoleId = client.getTestRoleIdByName("USER")
            val addMemberResult = client.addMemberToOrganization(
                AddMember(
                    apiOrganization.id,
                    memberId,
                    listOf(userRoleId)
                ),
                token,
                organizationContextId
            ) {
                assertEquals(HttpStatusCode.OK, status)
            }
            assertIs<Result.Success<ApiOrganization>>(addMemberResult)
            val organizationWithMember = addMemberResult.data
            assertTrue { organizationWithMember.members.isNotEmpty() }
            assertEquals(2, organizationWithMember.members.size)
            val member = organizationWithMember.members.find{it.userId == memberId}
            assertNotNull(member)
            assertEquals(memberId, member.userId)
            assertEquals(1, member.roles.size)
            assertEquals(userRoleId, member.roles.first().id)
            assertEquals("USER", member.roles.first().name)

            client.removeMemberFromOrganization(
                RemoveMember(
                    apiOrganization.id,
                    memberId
                ),
                unAuthorizedToken,
                organizationContextId
            ) {
                assertEquals(HttpStatusCode.Forbidden, status)
            }
        }
    }

    @Api@Test fun cannotRemoveMemberFromOrganizationInWrongContextTest() = runBlocking {
        testApplication {
            environment {
                // Load the HOCON file explicitly with the file path
                val configFile = File("src/test/resources/usermanagement.api.test.conf")
                config = HoconApplicationConfig(ConfigFactory.parseFile(configFile))

            }
            application {

            }

            // data
            val organizationName = "cannot-remove-member-from-organization-in-wrong-context-test"

            // get token and context
            val token = client.getTestToken("developer@alpha-structure.com")
            val unAuthorizedToken = client.getTestToken("unautorized@solyton.org")
            val applicationContextId = client.getTestContextIdByName("APPLICATION")

            val organizationResult: Result<ApiOrganization> = client.createOrganization(
                CreateOrganization(organizationName),
                token,
                applicationContextId
            ) {
                assertEquals(HttpStatusCode.OK, this.status) { "Status not OK!" }
            }
            assertIs<Result.Success<ApiOrganization>>(organizationResult)

            val apiOrganization = organizationResult.data
            assertEquals(organizationName, apiOrganization.name)

            val contextExists = client.contextExists(UUID.fromString(apiOrganization.contextId))
            assertTrue(contextExists)

            // Add member
            val organizationContextId = apiOrganization.contextId
            val memberId = client.getUserId("unautorized@solyton.org")
            val userRoleId = client.getTestRoleIdByName("USER")
            val addMemberResult = client.addMemberToOrganization(
                AddMember(
                    apiOrganization.id,
                    memberId,
                    listOf(userRoleId)
                ),
                token,
                organizationContextId
            ) {
                assertEquals(HttpStatusCode.OK, status)
            }
            assertIs<Result.Success<ApiOrganization>>(addMemberResult)
            val organizationWithMember = addMemberResult.data
            assertTrue { organizationWithMember.members.isNotEmpty() }
            assertEquals(2, organizationWithMember.members.size)
            val member = organizationWithMember.members.find{it.userId == memberId}
            assertNotNull(member)
            assertEquals(memberId, member.userId)
            assertEquals(1, member.roles.size)
            assertEquals(userRoleId, member.roles.first().id)
            assertEquals("USER", member.roles.first().name)

            client.removeMemberFromOrganization(
                RemoveMember(
                    apiOrganization.id,
                    memberId
                ),
                token,
                applicationContextId
            ) {
                assertEquals(HttpStatusCode.Forbidden, status)
            }
        }
    }
}
