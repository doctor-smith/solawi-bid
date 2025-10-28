package org.solyton.solawi.bid.module.usermanagement.routing

import com.typesafe.config.ConfigFactory
import io.ktor.http.HttpStatusCode
import io.ktor.server.config.HoconApplicationConfig
import io.ktor.server.testing.testApplication
import kotlinx.coroutines.runBlocking
import org.evoleq.ktorx.result.Result
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.Api
import org.solyton.solawi.bid.module.testFramework.contextExists
import org.solyton.solawi.bid.module.testFramework.getTestContextIdByName
import org.solyton.solawi.bid.module.testFramework.getTestToken
import org.solyton.solawi.bid.module.user.data.api.organization.ApiOrganization
import org.solyton.solawi.bid.module.user.data.api.organization.ApiOrganizations
import org.solyton.solawi.bid.module.user.data.api.organization.CreateChildOrganization
import org.solyton.solawi.bid.module.user.data.api.organization.CreateOrganization
import org.solyton.solawi.bid.module.user.data.api.organization.UpdateOrganization
import org.solyton.solawi.bid.module.usermanagement.routing.util.createChildOrganization
import org.solyton.solawi.bid.module.usermanagement.routing.util.createOrganization
import org.solyton.solawi.bid.module.usermanagement.routing.util.readOrganizations
import org.solyton.solawi.bid.module.usermanagement.routing.util.updateOrganization
import java.io.File
import java.util.UUID
import kotlin.test.assertIs
import kotlin.test.assertTrue

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

    @Api@Test fun readOrganization() = runBlocking {
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
                applicationContextId
            )
            assertIs<Result.Success<ApiOrganization>>(updatedOrganizationResult)
            val updatedOrganization = updatedOrganizationResult.data

            assertEquals(newName, updatedOrganization.name)
        }
    }
}
