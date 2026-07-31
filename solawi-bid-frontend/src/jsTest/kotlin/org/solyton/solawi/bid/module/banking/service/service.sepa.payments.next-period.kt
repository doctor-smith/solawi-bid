package org.solyton.solawi.bid.module.banking.service

import org.evoleq.kotlinx.date.now
import org.evoleq.uuid.NIL_UUID
import org.solyton.solawi.bid.module.banking.data.SepaCollectionId
import org.solyton.solawi.bid.module.banking.data.SepaMandateId
import org.solyton.solawi.bid.module.banking.data.SepaPaymentId
import org.solyton.solawi.bid.module.banking.data.sepa.PaymentExecutionStatus
import org.solyton.solawi.bid.module.banking.data.sepa.SepaSequenceType
import org.solyton.solawi.bid.module.banking.data.sepa.SuccessorKind
import org.solyton.solawi.bid.module.banking.data.sepa.payment.SepaPayment
import org.solyton.solawi.bid.module.banking.data.sepa.payment.SepaPaymentHistories
import org.solyton.solawi.bid.module.banking.data.sepa.payment.SepaPaymentHistory
import org.solyton.solawi.bid.test.UUID_1
import org.solyton.solawi.bid.test.UUID_2
import org.solyton.solawi.bid.test.UUID_3
import kotlin.test.Test
import kotlin.test.assertEquals


data class TestCase<Input, Result> (
    val name: String,
    val description: String,
    val data: Input,
    val expected: Result
)


class IsCandidateForNextPeriodPaymentTest {

    val forbiddenSeqTypes = listOf(
        SepaSequenceType.OOFF,
        SepaSequenceType.FNAL,
        SepaSequenceType.UNCLEAR,
    )
    data class Data(
        val payment: SepaPayment,
        val histories: SepaPaymentHistories,
    )

    fun SepaPaymentHistory.Initial.asHistories(): SepaPaymentHistories = SepaPaymentHistories(listOf(this))

    fun String.toSepaPaymentId(): SepaPaymentId = SepaPaymentId(this)

    val base = SepaPayment(
        SepaPaymentId(UUID_1),
        SepaMandateId(UUID_1),
        SepaCollectionId(UUID_1),
        100.0,
        now().date,
        SepaSequenceType.RCUR,
        PaymentExecutionStatus.CONFIRMED,
    )


