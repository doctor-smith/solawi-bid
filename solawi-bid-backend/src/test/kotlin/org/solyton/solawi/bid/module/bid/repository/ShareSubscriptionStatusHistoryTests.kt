package org.solyton.solawi.bid.module.bid.repository

import org.evoleq.exposedx.test.runSimpleH2Test
import org.evoleq.uuid.UUID_ZERO
import org.jetbrains.exposed.dao.flushCache
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.Transaction
import org.joda.time.DateTime
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.solyton.solawi.bid.DbFunctional
import org.solyton.solawi.bid.module.banking.repository.UUID_ONE
import org.solyton.solawi.bid.module.banking.repository.createFiscalYear
import org.solyton.solawi.bid.module.banking.schema.FiscalYearsTable
import org.solyton.solawi.bid.module.bid.data.internal.ShareStatus
import org.solyton.solawi.bid.module.bid.data.internal.ChangeReason
import org.solyton.solawi.bid.module.bid.data.internal.ChangedBy
import org.solyton.solawi.bid.module.bid.processes.AuctionProcesses
import org.solyton.solawi.bid.module.bid.schema.CoSubscribersTable
import org.solyton.solawi.bid.module.bid.schema.PricingType
import org.solyton.solawi.bid.module.bid.schema.ShareOfferEntity
import org.solyton.solawi.bid.module.bid.schema.ShareOffersTable
import org.solyton.solawi.bid.module.bid.schema.ShareStatusTable
import org.solyton.solawi.bid.module.bid.schema.ShareSubscriptionEntity
import org.solyton.solawi.bid.module.bid.schema.ShareSubscriptionStatusHistory
import org.solyton.solawi.bid.module.bid.schema.ShareSubscriptionStatusHistoryEntry
import org.solyton.solawi.bid.module.bid.schema.ShareSubscriptionsTable
import org.solyton.solawi.bid.module.bid.schema.ShareTypeEntity
import org.solyton.solawi.bid.module.bid.schema.ShareTypesTable
import org.solyton.solawi.bid.module.permission.schema.ContextsTable
import org.solyton.solawi.bid.module.permission.schema.RightsTable
import org.solyton.solawi.bid.module.permission.schema.RolesTable
import org.solyton.solawi.bid.module.system.repository.createSystemProcess
import org.solyton.solawi.bid.module.system.schema.SystemProcesses
import org.solyton.solawi.bid.module.user.schema.UserEntity
import org.solyton.solawi.bid.module.user.schema.UserProfileEntity
import org.solyton.solawi.bid.module.user.schema.UserProfilesTable
import org.solyton.solawi.bid.module.user.schema.UserStatus
import org.solyton.solawi.bid.module.user.schema.UsersTable
import java.util.UUID
import kotlin.test.assertEquals


val tables = arrayOf(
    ShareSubscriptionStatusHistory,
    ShareSubscriptionsTable,
    ShareOffersTable,
    ShareStatusTable,
    ShareTypesTable,
    FiscalYearsTable,
    UserProfilesTable,
    UsersTable,
    SystemProcesses,
    CoSubscribersTable,
    RolesTable,
    RightsTable,
    ContextsTable,
)

class ShareSubscriptionStatusHistoryTests {

    data class TestCase(
        override val testId: String = "",
        override val description: String = "",
        val fromStatus: ShareStatus? = null,
        val toStatus: ShareStatus,
        val reason: ChangeReason? = null,
        val changedBy: ChangedBy? = null,
        val humanModifierId: UUID? = null,
        val comment: String? = null,
        val shareSubscription: ShareSubscriptionEntity? = null,
        val isRollingOverFromSubscription: Boolean = false,
        val rollingOverFromSubscription: ShareSubscriptionEntity? = null,
        val actAndAssert: Transaction.(TestCase) -> Unit
    ): TestCaseSpecification
    @DbFunctional@Test fun x() {
        val x = shareStatuses.size
        val y = modifiers.size
        assertEquals(x * y, (shareStatuses cross  modifiers).size)
    }


