package org.solyton.solawi.bid.module.processes

import org.evoleq.math.Get
import org.evoleq.math.dispatch
import org.evoleq.math.emit
import org.evoleq.math.times
import org.evoleq.optics.transform.*
import org.jetbrains.compose.web.testutils.ComposeWebExperimentalTestsApi
import org.jetbrains.compose.web.testutils.runTest
import org.solyton.solawi.bid.module.process.data.process.Process
import org.solyton.solawi.bid.module.process.data.process.ProcessState
import org.solyton.solawi.bid.module.process.data.processes.IsActive
import org.solyton.solawi.bid.module.process.data.processes.IsFinished
import org.solyton.solawi.bid.module.process.data.processes.IsInactive
import org.solyton.solawi.bid.module.process.data.processes.IsNotActive
import org.solyton.solawi.bid.module.process.data.processes.IsNotFinished
import org.solyton.solawi.bid.module.process.data.processes.IsNotRegistered
import org.solyton.solawi.bid.module.process.data.processes.IsRegistered
import org.solyton.solawi.bid.module.process.data.processes.Processes
import org.solyton.solawi.bid.module.process.data.processes.Register
import org.solyton.solawi.bid.module.process.data.processes.SetStateOf
import org.solyton.solawi.bid.module.process.data.processes.SetStatesOf
import org.solyton.solawi.bid.module.process.data.processes.registry
import org.solyton.solawi.bid.test.storage.TestStorage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ProcessesTest {

    @OptIn(ComposeWebExperimentalTestsApi::class)
    @Test
    fun registerProcessTest() = runTest {

        composition {
            val storage = TestStorage(Processes())
            (storage * Register dispatch Process("process"))

            val storedProcess = (storage * registry).read()["process"]

            assertNotNull(storedProcess)
            assertEquals(
                Process("process"),
                storedProcess
            )
        }
    }

    @OptIn(ComposeWebExperimentalTestsApi::class)
    @Test
    fun isRegisterProcessTest() = runTest {

        composition {
            val storage = TestStorage(Processes())
            (storage * Register dispatch Process("process"))

            val isRegistered = (storage * IsRegistered("process")).emit()

            assertTrue(isRegistered)
        }
    }

    @OptIn(ComposeWebExperimentalTestsApi::class)
    @Test
    fun isNotRegisterProcessTest() = runTest {

        composition {
            val storage = TestStorage(Processes())
            (storage * Register dispatch Process("process"))

            val isRegistered = (storage * IsNotRegistered("process2")).emit()

            assertTrue(isRegistered)
        }
    }

    @OptIn(ComposeWebExperimentalTestsApi::class)
    @Test
    fun getProcessTest() = runTest {

        composition {
            val storage = TestStorage(Processes())
            (storage * Register dispatch Process("process"))

            val storedProcess = (storage * registry * Get("process")).emit()

            assertNotNull(storedProcess)
            assertEquals(
                Process("process"),
                storedProcess
            )
        }
    }



    @OptIn(ComposeWebExperimentalTestsApi::class)
    @Test
    fun setStateOfTest() = runTest {
        composition {
            val storage = TestStorage(Processes())
            (storage * Register dispatch Process("process"))

            val newState = ProcessState.Finished
            storage * SetStateOf("process") dispatch newState

            val storedProcess = (storage * registry * Get("process")).emit()
            assertNotNull(storedProcess)
            assertEquals(newState, storedProcess.state)
        }
    }

    @OptIn(ComposeWebExperimentalTestsApi::class)
    @Test
    fun setStatesOfTest() = runTest {
        composition {
            val storage = TestStorage(Processes())
            (storage * Register dispatch Process("process1"))
            (storage * Register dispatch Process("process2"))
            (storage * Register dispatch Process("process3"))

            val newState = ProcessState.Finished
            storage * SetStatesOf("process1", "process2") dispatch newState

            val storedProcess1 = (storage * registry * Get("process1")).emit()
            assertNotNull(storedProcess1)
            assertEquals(newState, storedProcess1.state)

            val storedProcess2 = (storage * registry * Get("process2")).emit()
            assertNotNull(storedProcess2)
            assertEquals(newState, storedProcess2.state)

            val storedProcess3 = (storage * registry * Get("process3")).emit()
            assertNotNull(storedProcess3)
            assertEquals(ProcessState.Active, storedProcess3.state)
        }
    }


    @OptIn(ComposeWebExperimentalTestsApi::class)
    @Test
    fun isActiveStateTest() = runTest {
        composition {
            val storage = TestStorage(Processes())
            (storage * Register dispatch Process("process"))

            val newState = ProcessState.Active
            storage * SetStateOf("process") dispatch newState

            assertTrue { (storage * registry * Get("process") * IsActive).emit()  }
        }
    }

    @OptIn(ComposeWebExperimentalTestsApi::class)
    @Test
    fun isInActiveStateTest() = runTest {
        composition {
            val storage = TestStorage(Processes())
            (storage * Register dispatch Process("process"))

            val newState = ProcessState.Inactive
            storage * SetStateOf("process") dispatch newState

            assertTrue { (storage * registry * Get("process") * IsInactive).emit()  }
        }
    }

    @OptIn(ComposeWebExperimentalTestsApi::class)
    @Test
    fun isNotActiveStateTest1() = runTest {
        composition {
            val storage = TestStorage(Processes())
            (storage * Register dispatch Process("process"))

            val newState = ProcessState.Inactive
            storage * SetStateOf("process") dispatch newState

            assertTrue { (storage * registry * Get("process") * IsNotActive).emit()  }
        }
    }

    @OptIn(ComposeWebExperimentalTestsApi::class)
    @Test
    fun isNotActiveStateTest2() = runTest {
        composition {
            val storage = TestStorage(Processes())
            (storage * Register dispatch Process("process"))

            val newState = ProcessState.Finished
            storage * SetStateOf("process") dispatch newState

            assertTrue { (storage * registry * Get("process") * IsNotActive).emit()  }
        }
    }

    @OptIn(ComposeWebExperimentalTestsApi::class)
    @Test
    fun isFinishedStateTest() = runTest {
        composition {
            val storage = TestStorage(Processes())
            (storage * Register dispatch Process("process"))

            val newState = ProcessState.Finished
            storage * SetStateOf("process") dispatch newState

            assertTrue { (storage * registry * Get("process") * IsFinished).emit()  }
        }
    }

    @OptIn(ComposeWebExperimentalTestsApi::class)
    @Test
    fun isNoFinishedStateTest1() = runTest {
        composition {
            val storage = TestStorage(Processes())
            (storage * Register dispatch Process("process"))

            val newState = ProcessState.Active
            storage * SetStateOf("process") dispatch newState

            assertTrue { (storage * registry * Get("process") * IsNotFinished).emit()  }
        }
    }

    @OptIn(ComposeWebExperimentalTestsApi::class)
    @Test
    fun isNoFinishedStateTest2() = runTest {
        composition {
            val storage = TestStorage(Processes())
            (storage * Register dispatch Process("process"))

            val newState = ProcessState.Inactive
            storage * SetStateOf("process") dispatch newState

            assertTrue { (storage * registry * Get("process") * IsNotFinished).emit()  }
        }
    }
}
