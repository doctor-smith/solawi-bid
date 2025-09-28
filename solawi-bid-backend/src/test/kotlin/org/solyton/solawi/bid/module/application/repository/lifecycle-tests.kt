package org.solyton.solawi.bid.module.application.repository

import org.evoleq.exposedx.test.runSimpleH2Test
import org.evoleq.uuid.UUID_ZERO
import org.joda.time.DateTime
import org.joda.time.Duration
import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.DbFunctional
import org.solyton.solawi.bid.module.application.exception.ApplicationException
import org.solyton.solawi.bid.module.application.schema.*
import org.solyton.solawi.bid.module.permission.schema.ContextsTable
import org.solyton.solawi.bid.module.permission.schema.repository.createRootContext
import java.util.*
import kotlin.test.*

class LifecycleTests {
    @DbFunctional@Test
    fun createLifecycleStage() = runSimpleH2Test(
        LifecycleStagesTable
    ){
        val now = DateTime.now()

        val lifecycleStage = createLifecycleStage(
            "Test_StAge", "D", UUID_ZERO
        )

        assertEquals("TEST_STAGE", lifecycleStage.name)
        assertEquals("D", lifecycleStage.description)
        assertEquals(UUID_ZERO, lifecycleStage.createdBy)
        assertTrue { lifecycleStage.createdAt >= now.minus(Duration.millis(60_000))}
        assertNull(lifecycleStage.modifiedAt)
        assertNull(lifecycleStage.modifiedBy)
    }

    @DbFunctional@Test
    fun cannotCreateLifecycleStageWithDuplicateName() =runSimpleH2Test(
        LifecycleStagesTable
    ) {
        var resultException: Exception? = null
        val name = "TEST_STAGE"
        createLifecycleStage(
            name,
            "D",
            UUID_ZERO
        )
        try{
            createLifecycleStage(
                name,
                "D2",
                UUID_ZERO
            )
        } catch (exception: Exception) {
            resultException = exception
        }
        assertNotNull(resultException)
        assertIs<ApplicationException.DuplicateLifecycleStage>(resultException)
    }

    @DbFunctional@Test
    fun updateLifecycleStage() = runSimpleH2Test(
        LifecycleStagesTable
    ) {
        val now = DateTime.now()
        val lifecycleStage = createLifecycleStage(
            "TEST_STAGE",
            "D",
            UUID_ZERO
        )

        val newName = "NEW_TEST_STAGE"
        val newDescription = "NEW_D"
        val modifier = UUID.randomUUID()

        val updatedLifecycleStage = updateLifecycleStage(
            lifecycleStage.id.value,
            newName,
            newDescription,
            modifier
        )

        assertEquals(lifecycleStage.createdAt, updatedLifecycleStage.createdAt)
        assertEquals(lifecycleStage.createdBy, updatedLifecycleStage.createdBy)

        assertEquals(newName, updatedLifecycleStage.name)
        assertEquals(newDescription, updatedLifecycleStage.description)
        assertEquals(modifier, updatedLifecycleStage.modifiedBy)
        assertNotNull(updatedLifecycleStage.modifiedAt)
        assertTrue { updatedLifecycleStage.modifiedAt!! >= now.minus(Duration.millis((60_000))) }
    }

     @DbFunctional@Test
     fun cannotUpdateNonExistingLifecycleStage() = runSimpleH2Test(
         LifecycleStagesTable
     ) {
         var resultException: Exception? = null
         try{
             updateLifecycleStage(UUID_ZERO, "", "", UUID_ZERO)
         } catch (exception: Exception) {
             resultException = exception
         }
         assertNotNull(resultException)
         assertIs<ApplicationException.NoSuchLifecycleStage>(resultException)
     }

    @DbFunctional@Test
    fun cannotUpdateLifecycleStageWithDuplicateName() = runSimpleH2Test(
        LifecycleStagesTable
    ) {
        var resultException: Exception? = null
        createLifecycleStage(
            "TEST_STAGE_1",
            "D",
            UUID_ZERO
        )
        val stage = createLifecycleStage(
            "TEST_STAGE_2",
            "D",
            UUID_ZERO
        )
        try {
            updateLifecycleStage(
                stage.id.value,
                "TEST_STAGE_1",
                "D",
                UUID.randomUUID()
            )
        } catch (exception: Exception) {
            resultException = exception
        }

        assertNotNull(resultException)
        assertIs<ApplicationException.DuplicateLifecycleStage>(resultException)
    }


    @DbFunctional@Test
    fun createLifecycleTransition() = runSimpleH2Test(
        LifecycleStagesTable,
        LifecycleTransitionsTable
    ) {
        val now = DateTime.now()
        val from = createLifecycleStage(
            "FROM_STAGE", "D", UUID_ZERO
        )
        val to = createLifecycleStage(
            "TO_STAGE", "D", UUID_ZERO
        )
        val transition = createLifecycleTransition(
            from.id.value,
            to.id.value,
            "D",
            UUID_ZERO
        )
        assertEquals(from, transition.from)
        assertEquals(to, transition.to)
        assertEquals("D", transition.description)
        assertNull(transition.modifiedAt)
        assertNull(transition.modifiedBy)
        assertEquals(UUID_ZERO, transition.createdBy)
        assertTrue{now >= transition.createdAt.minus(Duration.millis(60_000))}
    }

