package org.solyton.solawi.bid.module.application.schema

import org.evoleq.exposedx.test.runSimpleH2Test
import org.evoleq.uuid.UUID_ZERO
import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.Schema
import org.solyton.solawi.bid.module.application.schema.ApplicationEntity
import org.solyton.solawi.bid.module.application.schema.ModuleEntity
import kotlin.test.assertEquals

class ApplicationSchemaTests {

    @Schema@Test
    fun appsAndModules() = runSimpleH2Test(
        ApplicationsTable,
        ModulesTable
    ) {
        val app = ApplicationEntity.new {
            name = "TestApplication"
            description = "Description"
            createdBy= UUID_ZERO
        }

        val module1 = ModuleEntity.new {
            name = "TestModule1"
            description = "module-1-1description"
            application = app
            createdBy = UUID_ZERO
        }

        val module2 = ModuleEntity.new {
            name = "TestModule2"
            description = "module-2-1description"
            application = app
            createdBy = UUID_ZERO
        }


        assertEquals(app, module1.application)
        assertEquals(app, module2.application)
        assertEquals(2, app.modules.count())
    }
}
