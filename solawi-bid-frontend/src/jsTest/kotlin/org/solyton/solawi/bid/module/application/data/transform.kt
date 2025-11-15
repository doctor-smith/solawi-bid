package org.solyton.solawi.bid.module.application.data

import org.solyton.solawi.bid.module.application.data.application.Application
import org.solyton.solawi.bid.module.application.data.module.Module
import org.solyton.solawi.bid.module.application.data.organizationrelation.ApplicationOrganizationRelation
import org.solyton.solawi.bid.module.application.data.userapplication.UserApplications
import org.solyton.solawi.bid.module.permissions.data.relations.ContextRelation
import kotlin.test.Test
import kotlin.test.assertEquals

class TransformationTests {

    @Test fun userApplicationToDomainTypeTest() {
        val apiApplications = ApiApplications(
            listOf(
                ApiApplication(
                    id = "ID_1",
                    name = "APP_1",
                    description = "D_1",
                    lifecycleStage = LifecycleStage.Empty,
                    modules = listOf(
                        ApiModule(
                            id = "ID_1",
                            name = "MOD_1",
                            description = "D_1",
                            lifecycleStage = LifecycleStage.Empty
                        ),
                        ApiModule(
                            id = "ID_3",
                            name = "MOD_3",
                            description = "D_3",
                            lifecycleStage = LifecycleStage.Empty
                        )
                    )
                ),
                ApiApplication(
                    id = "ID_2",
                    name = "APP_2",
                    description = "D_2",
                    lifecycleStage = LifecycleStage.Empty,
                    modules = listOf(
                        ApiModule(
                            id = "ID_2",
                            name = "MOD_2",
                            description = "D_2",
                            lifecycleStage = LifecycleStage.Empty
                        )
                    )
                )
            )
        )
        val expectedApplications = listOf(UserApplications(
            userId = "USER_ID",
            applications = listOf(
                Application(
                    id = "ID_1",
                    name = "APP_1",
                    state = LifecycleStage.Empty,
                    modules = listOf(
                        Module(
                            id = "ID_1",
                            name = "MOD_1",
                            state = LifecycleStage.Empty
                        ),
                        Module(
                            id = "ID_3",
                            name = "MOD_3",
                            state = LifecycleStage.Empty
                        )
                    )
                ),
                Application(
                    id = "ID_2",
                    name = "APP_2",
                    state = LifecycleStage.Empty,
                    modules = listOf(
                        Module(
                            id = "ID_2",
                            name = "MOD_2",
                            state = LifecycleStage.Empty
                        )
                    )
                )
            )
        ))

        val result: List<UserApplications> = ApiUserApplications(mapOf("USER_ID" to apiApplications.list)).toDomainType()

        assertEquals(
            expectedApplications,
            result
        )
    }

    @Test fun applicationToDomainTypeTest() {
        val apiApplications = ApiApplications(
            listOf(
                ApiApplication(
                    id = "ID_1",
                    name = "APP_1",
                    description = "D_1",
                    lifecycleStage = LifecycleStage.Empty,
                    modules = listOf(
                        ApiModule(
                            id = "ID_1",
                            name = "MOD_1",
                            description = "D_1",
                            lifecycleStage = LifecycleStage.Empty
                        ),
                        ApiModule(
                            id = "ID_3",
                            name = "MOD_3",
                            description = "D_3",
                            lifecycleStage = LifecycleStage.Empty
                        )
                    )
                ),
                ApiApplication(
                    id = "ID_2",
                    name = "APP_2",
                    description = "D_2",
                    lifecycleStage = LifecycleStage.Empty,
                    modules = listOf(
                        ApiModule(
                            id = "ID_2",
                            name = "MOD_2",
                            description = "D_2",
                            lifecycleStage = LifecycleStage.Empty
                        )
                    )
                )
            )
        )
        val expectedApplications = listOf(
            Application(
                id = "ID_1",
                name = "APP_1",
                state = LifecycleStage.Empty,
                modules = listOf(
                    Module(
                        id = "ID_1",
                        name = "MOD_1",
                        state = LifecycleStage.Empty
                    ),
                    Module(
                        id = "ID_3",
                        name = "MOD_3",
                        state = LifecycleStage.Empty
                    )
                )
            ),
            Application(
                id = "ID_2",
                name = "APP_2",
                state = LifecycleStage.Empty,
                modules = listOf(
                    Module(
                        id = "ID_2",
                        name = "MOD_2",
                        state = LifecycleStage.Empty
                    )
                )
            )
        )
        val domainApplications = apiApplications.toDomainType()

        assertEquals(
            expectedApplications,
            domainApplications
        )
    }

