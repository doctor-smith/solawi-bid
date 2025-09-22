package org.solyton.solawi.bid.module.application.data

import org.solyton.solawi.bid.module.application.data.application.Application
import org.solyton.solawi.bid.module.application.data.module.Module
import org.solyton.solawi.bid.module.application.data.userapplication.UserApplications
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
}
