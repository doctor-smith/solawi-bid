package org.solyton.solawi.bid.module.application.repository

import org.evoleq.exposedx.test.runSimpleH2Test
import org.evoleq.uuid.UUID_ZERO
import org.joda.time.DateTime
import org.joda.time.Duration
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.DbFunctional
import org.solyton.solawi.bid.module.application.exception.ApplicationException
import org.solyton.solawi.bid.module.application.schema.ApplicationsTable
import org.solyton.solawi.bid.module.application.schema.ModulesTable
import org.solyton.solawi.bid.module.application.schema.UserApplications
import org.solyton.solawi.bid.module.application.schema.UserModules
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ApplicationRepositoryTests {
    val tables = arrayOf(
        ApplicationsTable,
        ModulesTable,
        UserApplications,
        UserModules
    )

    @DbFunctional@Test
    fun createApplication() = runSimpleH2Test(*tables) {
        val now = DateTime.now()
        val app = createApplication("TestApp", "d", UUID_ZERO)


        assertEquals("TestApp", app.name)
        assertEquals("d", app.description)

        assertTrue {   app.createdAt >= now.minus(Duration.millis(2_000)) }
        assertEquals(UUID_ZERO, app.createdBy)
        assertNull(app.modifiedBy)
        assertNull(app.modifiedAt)

    }

    @DbFunctional@Test
    fun updateApplicationWithoutChanges() = runSimpleH2Test(*tables) {
        val now = DateTime.now()
        val app = createApplication("TestApp", "d", UUID_ZERO)

        assertTrue { app.createdAt >= now.minus(Duration.millis(2_000)) }
        assertEquals(UUID_ZERO, app.createdBy)
        assertNull(app.modifiedBy)
        assertNull(app.modifiedAt)

        val userId = UUID.randomUUID()

        val newApp1 = updateApplication(app.id.value, app.name, app.description, userId)
        assertEquals(app, newApp1)
    }

    @DbFunctional@Test
    fun createAndUpdateApplication() = runSimpleH2Test(*tables) {
        val now = DateTime.now()
        val app = createApplication("TestApp", "d", UUID_ZERO)

        assertTrue {   app.createdAt >= now.minus(Duration.millis(2_000)) }
        assertEquals(UUID_ZERO, app.createdBy)
        assertNull(app.modifiedBy)
        assertNull(app.modifiedAt)

        val userId = UUID.randomUUID()

        val newApp1 = updateApplication(app.id.value, app.name, app.description, userId)
        assertEquals(app, newApp1)


        val modifyDate = DateTime.now()
        val newApp2 = updateApplication(app.id.value, "TestApp 2", app.description, userId)

        assertNotNull(newApp2.modifiedAt)
        assertTrue{newApp2.modifiedAt!! >= modifyDate}
        assertEquals(userId, newApp2.modifiedBy)
        assertEquals("TestApp 2", newApp2.name)
    }

    @DbFunctional@Test
    fun cannotCreateTwoAppsWithSameName() = runSimpleH2Test(*tables) {
        var resultException: Exception? = null
        val app1 = createApplication("TEST_APP", "d", UUID_ZERO)
        try {
            createApplication("TEST_APP", "d", UUID_ZERO)
        } catch (exception: Exception) {
            resultException = exception
        }
        assertIs<ApplicationException.DuplicateApplicationName>(resultException)
        assertTrue(resultException.message.contains(app1.name))
    }

    @DbFunctional@Test
    fun cannotUpdateApplicationWithDuplicateName() = runSimpleH2Test(*tables) {
        var resultException: Exception? = null
        val app1 = createApplication("TEST_APP", "d", UUID_ZERO)
        try {
            val app2 = createApplication("TEST_APP_1", "d", UUID_ZERO)
            updateApplication(app2.id.value, app1.name, app2.description, app1.createdBy)
        } catch (exception: Exception) {
            resultException = exception
        }
        assertIs<ApplicationException.DuplicateApplicationName>(resultException)
        assertTrue(resultException.message.contains(app1.name))
    }

    @DbFunctional@Test
    fun createModule() = runSimpleH2Test(*tables) {
        val app = createApplication("TEST_APP", "d", UUID_ZERO)

        val now = DateTime.now()

        val module = createModule("TEST_MODULE", "D", app.id.value, UUID_ZERO)

        assertTrue{module.createdAt >= now.minus(Duration.millis(2_000))}
        assertEquals(UUID_ZERO, module.createdBy)
        assertEquals("TEST_MODULE", module.name)
        assertEquals("D", module.description)
        assertEquals(app, module.application)
        assertNull(module.modifiedAt)
        assertNull(module.modifiedBy)
    }

    @DbFunctional@Test
    fun updateModule() = runSimpleH2Test(*tables) {
        val app = createApplication("TEST_APP", "d", UUID_ZERO)

        val now = DateTime.now()

        val module = createModule("TEST_MODULE", "D", app.id.value, UUID_ZERO)

        assertTrue{module.createdAt >= now.minus(Duration.millis(2_000))}
        assertEquals(UUID_ZERO, module.createdBy)
        assertEquals("TEST_MODULE", module.name)
        assertEquals("D", module.description)
        assertEquals(app, module.application)
        assertNull(module.modifiedAt)
        assertNull(module.modifiedBy)

        val userId = UUID.randomUUID()
        val module2 = updateModule(module.id.value, "TEST_MODULE_2", "D_2", app.id.value, userId)

        assertTrue { module2.modifiedAt!! >= now.minus(Duration.millis(2_000)) }
        assertEquals(userId, module2.modifiedBy)
        assertEquals("TEST_MODULE_2", module2.name)
        assertEquals("D_2", module2.description)
        assertEquals(app, module.application)

    }

    @DbFunctional@Test
    fun cannotCreateModuleWithDuplicateName() = runSimpleH2Test(*tables) {
        var resultException: Exception? = null

        val app = createApplication("TEST_APP", "d", UUID_ZERO)
        val module = createModule("TEST_MODULE", "D", app.id.value, UUID_ZERO)

        try {
            createModule("TEST_MODULE", "D_2", app.id.value, UUID_ZERO)
        } catch (e: Exception) {
            resultException = e
        }

        assertIs<ApplicationException.DuplicateModuleName>(resultException)
    }

    @DbFunctional@Test
    fun updateModuleApp() = runSimpleH2Test(*tables) {
        val app = createApplication("TEST_APP", "d", UUID_ZERO)
        val targetApp = createApplication("TARGET_TEST_APP", "D", UUID_ZERO)
        val now = DateTime.now()

        val module = createModule("TEST_MODULE", "D", app.id.value, UUID_ZERO)
        val module2 = updateModule(module.id.value, module.name, module.description, targetApp.id.value, UUID_ZERO)

        assertEquals(targetApp, module2.application)
    }

    @DbFunctional@Test
    fun cannotUpdateModuleWithDuplicateName() = runSimpleH2Test(*tables) {
        var resultException: Exception? = null

        val app = createApplication("TEST_APP", "d", UUID_ZERO)
        val targetApp = createApplication("TARGET_TEST_APP", "D", UUID_ZERO)
        createModule("TEST_MODULE", "D", targetApp.id.value, UUID_ZERO)

        val module = createModule("TEST_MODULE", "D", app.id.value, UUID_ZERO)
        try{
            updateModule(module.id.value, module.name, module.description,targetApp.id.value, UUID_ZERO)
        } catch (e: Exception) {
            resultException = e
        }

        assertIs<ApplicationException.DuplicateModuleName>(resultException)
    }
}