    @DbFunctional@ParameterizedTest
    @MethodSource("testCases")
    fun test(testCase: TestCase) = with(testCase) testCase@{
        runSimpleH2Test(testId, *tables) {
            createSystemProcess(
                AuctionProcesses.SHARE_MANAGEMENT,
                ""
            )

            val user = UserEntity.new {
                this.createdBy = UUID_ZERO
                username = "username"
                password = "password"
                status = UserStatus.ACTIVE
            }
            val userProfile = UserProfileEntity.new {
                this.createdBy = UUID_ZERO
                this.user = user
                firstName = "firstname"
                lastName = "lastname"
                phoneNumber = "12345"
                title = "title"

            }
            val now = DateTime.now()
            val fiscalYear = createFiscalYear(
                UUID_ONE,
                now,
                now.plusYears(1),
                UUID_ZERO,
            )
            createTestShareStatuses()
            // create a shareSubscription
            // share type
            val shareType = ShareTypeEntity.new {
                this.createdBy = UUID_ZERO
                providerId = UUID_ONE
                name = "TestShareType"
                description = "TestShareType"
            }
            // share offer
            val shareOffer = ShareOfferEntity.new {
                this.shareType = shareType
                this.fiscalYear = fiscalYear
                this.price = null
                this.pricingType = PricingType.FLEXIBLE
                this.createdBy = UUID_ZERO
            }

            val shareSubscription = ShareSubscriptionEntity.new{
                this.createdBy = UUID_ZERO
                this.shareOffer = shareOffer
                this.userProfile = userProfile
                this.fiscalYear = fiscalYear
                this.status = statusEntity(toStatus)
            }

            val rollingOverFromShareSubscription = if(testCase.isRollingOverFromSubscription) {
                 ShareSubscriptionEntity.new{
                    this.createdBy = UUID_ZERO
                    this.shareOffer = shareOffer
                    this.userProfile = userProfile
                    this.fiscalYear = fiscalYear
                    this.status = statusEntity(toStatus)
                }

            } else {
                null
            }

            actAndAssert(this@testCase.copy(
                shareSubscription = shareSubscription,
                rollingOverFromSubscription = rollingOverFromShareSubscription
            ))
        }
    }