    @DbFunctional@Test
    fun computeTransitionTargetOfLifeCycleStage() = runSimpleH2Test(
        LifecycleStagesTable,
        LifecycleTransitionsTable
    ) {
        val from = createLifecycleStage(
            "FROM_STAGE",
            "D",
            UUID_ZERO
        )
        val to = createLifecycleStage(
            "TO_STAGE",
            "D",
            UUID_ZERO
        )

        createLifecycleTransition(
            from.id.value,
            to.id.value,
            "D",
            UUID_ZERO
        )

        val transitionTarget = transitionTargetOf(from.id.value, to.id.value)
        assertEquals(to, transitionTarget)
    }

    @DbFunctional@Test
    fun cannotTransitionByNonexistingTransition_1() = runSimpleH2Test(
        LifecycleStagesTable,
        LifecycleTransitionsTable
    ) {
        var resultException: Exception? = null

        val stage = createLifecycleStage(
            "FROM_STAGE",
            "D",
            UUID_ZERO
        )

        try {
            transitionTargetOf(stage.id.value, UUID_ZERO)
        } catch(exception: Exception) {
            resultException = exception
        }
        assertNotNull(resultException)
        assertIs<ApplicationException.ForbiddenLifecycleTransition>(resultException)
    }

    @DbFunctional@Test
    fun cannotTransitionByNonexistingTransition_2() = runSimpleH2Test(
        LifecycleStagesTable,
        LifecycleTransitionsTable
    ) {
        var resultException: Exception? = null

        val stage = createLifecycleStage(
            "TO_STAGE",
            "D",
            UUID_ZERO
        )

        try {
            transitionTargetOf(UUID_ZERO, stage.id.value)
        } catch(exception: Exception) {
            resultException = exception
        }
        assertNotNull(resultException)
        assertIs<ApplicationException.ForbiddenLifecycleTransition>(resultException)
    }

    @DbFunctional@Test
    fun cannotTransitionByNonexistingTransition_3() = runSimpleH2Test(
        LifecycleStagesTable,
        LifecycleTransitionsTable
    ) {
        var resultException: Exception? = null

        try {
            transitionTargetOf(UUID.randomUUID(), UUID.randomUUID())
        } catch(exception: Exception) {
            resultException = exception
        }
        assertNotNull(resultException)
        assertIs<ApplicationException.ForbiddenLifecycleTransition>(resultException)
    }

    @DbFunctional@Test
    fun moveLifecycleStageOfApplication() = runSimpleH2Test(
        ApplicationsTable,
        UserApplications,
        LifecycleStagesTable,
        LifecycleTransitionsTable,
        ContextsTable
    ){

        val emptyContext = createRootContext("EMPTY")
        val now = DateTime.now()

        val lifecycleStage1 = createLifecycleStage(
            "STAGE_1", "D", UUID_ZERO
        )
        val lifecycleStage2 = createLifecycleStage(
            "STAGE_2", "D", UUID_ZERO
        )
        createLifecycleTransition(lifecycleStage1.id.value, lifecycleStage2.id.value,  "D", UUID_ZERO)

        val application = createApplication(
            "TEST_APP", "D", UUID_ZERO
        )
        val coolUserId = UUID.randomUUID()
        val modifierId = UUID.randomUUID()

        val userApplication = UserApplication.new {
            this.application = application
            userId = coolUserId
            lifecycleStage = lifecycleStage1
            createdBy = coolUserId
            createdAt = now
            context = emptyContext
        }

        val updatedUserApplication = moveLifecycleStage(
            userApplication, lifecycleStage2.id.value, modifierId
        )

        assertEquals(lifecycleStage2, updatedUserApplication.lifecycleStage)
        assertEquals(coolUserId, updatedUserApplication.userId)
        assertEquals(modifierId, updatedUserApplication.modifiedBy)
        assertNotNull(updatedUserApplication.modifiedAt)
        assertTrue{ updatedUserApplication.modifiedAt!! >= now.minus(Duration.millis(60_00)) }
    }


    @DbFunctional@Test
    fun moveLifecycleStageOfModule() = runSimpleH2Test(
        ModulesTable,
        UserModules,
        LifecycleStagesTable,
        LifecycleTransitionsTable
    ){

        val emptyContext = createRootContext("EMPTY")
        val now = DateTime.now()

        val lifecycleStage1 = createLifecycleStage(
            "STAGE_1", "D", UUID_ZERO
        )
        val lifecycleStage2 = createLifecycleStage(
            "STAGE_2", "D", UUID_ZERO
        )
        createLifecycleTransition(lifecycleStage1.id.value, lifecycleStage2.id.value,  "D", UUID_ZERO)

        val application = createApplication(
            "TEST_APP", "D", UUID_ZERO
        )
        val module = createModule("TEST_MODULE", "D", application.id.value, UUID_ZERO)

        val coolUserId = UUID.randomUUID()
        val modifierId = UUID.randomUUID()

        val userModule = UserModule.new {
            this.module = module
            userId = coolUserId
            lifecycleStage = lifecycleStage1
            createdBy = coolUserId
            createdAt = now
            context = emptyContext
        }

        val updatedUserModule = moveLifecycleStage(
            userModule, lifecycleStage2.id.value, modifierId
        )

        assertEquals(lifecycleStage2, updatedUserModule.lifecycleStage)
        assertEquals(coolUserId, updatedUserModule.userId)
        assertEquals(modifierId, updatedUserModule.modifiedBy)
        assertNotNull(updatedUserModule.modifiedAt)
        assertTrue{ updatedUserModule.modifiedAt!! >= now.minus(Duration.millis(60_00)) }
    }
}
