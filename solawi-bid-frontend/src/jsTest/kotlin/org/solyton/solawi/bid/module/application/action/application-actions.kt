package org.solyton.solawi.bid.module.application.action

import org.evoleq.ktorx.result.on
import org.evoleq.math.emit
import org.evoleq.math.write
import org.evoleq.optics.storage.ActionDispatcher
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.testutils.ComposeWebExperimentalTestsApi
import org.jetbrains.compose.web.testutils.runTest
import org.solyton.solawi.bid.application.data.transform.application.management.applicationManagementModule
import org.solyton.solawi.bid.module.application.data.*
import org.solyton.solawi.bid.module.application.data.management.ApplicationManagement
import org.solyton.solawi.bid.module.application.data.management.availableApplications
import org.solyton.solawi.bid.module.application.data.management.personalApplicationContextRelations
import org.solyton.solawi.bid.module.application.data.management.personalApplications
import org.solyton.solawi.bid.module.application.data.management.personalModuleContextRelations
import org.solyton.solawi.bid.module.application.data.management.userApplications
import org.solyton.solawi.bid.module.i18n.data.Environment
import org.solyton.solawi.bid.module.i18n.data.I18N
import org.solyton.solawi.bid.module.i18n.data.I18nResources
import org.solyton.solawi.bid.test.storage.TestStorage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ApplicationActionsTest {

    val applicationManagement = ApplicationManagement(
        actions = ActionDispatcher {  },
        environment = Environment(
            I18nResources(
                url = "",
                port = 0
            )
        ),
        i18n = I18N(),

    )

    @OptIn(ComposeWebExperimentalTestsApi::class)
    @Test
    fun readApplicationsTest() = runTest{

        val action = readApplications

        val apiApplications = ApiApplications(
            list = listOf(ApiApplication(
                id = "id_1",
                name = "APPLICATION_1",
                description = "",
                lifecycleStage = LifecycleStage.Registered,
                modules = listOf(
                    Module(
                        id = "id_1",
                        name = "MODULE_1",
                        description = "",
                        lifecycleStage = LifecycleStage.Registered
                    )
                )
            )),
        )
        val domainApplications = apiApplications.toDomainType()

        composition {
            val storage = TestStorage() * applicationManagementModule

            assertIs<ReadApplications>((storage * action.reader).emit())
            assertEquals(0,(storage * availableApplications).read().size)


            (storage * action.writer).write(apiApplications) on Unit
            val applications = (storage * availableApplications).read()

            assertEquals(1,applications.size)
            assertEquals(domainApplications, applications)
        }
    }

    @OptIn(ComposeWebExperimentalTestsApi::class)
    @Test
    fun readPersonalUserApplicationsTest() = runTest{

        val action = readPersonalApplications

        val apiApplications = ApiApplications(
            list = listOf(ApiApplication(
                id = "id_1",
                name = "APPLICATION_1",
                description = "",
                lifecycleStage = LifecycleStage.Registered,
                modules = listOf(
                    Module(
                        id = "id_1",
                        name = "MODULE_1",
                        description = "",
                        lifecycleStage = LifecycleStage.Registered
                    )
                )
            )),
        )
        val domainApplications = apiApplications.toDomainType()

        composition {
            val storage = TestStorage() * applicationManagementModule

            assertIs< ReadPersonalUserApplications>((storage * action.reader).emit())
            assertEquals(0,(storage * personalApplications).read().size)


            (storage * action.writer).write(apiApplications) on Unit
            val applications = (storage * personalApplications).read()

            assertEquals(1,applications.size)
            assertEquals(domainApplications, applications)
        }
    }


    @OptIn(ComposeWebExperimentalTestsApi::class)
    @Test
    fun readUserApplicationsTest() = runTest{

        val action = readUserApplications(listOf("USER_ID"))

        val apiApplications = listOf(ApiApplication(
                id = "id_1",
                name = "APPLICATION_1",
                description = "",
                lifecycleStage = LifecycleStage.Registered,
                modules = listOf(
                    Module(
                        id = "id_1",
                        name = "MODULE_1",
                        description = "",
                        lifecycleStage = LifecycleStage.Registered
                    )
                )
            ),
        )
        val apiUserApplications = ApiUserApplications(mapOf(
            "USER_ID" to apiApplications
        ))

        val domainApplications = apiUserApplications.toDomainType()

        composition {
            val storage = TestStorage() * applicationManagementModule

            assertIs<ReadUserApplications>((storage * action.reader).emit())
            assertEquals(ReadUserApplications(listOf("USER_ID")),(storage * action.reader).emit())
            assertEquals(0,(storage * userApplications).read().size)


            (storage * action.writer).write(apiUserApplications) on Unit
            val applications = (storage * userApplications).read()

            assertEquals(1,applications.size)
            assertEquals(domainApplications, applications)
        }
    }

    @OptIn(ComposeWebExperimentalTestsApi::class)
    @Test
    fun registerForApplicationsTest() = runTest{

        val action = registerForApplications(listOf("APP_ID"))

        val apiApplications = ApiApplications(
            list = listOf(ApiApplication(
                id = "id_1",
                name = "APPLICATION_1",
                description = "",
                lifecycleStage = LifecycleStage.Registered,
                modules = listOf(
                    Module(
                        id = "id_1",
                        name = "MODULE_1",
                        description = "",
                        lifecycleStage = LifecycleStage.Registered
                    )
                )
            )),
        )
        val domainApplications = apiApplications.toDomainType()

        composition {
            val storage = TestStorage() * applicationManagementModule

            assertIs<RegisterForApplications>((storage * action.reader).emit())
            assertEquals(RegisterForApplications(listOf("APP_ID")), (storage * action.reader).emit())
            assertEquals(0,(storage * personalApplications).read().size)


            (storage * action.writer).write(apiApplications) on Unit
            val applications = (storage * personalApplications).read()

            assertEquals(1,applications.size)
            assertEquals(domainApplications, applications)
        }
    }

    @OptIn(ComposeWebExperimentalTestsApi::class)
    @Test
    fun startTrialOfApplicationsTest() = runTest{

        val action = startTrialsOfApplications(listOf("APP_ID"))

        val apiApplications = ApiApplications(
            list = listOf(ApiApplication(
                id = "id_1",
                name = "APPLICATION_1",
                description = "",
                lifecycleStage = LifecycleStage.Registered,
                modules = listOf(
                    Module(
                        id = "id_1",
                        name = "MODULE_1",
                        description = "",
                        lifecycleStage = LifecycleStage.Registered
                    )
                )
            )),
        )
        val domainApplications = apiApplications.toDomainType()

        composition {
            val storage = TestStorage() * applicationManagementModule

            assertIs<StartTrialsOfApplications>((storage * action.reader).emit())
            assertEquals(StartTrialsOfApplications(listOf("APP_ID")), (storage * action.reader).emit())
            assertEquals(0,(storage * personalApplications).read().size)


            (storage * action.writer).write(apiApplications) on Unit
            val applications = (storage * personalApplications).read()

            assertEquals(1,applications.size)
            assertEquals(domainApplications, applications)
        }
    }

    @OptIn(ComposeWebExperimentalTestsApi::class)
    @Test
    fun subscribeApplicationsTest() = runTest{

        val action = subscribeApplications(listOf("APP_ID"))

        val apiApplications = ApiApplications(
            list = listOf(ApiApplication(
                id = "id_1",
                name = "APPLICATION_1",
                description = "",
                lifecycleStage = LifecycleStage.Registered,
                modules = listOf(
                    Module(
                        id = "id_1",
                        name = "MODULE_1",
                        description = "",
                        lifecycleStage = LifecycleStage.Registered
                    )
                )
            )),
        )
        val domainApplications = apiApplications.toDomainType()

        composition {
            val storage = TestStorage() * applicationManagementModule

            assertIs< SubscribeApplications>((storage * action.reader).emit())
            assertEquals(SubscribeApplications(listOf("APP_ID")), (storage * action.reader).emit())
            assertEquals(0,(storage * personalApplications).read().size)


            (storage * action.writer).write(apiApplications) on Unit
            val applications = (storage * personalApplications).read()

            assertEquals(1,applications.size)
            assertEquals(domainApplications, applications)
        }
    }


    @OptIn(ComposeWebExperimentalTestsApi::class)
    @Test
    fun registerForModulesTest() = runTest{

        val action = registerForModules(listOf("MODULE_ID"))

        val apiApplications = ApiApplications(
            list = listOf(ApiApplication(
                id = "id_1",
                name = "APPLICATION_1",
                description = "",
                lifecycleStage = LifecycleStage.Registered,
                modules = listOf(
                    Module(
                        id = "id_1",
                        name = "MODULE_1",
                        description = "",
                        lifecycleStage = LifecycleStage.Registered
                    )
                )
            )),
        )
        val domainApplications = apiApplications.toDomainType()

        composition {
            val storage = TestStorage() * applicationManagementModule

            assertIs< RegisterForModules>((storage * action.reader).emit())
            assertEquals(RegisterForModules(listOf("MODULE_ID")), (storage * action.reader).emit())
            assertEquals(0,(storage * personalApplications).read().size)


            (storage * action.writer).write(apiApplications) on Unit
            val applications = (storage * personalApplications).read()

            assertEquals(1,applications.size)
            assertEquals(domainApplications, applications)
        }
    }

    @OptIn(ComposeWebExperimentalTestsApi::class)
    @Test
    fun startTrialsOfModulesTest() = runTest{

        val action = startTrialsOfModules(listOf("MODULE_ID"))

        val apiApplications = ApiApplications(
            list = listOf(ApiApplication(
                id = "id_1",
                name = "APPLICATION_1",
                description = "",
                lifecycleStage = LifecycleStage.Registered,
                modules = listOf(
                    Module(
                        id = "id_1",
                        name = "MODULE_1",
                        description = "",
                        lifecycleStage = LifecycleStage.Registered
                    )
                )
            )),
        )
        val domainApplications = apiApplications.toDomainType()

        composition {
            val storage = TestStorage() * applicationManagementModule

            assertIs<StartTrialsOfModules>((storage * action.reader).emit())
            assertEquals(StartTrialsOfModules(listOf("MODULE_ID")), (storage * action.reader).emit())
            assertEquals(0,(storage * personalApplications).read().size)

            (storage * action.writer).write(apiApplications) on Unit
            val applications = (storage * personalApplications).read()

            assertEquals(1,applications.size)
            assertEquals(domainApplications, applications)
        }
    }

    @OptIn(ComposeWebExperimentalTestsApi::class)
    @Test
    fun subscribeModulesTest() = runTest{

        val action = subscribeModules(listOf("MODULE_ID"))

        val apiApplications = ApiApplications(
            list = listOf(ApiApplication(
                id = "id_1",
                name = "APPLICATION_1",
                description = "",
                lifecycleStage = LifecycleStage.Registered,
                modules = listOf(
                    Module(
                        id = "id_1",
                        name = "MODULE_1",
                        description = "",
                        lifecycleStage = LifecycleStage.Registered
                    )
                )
            )),
        )
        val domainApplications = apiApplications.toDomainType()

        composition {
            val storage = TestStorage() * applicationManagementModule

            assertIs<SubscribeModules>((storage * action.reader).emit())
            assertEquals(SubscribeModules(listOf("MODULE_ID")), (storage * action.reader).emit())
            assertEquals(0,(storage * personalApplications).read().size)


            (storage * action.writer).write(apiApplications) on Unit
            val applications = (storage * personalApplications).read()

            assertEquals(1,applications.size)
            assertEquals(domainApplications, applications)
        }
    }

    @OptIn(ComposeWebExperimentalTestsApi::class)
    @Test
    fun readModuleContextRelationsTest() = runTest{

        val action = readModuleContextRelations

        val apiModuleContextRelations = ApiModuleContextRelations(
            all = listOf(
                ApiModuleContextRelation(
                    "moduleId_1", "contextId_1"
                ),
                ApiModuleContextRelation(
                    "moduleId_2", "contextId_2"
                )
            ),
        )
        val domainApplications = apiModuleContextRelations.toDomainType()

        composition {
            val storage = TestStorage() * applicationManagementModule

            assertIs<ReadPersonalModuleContextRelations>((storage * action.reader).emit())
            assertEquals(0,(storage * personalModuleContextRelations).read().size)


            (storage * action.writer).write(apiModuleContextRelations) on Unit
            val applications = (storage * personalModuleContextRelations).read()

            assertEquals(2,applications.size)
            assertEquals(domainApplications, applications)
        }
    }

    @OptIn(ComposeWebExperimentalTestsApi::class)
    @Test
    fun readApplicationContextRelationsTest() = runTest{

        val action = readApplicationContextRelations

        val apiApplicationContextRelations = ApiApplicationContextRelations(
            all = listOf(
                ApiApplicationContextRelation(
                    "applicationId_1", "contextId_1"
                ),
                ApiApplicationContextRelation(
                    "applicationId_2", "contextId_2"
                )
            ),
        )
        val domainApplications = apiApplicationContextRelations.toDomainType()

        composition {
            val storage = TestStorage() * applicationManagementModule

            assertIs< ReadPersonalApplicationContextRelations>((storage * action.reader).emit())
            assertEquals(0,(storage * personalApplicationContextRelations).read().size)


            (storage * action.writer).write(apiApplicationContextRelations) on Unit
            val applications = (storage * personalApplicationContextRelations).read()

            assertEquals(2,applications.size)
            assertEquals(domainApplications, applications)
        }
    }
}