    companion object {
        /**
         * We want to test the creation of history entries
         */
        fun Transaction.createHistoryEntry(testCase: TestCase): ShareSubscriptionStatusHistoryEntry {
            val fromStatusEntity = if(testCase.fromStatus != null){
                statusEntity(testCase.fromStatus)
            } else {
                null
            }
            val entry = ShareSubscriptionStatusHistoryEntry.new {
                this.shareSubscription = testCase.shareSubscription!!
                fromStatus = fromStatusEntity
                toStatus = testCase.shareSubscription.status
                this.reason = testCase.reason!!
                this.changedBy = testCase.changedBy!!
                this.comment = testCase.comment
                this.humanModifierId = testCase.humanModifierId
                this.rollingOverFromShareSubscription = testCase.rollingOverFromSubscription
            }
            flushCache()
            return entry
        }

        @JvmStatic
        fun testCases(): List<TestCase> {
            return testCases
        }

        val testCases = listOf<TestCase>(
            // INITIAL CREATION
            TestCase(
                testId = "${UUID.randomUUID()}",
                description = """
                    Test initial creation by USER: no failure
                """.trimIndent(),
                fromStatus = null,
                toStatus = ShareStatus.PendingActivation,
                reason = ChangeReason.INITIAL_CREATION,
                changedBy = ChangedBy.USER,
                humanModifierId = UUID_ZERO,
                comment = "Want to subscribe a share",
            ) {
                testCase -> assertDoesNotThrow { createHistoryEntry(testCase) }
            },
            TestCase(
                testId = "${UUID.randomUUID()}",
                description = """
                    Test initial creation by provider: no failure
                """.trimIndent(),
                fromStatus = null,
                toStatus = ShareStatus.PendingActivation,
                reason = ChangeReason.INITIAL_CREATION,
                changedBy = ChangedBy.PROVIDER,
                humanModifierId = UUID_ONE,
                comment = "Wants to subscribe a share",
            ) {
                    testCase -> assertDoesNotThrow { createHistoryEntry(testCase) }
            },
            TestCase(
                description = "INITIAL_CREATION by system",
                fromStatus = null,
                toStatus = ShareStatus.PendingActivation,
                reason = ChangeReason.INITIAL_CREATION,
                changedBy = ChangedBy.SYSTEM,
                humanModifierId = null,
                comment = null,
            ) {
                assertDoesNotThrow { createHistoryEntry(it) }
            },
            *(shareStatuses cross modifiers).map { (status, modifier) ->
                TestCase(
                    testId = "${UUID.randomUUID()}",
                    description = """
                        initial creation by $modifier: failure due to non-null fromStatus
                    """.trimIndent(),
                    fromStatus = status,
                    toStatus = ShareStatus.PendingActivation,
                    reason = ChangeReason.INITIAL_CREATION,
                    changedBy = modifier,
                    humanModifierId = UUID_ZERO,
                    comment = "Want to subscribe a share",
                ) {
                        testCase -> assertThrows<ExposedSQLException> { createHistoryEntry(testCase) }
                }
            }.toTypedArray(),


            *changeReasons.map{
                TestCase(
                    description = "$it by system: failure du to non-null humanModifierId",
                    fromStatus = null,
                    toStatus = ShareStatus.PendingActivation,
                    reason = it,
                    changedBy = ChangedBy.SYSTEM,
                    humanModifierId = UUID_ONE,
                    comment = null,
                ) {
                    testCase -> assertThrows<ExposedSQLException> { createHistoryEntry(testCase) }
                }
            }.toTypedArray()
            ,


            TestCase(
                testId = "${UUID.randomUUID()}",
                description = """
                    Test initial creation by system: no failure due missing humanModifierId
                """.trimIndent(),
                fromStatus = null,
                toStatus = ShareStatus.PendingActivation,
                reason = ChangeReason.INITIAL_CREATION,
                changedBy = ChangedBy.SYSTEM,
                humanModifierId = null,
                comment = "Wants to subscribe a share",
                isRollingOverFromSubscription = false
            ) {
                    testCase -> assertDoesNotThrow { createHistoryEntry(testCase) }
            },

            // ROLLOVER
            TestCase(
                description = "ROLLOVER by system",
                fromStatus = null,
                toStatus = ShareStatus.PendingActivation,
                reason = ChangeReason.ROLLOVER,
                changedBy = ChangedBy.SYSTEM,
                isRollingOverFromSubscription = true,
                humanModifierId = null,
                comment = null,
            ) {
                assertDoesNotThrow { createHistoryEntry(it) }
            },
            TestCase(
                description = "ROLLOVER by provider",
                fromStatus = null,
                toStatus = ShareStatus.PendingActivation,
                reason = ChangeReason.ROLLOVER,
                changedBy = ChangedBy.PROVIDER,
                isRollingOverFromSubscription = true,
                humanModifierId = UUID_ONE,
                comment = "Rollover from previous subscription",
            ) {
                assertDoesNotThrow { createHistoryEntry(it) }
            },
            // todo:test is this correct?
            TestCase(
                description = "ROLLOVER by user",
                fromStatus = null,
                toStatus = ShareStatus.PendingActivation,
                reason = ChangeReason.ROLLOVER,
                changedBy = ChangedBy.USER,
                isRollingOverFromSubscription = true,
                humanModifierId = UUID_ONE,
                comment = "Rollover from previous subscription",
            ) {
                assertDoesNotThrow { createHistoryEntry(it) }
            },
            // Failures
            TestCase(
                testId = "${UUID.randomUUID()}",
                description = """
                    ROLLOVER by system: failure due to invalid rollover
                """.trimIndent(),
                fromStatus = null,
                toStatus = ShareStatus.PendingActivation,
                reason = ChangeReason.ROLLOVER,
                changedBy = ChangedBy.SYSTEM,
                humanModifierId = UUID_ONE,
                comment = "Wants to subscribe a share",
                isRollingOverFromSubscription = false
            ) {
                    testCase -> assertThrows<ExposedSQLException> {
                        createHistoryEntry(testCase)
                    }
            },
            TestCase(
                testId = "${UUID.randomUUID()}",
                description = """
                    ROLLOVER by user: failure due to invalid rollover
                """.trimIndent(),
                fromStatus = null,
                toStatus = ShareStatus.PendingActivation,
                reason = ChangeReason.ROLLOVER,
                changedBy = ChangedBy.USER,
                humanModifierId = UUID_ONE,
                comment = "Want to subscribe a share",
                isRollingOverFromSubscription = false
            ) {
                    testCase -> assertThrows<ExposedSQLException> { createHistoryEntry(testCase) }
            },

            *arrayOf(
                ChangeReason.INITIAL_CREATION,
                ChangeReason.ADMIN_ACTION,
                ChangeReason.SYSTEM_EVENT,
                ChangeReason.PAYMENT_EVENT,
                ChangeReason.AUTHORIZATION_EVENT,
                ChangeReason.USER_ACTION
            ).map {
                TestCase(
                    testId = "${UUID.randomUUID()}",
                    description = """
                    ROLLOVER by user: failure due to invalid rollover
                """.trimIndent(),
                    fromStatus = null,
                    toStatus = ShareStatus.PendingActivation,
                    reason = it,
                    changedBy = ChangedBy.USER,
                    humanModifierId = UUID_ONE,
                    comment = "Want to subscribe a share",
                    isRollingOverFromSubscription = true
                ) {
                        testCase -> assertThrows<ExposedSQLException> { createHistoryEntry(testCase) }
                }
            }.toTypedArray(),
            TestCase(
                testId = "${UUID.randomUUID()}",
                description = """
                    ROLLOVER by provider: failure due to invalid rollover
                """.trimIndent(),
                fromStatus = null,
                toStatus = ShareStatus.PendingActivation,
                reason = ChangeReason.ROLLOVER,
                changedBy = ChangedBy.PROVIDER,
                humanModifierId = UUID_ONE,
                comment = "Want to subscribe a share",
                isRollingOverFromSubscription = false
            ) {
                    testCase -> assertThrows<ExposedSQLException> { createHistoryEntry(testCase) }
            },


            TestCase(
                testId = "${UUID.randomUUID()}",
                description = """
                    ROLLOVER by user: failure due missing humanModifierId
                """.trimIndent(),
                fromStatus = null,
                toStatus = ShareStatus.PendingActivation,
                reason = ChangeReason.ROLLOVER,
                changedBy = ChangedBy.USER,
                humanModifierId = null,
                comment = "Wants to subscribe a share",
                isRollingOverFromSubscription = false
            ) {
                    testCase -> assertThrows<ExposedSQLException> { createHistoryEntry(testCase) }
            },
            TestCase(
                testId = "${UUID.randomUUID()}",
                description = """
                    ROLLOVER by provider: failure due missing humanModifierId
                """.trimIndent(),
                fromStatus = null,
                toStatus = ShareStatus.PendingActivation,
                reason = ChangeReason.ROLLOVER,
                changedBy = ChangedBy.PROVIDER,
                humanModifierId = null,
                comment = "Wants to subscribe a share",
                isRollingOverFromSubscription = false
            ) {
                    testCase -> assertThrows<ExposedSQLException> { createHistoryEntry(testCase) }
            },

        ).mapIndexed { index, testCase ->   testCase.copy(testId = "$index-${testCase.testId}") }
    }
}
