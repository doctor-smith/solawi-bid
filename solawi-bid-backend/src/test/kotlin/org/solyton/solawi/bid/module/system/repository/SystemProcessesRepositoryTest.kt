package org.solyton.solawi.bid.module.system.repository

import SystemProcessException
import org.evoleq.exposedx.test.runSimpleH2Test
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.solyton.solawi.bid.DbFunctional
import org.solyton.solawi.bid.module.banking.repository.UUID_ONE
import org.solyton.solawi.bid.module.system.schema.SystemProcessesTable
import kotlin.test.assertEquals

class SystemProcessesRepositoryTest {

    val tables = arrayOf(
        SystemProcessesTable
    )

    @DbFunctional@Test
    fun createSystemProcess() = runSimpleH2Test(*tables) {
        val name = "name"
        val description = "description"

        val systemProcess = createSystemProcess(
            name, description
        )

        assertEquals(name, systemProcess.name)
        assertEquals(description, systemProcess.description)
    }

    @DbFunctional@Test
    fun createSystemProcessFailDueToDuplicateName() = runSimpleH2Test(*tables) {
        val name = "name"
        val description = "description"

        createSystemProcess(
            name, "$description-1"
        )

        assertThrows<SystemProcessException.DuplicateProcessName> {
            createSystemProcess(name, "$description-2")
        }
    }



    @DbFunctional@Test
    fun validatedSystemProcessByName() = runSimpleH2Test(*tables) {
        val name = "name"
        val description = "description"

        createSystemProcess(
            name, description
        )

        val systemProcess = assertDoesNotThrow { validatedSystemProcess(name) }
        assertEquals(description, systemProcess.description)
    }

    @DbFunctional@Test
    fun validatedSystemProcessById() = runSimpleH2Test(*tables) {
        val name = "name"
        val description = "description"

        val id = createSystemProcess(
            name, description
        ).id.value

        val systemProcess = assertDoesNotThrow { validatedSystemProcess(id) }
        assertEquals(name, systemProcess.name)
        assertEquals(description, systemProcess.description)

    }

    @DbFunctional@Test
    fun updateSystemProcess() = runSimpleH2Test(*tables) {
        val name = "name"
        val description = "description"

        val systemProcess = createSystemProcess(
            name, "$description-1"
        )

        val newName = "newName"
        val newDescription = "newDescription"

        val updated = updateSystemProcess(
            systemProcess.id.value,
            newName,
            newDescription
        )
        assertEquals(newName, updated.name)
        assertEquals(newDescription, updated.description)
    }

    @DbFunctional@Test
    fun updateSystemProcessFailDueToNonExistence() = runSimpleH2Test(*tables) {
        assertThrows<SystemProcessException.NoSuchProcess> {
            updateSystemProcess(
                UUID_ONE,
                "name",
                "description"
            )
        }
    }

    @DbFunctional@Test
    fun updateSystemProcessFailDueToDuplicateName() = runSimpleH2Test(*tables) {
        val name = "name"
        val description = "description"

        val systemProcess = createSystemProcess(
            name, "$description-1"
        )

        val newName = name
        val newDescription = "newDescription"

        assertThrows<SystemProcessException.DuplicateProcessName> {
            updateSystemProcess(
                systemProcess.id.value,
                newName,
                newDescription
            )
        }
    }

    @DbFunctional@Test
    fun deleteSystemProcess() = runSimpleH2Test(*tables) {
        val name = "name"
        val description = "description"

        createSystemProcess(
            name, description
        )

        deleteSystemProcess(name)
        assertThrows<SystemProcessException.NoSuchProcess> {
            validatedSystemProcess(name)
        }
    }
}