    @Test fun applicationToDomainType() {
        val apiApplication = ApiApplication(
            id = "ID_1",
            name = "APP_1",
            description = "D_1",
            lifecycleStage = LifecycleStage.Empty,
            modules = listOf(
                ApiModule(
                    id = "ID_1",
                    name = "MOD_1",
                    description = "D_1",
                    lifecycleStage = LifecycleStage.Empty
                ),
                ApiModule(
                    id = "ID_3",
                    name = "MOD_3",
                    description = "D_3",
                    lifecycleStage = LifecycleStage.Empty
                )
            )
        )
        val expectedApplication = Application(
            id = "ID_1",
            name = "APP_1",
            state = LifecycleStage.Empty,
            modules = listOf(
                Module(
                    id = "ID_1",
                    name = "MOD_1",
                    state = LifecycleStage.Empty
                ),
                Module(
                    id = "ID_3",
                    name = "MOD_3",
                    state = LifecycleStage.Empty
                )
            )
        )

        val result = apiApplication.toDomainType()
        assertEquals(
            expectedApplication,
            result
        )
    }

    @Test fun moduleToDomainType() {
        val apiModule = ApiModule(
            id = "ID_1",
            name = "MOD_1",
            description = "D_1",
            lifecycleStage = LifecycleStage.Empty
        )

        val expectedModule = Module(
            id = "ID_1",
            name = "MOD_1",
            state = LifecycleStage.Empty
        )

        assertEquals(
            expectedModule,
            apiModule.toDomainType()
        )
    }

    @Test fun moduleContextRelationsToDomainTypeTest() {
        val moduleContextRelations = ApiModuleContextRelations(
            all = listOf(
                ApiModuleContextRelation(
                    "moduleId_1","contextId_1"
                ),
                ApiModuleContextRelation(
                    "moduleId_2","contextId_2"
                )
            )
        )

        val expectedContextRelations = listOf(
            ContextRelation(
                "contextId_1","moduleId_1"
            ),
            ContextRelation(
                "contextId_2","moduleId_2"
            )
        )

        val result = moduleContextRelations.toDomainType()

        assertEquals(
            expectedContextRelations,
            result
        )
    }

    @Test fun moduleContextRelationToDomainTypeTest() {
        val contextId = "contextId"
        val moduleId = "moduleId"
        val moduleContextRelation = ApiModuleContextRelation(
            moduleId = moduleId,
            contextId = contextId
        )

        val expectedContextRelation = ContextRelation(
            contextId = contextId,
            relatedId = moduleId
        )

        val result = moduleContextRelation.toDomainType()

        assertEquals(
            expectedContextRelation,
            result
        )
    }

    @Test fun applicationContextRelationsToDomainTypeTest() {
        val applicationContextRelations = ApiApplicationContextRelations(
            all = listOf(
                ApiApplicationContextRelation(
                    "moduleId_1","contextId_1"
                ),
                ApiApplicationContextRelation(
                    "moduleId_2","contextId_2"
                )
            )
        )

        val expectedContextRelations = listOf(
            ContextRelation(
                "contextId_1","moduleId_1"
            ),
            ContextRelation(
                "contextId_2","moduleId_2"
            )
        )

        val result = applicationContextRelations.toDomainType()

        assertEquals(
            expectedContextRelations,
            result
        )
    }

    @Test fun applicationContextRelationToDomainTypeTest() {
        val contextId = "contextId"
        val applicationId = "applicationId"
        val applicationContextRelation = ApiApplicationContextRelation(
            applicationId = applicationId,
            contextId = contextId
        )

        val expectedContextRelation = ContextRelation(
            contextId = contextId,
            relatedId = applicationId
        )

        val result = applicationContextRelation.toDomainType()

        assertEquals(
            expectedContextRelation,
            result
        )
    }

    @Test fun applicationOrganizationRelationToDomainTypeTest() {
        val organizationId = "organizationId"
        val applicationId = "applicationId"
        val moduleIds = listOf("module_1, module_2")

        val apiApplicationOrganizationRelation = ApiApplicationOrganizationRelation(
            applicationId, organizationId, moduleIds
        )

        val expected = ApplicationOrganizationRelation(
            applicationId, organizationId, moduleIds
        )

        assertEquals(expected, apiApplicationOrganizationRelation.toDomainType())
    }

    @Test fun applicationOrganizationRelationsToDomainTypeTest() {
        val organizationId = "organizationId"
        val applicationId = "applicationId"
        val moduleIds = listOf("module_1, module_2")

        val apiApplicationOrganizationRelations = ApiApplicationOrganizationRelations(
            listOf(
                ApiApplicationOrganizationRelation(
                    applicationId + "1",
                    organizationId+"1",
                    moduleIds
                ),
                ApiApplicationOrganizationRelation(
                    applicationId + "2",
                    organizationId+"2",
                    moduleIds
                )
            )
        )

        val expected = listOf(
            ApplicationOrganizationRelation(
                applicationId + "1",
                organizationId+"1",
                moduleIds
            ),
            ApplicationOrganizationRelation(
                applicationId + "2",
                organizationId+"2",
                moduleIds
            )
        )

        assertEquals(expected, apiApplicationOrganizationRelations.toDomainType())
    }
}