    val cases = sequenceOf<TestCase<Data, Boolean>>(
        TestCase(
            name = "Case 1",
            description =
                """
                |Initial payment 
                |    status: ............: CONFIRMED
                |    failing predecessors: ∅  
                |    successors .........: ∅  
                |
                |Expectation: ✅ RETRY CANDIDATE         
                """.trimMargin(),
            data = Data(
                payment = base,
                histories = SepaPaymentHistory.Initial(
                    base.sepaPaymentId,
                    emptyList()
                ).asHistories(),
            ),
            expected = true
        ),
        TestCase(
            name = "Case 2",
            description = """
                |Initial payment 
                |    status: ............: CONFIRMED
                |    predecessors .......: ∅  
                |    successors .........: { NEXT_PERIOD }   
                |    
                |Expectation: 🚫 RETRY CANDIDATE         
                """.trimMargin(),
            data = Data(
                payment = base.copy(nextPeriodSuccessorId = UUID_2.toSepaPaymentId()),
                histories = SepaPaymentHistory.Initial(
                    base.sepaPaymentId,
                    listOf(
                        SepaPaymentHistory.Successor.Leaf(
                            id = UUID_2.toSepaPaymentId(),
                            kind = SuccessorKind.NEXT_PERIOD
                        )
                    )
                ).asHistories(),
            ),
            expected = false
        ),

        TestCase(
            name = "Case 3",
            description = """
                |Initial payment 
                |    status: ............: FAILED
                |    predecessors .......: ∅  
                |    successors .........: { NEXT_PERIOD }   
                |    
                |Expectation: 🚫 RETRY CANDIDATE         
                """.trimMargin(),
            data = Data(
                payment = base.copy(
                    status = PaymentExecutionStatus.FAILED,
                    nextPeriodSuccessorId = UUID_2.toSepaPaymentId(),
                ),
                histories = SepaPaymentHistory.Initial(
                    base.sepaPaymentId,
                    listOf(
                        SepaPaymentHistory.Successor.Leaf(
                            id = UUID_2.toSepaPaymentId(),
                            kind = SuccessorKind.NEXT_PERIOD
                        )
                    )
                ).asHistories(),
            ),
            expected = false
        ),
        TestCase(
            name = "Case 4",
            description = """
                |Initial payment 
                |    status: ............: FAILED
                |    predecessors .......: ∅  
                |    successors .........: { NEXT_PERIOD }   
                |    
                |Expectation: 🚫 RETRY CANDIDATE         
                """.trimMargin(),
            data = Data(
                payment = base.copy(
                    status = PaymentExecutionStatus.FAILED,
                    nextPeriodSuccessorId = UUID_2.toSepaPaymentId(),
                ),
                histories = SepaPaymentHistory.Initial(
                    base.sepaPaymentId,
                    listOf(
                        SepaPaymentHistory.Successor.Leaf(
                            id = UUID_2.toSepaPaymentId(),
                            kind = SuccessorKind.NEXT_PERIOD
                        )
                    )
                ).asHistories(),
            ),
            expected = false
        ),
        TestCase(
            name = "Case 5",
            description =
                """
                |Initial payment 
                |    status: ............: FAILED
                |    predecessors .......: ∅  
                |    successors .........: ∅  
                |
                |Expectation: ✅ RETRY CANDIDATE         
                """.trimMargin(),
            data = Data(
                payment = base.copy(status = PaymentExecutionStatus.FAILED),
                histories = SepaPaymentHistory.Initial(
                    base.sepaPaymentId,
                    emptyList()
                ).asHistories(),
            ),
            expected = true
        ),

        TestCase(
            name = "Case 6",
            description =
                """
                |Initial payment 
                |    status: ............: FAILED
                |    predecessors .......: ∅  
                |    successors .........: { RETRY }  
                |
                |Expectation: ✅ RETRY CANDIDATE         
                """.trimMargin(),
            data = Data(
                payment = base.copy(
                    status = PaymentExecutionStatus.FAILED,
                    retrySuccessorId = UUID_1.toSepaPaymentId()
                ),
                histories = SepaPaymentHistory.Initial(
                    base.sepaPaymentId,
                    listOf(
                        SepaPaymentHistory.Successor.Leaf(
                            UUID_1.toSepaPaymentId(),
                            kind= SuccessorKind.RETRY
                        )
                    )
                ).asHistories(),
            ),
            expected = true
        ),

        /*
            Non Initial Payments
         */
        TestCase(
            name = "Case 7",
            description = """
                |Non-initial payment 
                |    status: ............: CONFIRMED
                |    predecessors .......: { CONFIRMED }  
                |    successors .........: ∅     
                |         
                |Expectation: ✅ RETRY CANDIDATE         
                """.trimMargin(),
            data = Data(
                payment = base,
                histories = SepaPaymentHistory.Initial(
                    UUID_2.toSepaPaymentId(),
                    links = listOf(
                        SepaPaymentHistory.Successor.Leaf(
                            base.sepaPaymentId,
                            kind = SuccessorKind.NEXT_PERIOD
                        )
                    )
                ).asHistories(),
            ),
            expected = true
        ),
        TestCase(
            name = "Case 8",
            description = """
                |Non-initial payment 
                |    status: ............: CONFIRMED
                |    predecessors .......: { CONFIRMED }  
                |    successors .........: { CONFIRMED }     
                |         
                |Expectation: 🚫 RETRY CANDIDATE         
                """.trimMargin(),
            data = Data(
                payment = base.copy(
                    nextPeriodSuccessorId = UUID_3.toSepaPaymentId()
                ),
                histories = SepaPaymentHistory.Initial(
                    id = NIL_UUID.toSepaPaymentId(),
                    links = listOf(
                        SepaPaymentHistory.Successor.Node(
                            id = base.sepaPaymentId,
                            kind = SuccessorKind.NEXT_PERIOD,
                            links = listOf(
                                SepaPaymentHistory.Successor.Leaf(
                                    id = UUID_3.toSepaPaymentId(),
                                    kind = SuccessorKind.NEXT_PERIOD
                                )
                            )
                        )
                    )
                ).asHistories(),
            ),
            expected = false
        ),
        TestCase(
            name = "Case 9",
            description = """
                |Non-initial payment 
                |    status: ............: FAILED
                |    predecessors .......: { CONFIRMED }  
                |    successors .........: { FAILED }     
                |         
                |Expectation: ✅ RETRY CANDIDATE         
                """.trimMargin(),
            data = Data(
                payment = base.copy(
                    status = PaymentExecutionStatus.FAILED,
                    retrySuccessorId = UUID_2.toSepaPaymentId()
                ),
                histories = SepaPaymentHistory.Initial(
                    id = NIL_UUID.toSepaPaymentId(),
                    links = listOf(
                        SepaPaymentHistory.Successor.Node(
                            base.sepaPaymentId,
                            kind = SuccessorKind.NEXT_PERIOD,
                            links = listOf(
                                SepaPaymentHistory.Successor.Leaf(
                                    id = UUID_2.toSepaPaymentId(),
                                    kind = SuccessorKind.RETRY
                                )
                            )
                        )
                    )
                ).asHistories(),
            ),
            expected = true
        ),
        TestCase(
            name = "Case 10",
            description = """
                |Non-initial payment 
                |    status: ............: FAILED
                |    predecessors .......: { CONFIRMED }  
                |    successors .........: { CONFIRMED }     
                |         
                |Expectation: 🚫 RETRY CANDIDATE         
                """.trimMargin(),
            data = Data(
                payment = base.copy(
                    status = PaymentExecutionStatus.FAILED,
                    nextPeriodSuccessorId = UUID_2.toSepaPaymentId()
                ),
                histories = SepaPaymentHistory.Initial(
                    id = NIL_UUID.toSepaPaymentId(),
                    links = listOf(
                        SepaPaymentHistory.Successor.Node(
                            base.sepaPaymentId,
                            kind = SuccessorKind.NEXT_PERIOD,
                            links = listOf(
                                SepaPaymentHistory.Successor.Leaf(
                                    id = UUID_2.toSepaPaymentId(),
                                    kind = SuccessorKind.NEXT_PERIOD
                                )
                            )
                        )
                    )
                ).asHistories(),
            ),
            expected = false
        ),
        TestCase(
            name = "Case 11",
            description = """
                |Non-initial payment 
                |    status: ............: FAILED
                |    predecessors .......: { FAILED }  
                |    successors .........: ∅     
                |         
                |Expectation: 🚫 RETRY CANDIDATE         
                """.trimMargin(),
            data = Data(
                payment = base.copy(
                    status = PaymentExecutionStatus.FAILED,
                ),
                histories = SepaPaymentHistory.Initial(
                    id = NIL_UUID.toSepaPaymentId(),
                    links = listOf(
                        SepaPaymentHistory.Successor.Leaf(
                            id = base.sepaPaymentId,
                            kind = SuccessorKind.RETRY,
                        )
                    )
                ).asHistories(),
            ),
            expected = false
        ),
        TestCase(
            name = "Case 12",
            description = """
                |Non-initial payment 
                |    status: ............: FAILED
                |    predecessors .......: { FAILED }  
                |    successors .........: { FAILED }     
                |         
                |Expectation: 🚫 RETRY CANDIDATE         
                """.trimMargin(),
            data = Data(
                payment = base.copy(
                    status = PaymentExecutionStatus.FAILED,
                ),
                histories = SepaPaymentHistory.Initial(
                    id = NIL_UUID.toSepaPaymentId(),
                    links = listOf(
                        SepaPaymentHistory.Successor.Node(
                            base.sepaPaymentId,
                            kind = SuccessorKind.RETRY,

                            links = listOf(
                                SepaPaymentHistory.Successor.Leaf(
                                    id = UUID_2.toSepaPaymentId(),
                                    kind = SuccessorKind.RETRY
                                )
                            )
                        )
                    )
                ).asHistories(),
            ),
            expected = false
        ),
    )



    @Test
    fun testAll () {
        val results = cases.map {  testCase: TestCase<Data, Boolean> ->

            val (name, _, data, expected) = testCase
            val (payment, histories) = data
            println("""
                
                TestCase : $name
            """.trimIndent())

            testCase to payment.isCandidateForNextPeriodPayment(histories, forbiddenSeqTypes)

        }.map {
            val (case, result) = it
            (result == case.expected) to case.name
        }

        val errors = results.filterNot{
            val (passed, _) = it
            passed
        }.toList()

        val errorMessage = """
            Tests Failed: 
            ${errors.joinToString("\n") { (_, message) -> "name = $message" }}
            
            Assertion: 
        """.trimIndent()

        assertEquals(
            expected = true,
            actual = errors.isEmpty(),
            message = errorMessage
        )
    }
}
