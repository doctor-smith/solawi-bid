package org.solyton.solawi.bid.module.bid.repository

import org.evoleq.exposedx.test.runSimpleH2Test
import org.evoleq.uuid.UUID_ZERO
import org.jetbrains.exposed.dao.flushCache
import org.jetbrains.exposed.sql.Transaction
import org.joda.time.DateTime
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.solyton.solawi.bid.DbFunctional
import org.solyton.solawi.bid.module.banking.repository.UUID_ONE
import org.solyton.solawi.bid.module.banking.repository.createFiscalYear
import org.solyton.solawi.bid.module.bid.data.internal.ChangeReason
import org.solyton.solawi.bid.module.bid.data.internal.ChangedBy
import org.solyton.solawi.bid.module.bid.data.internal.ShareStatus
import org.solyton.solawi.bid.module.bid.exception.ShareStatusException
import org.solyton.solawi.bid.module.bid.processes.AuctionProcesses
import org.solyton.solawi.bid.module.bid.schema.*
import org.solyton.solawi.bid.module.bid.schema.ShareOfferEntity
import org.solyton.solawi.bid.module.bid.schema.ShareSubscriptionEntity
import org.solyton.solawi.bid.module.bid.schema.ShareTypeEntity
import org.solyton.solawi.bid.module.system.repository.createSystemProcess
import org.solyton.solawi.bid.module.user.schema.UserEntity
import org.solyton.solawi.bid.module.user.schema.UserProfileEntity
import org.solyton.solawi.bid.module.user.schema.UserStatus
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


class ShareStatusRolloverTests {
    data class TestCase(
        override val testId: String,
        override val description: String,
        val status: ShareStatus,
        val modifier: ChangedBy,
        val humanModifierId: UUID? = null,
        val comment: String? = null,
        val shareSubscription: ShareSubscription? = null,
        val nextShareOffer: ShareOffer? = null,
        val actAndAssert: Transaction.(TestCase) -> Unit
    ) : TestCaseSpecification

    @DbFunctional@ParameterizedTest
    @MethodSource("testCases")
    fun test(testCase: TestCase) = with(testCase) {
        runSimpleH2Test(testCase.testId, *tables) {
            createSystemProcess(AuctionProcesses.SHARE_MANAGEMENT, "")
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
                now.plusYears(1).minusDays(1),
                UUID_ZERO,
            )
            val nextFiscalYear = createFiscalYear(
                UUID_ONE,
                now.plusYears(1),
                now.plusYears(2).minusDays(1),
                UUID_ZERO,
            )
            createTestShareStatuses()
            // create a shareSubscription
            // share type
            val shareType = ShareTypeEntity.new {
                this.createdBy = UUID_ZERO
                providerId = UUID_ONE
                name = "TestShareType"
                key = "TestKey"
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
            val nextShareOffer = ShareOfferEntity.new {
                this.shareType = shareType
                this.fiscalYear = nextFiscalYear
                this.price = null
                this.pricingType = PricingType.FLEXIBLE
                this.createdBy = UUID_ZERO
            }
            val statusEntity = statusEntity(status)
            val shareSubscription = ShareSubscriptionEntity.new {
                this.createdBy = UUID_ZERO
                this.shareOffer = shareOffer
                this.userProfile = userProfile
                this.fiscalYear = fiscalYear
                this.status = statusEntity
            }

            flushCache()

            actAndAssert(testCase.copy(
                shareSubscription = shareSubscription,
                nextShareOffer = nextShareOffer
            ))
        }
    }

    companion object {

        fun Transaction.rolloverAndAssert(testCase: TestCase): ShareSubscriptionEntity {
            val rolledOver = rollover(
                testCase.shareSubscription!!.id.value,
                testCase.nextShareOffer!!.id.value,
                testCase.modifier,
                testCase.humanModifierId,
            )

            assertEquals(ShareStatus.RollingOver, ShareStatus.from(rolledOver.status.name))
            assertEquals(testCase.nextShareOffer, rolledOver.shareOffer)

            val entry1 = ShareSubscriptionStatusHistoryEntry.find {
                ShareSubscriptionStatusHistory.shareSubscriptionId eq testCase.shareSubscription.id.value
            }.firstOrNull()
            assertNotNull(entry1)
            assertNotNull(entry1.fromStatus)
            assertEquals(testCase.status, ShareStatus.from(entry1.fromStatus!!.name) )
            assertEquals(ShareStatus.RollingOver, ShareStatus.from(entry1.toStatus.name))

            val entry2 = ShareSubscriptionStatusHistoryEntry.find {
                ShareSubscriptionStatusHistory.rollingOverFromSubscriptionId eq testCase.shareSubscription.id.value
            }.firstOrNull()
            assertNotNull(entry2)
            assertEquals(rolledOver, entry2.shareSubscription)

            // todo:test improve assertions

            return rolledOver
        }

        fun Transaction.requestActivationAndAssert(
            testCase: TestCase,
            rollingOverShareSubscription: ShareSubscription,
            approve: Boolean
        ) {
            // val pendingSubscription =
            next(
                rollingOverShareSubscription.id.value,
                ShareStatus.PendingActivation,
                ChangeReason.NEW_PERIOD,
                ChangedBy.USER,
                UUID_ONE,
                "Want the share in the next period"
            )

            // todo:test assertions

            if(approve) {
                // val subscribedSubscription =
                next(
                    rollingOverShareSubscription.id.value,
                    ShareStatus.Subscribed,
                    ChangeReason.NEW_PERIOD,
                    ChangedBy.PROVIDER,
                    UUID_ONE,
                    "Want the share in the next period"
                )

                // todo:test assertions

                val companionSubscription = testCase.shareSubscription!!

                assertEquals(ShareStatus.RolledOver, ShareStatus.from(companionSubscription.status.name))
            }
        }
        @JvmStatic
        fun testCases(): List<TestCase> = testCases.mapIndexed {
            index, testCase -> testCase.copy(
                testId = "$index-${testCase.testId}"
            )
        }

        val testCases = listOf<TestCase>(
            TestCase(
                testId = "${UUID.randomUUID()}",
                description = """
                    Role over to new period; Change triggered by PROVIDER
                """.trimIndent(),
                status = ShareStatus.Subscribed,
                modifier = ChangedBy.PROVIDER,
                humanModifierId = UUID_ONE,
                comment = "Rolling over to new period"
            ) {
                testCase -> rolloverAndAssert(testCase)
            },
            TestCase(
                testId = "${UUID.randomUUID()}",
                description = """
                    Role over to new period; Change triggered by SYSTEM
                """.trimIndent(),
                status = ShareStatus.Subscribed,
                modifier = ChangedBy.SYSTEM,
                humanModifierId = null,
                comment = "Rolling over to new period"
            ) {
                    testCase -> rolloverAndAssert(testCase)
            },
            TestCase(
                testId = "${UUID.randomUUID()}",
                description = """
                    Role over to new period; Change triggered by USER fails
                """.trimIndent(),
                status = ShareStatus.Subscribed,
                modifier = ChangedBy.USER,
                humanModifierId = null,
                comment = "Rolling over to new period"
            ) {
                    testCase -> assertThrows<ShareStatusException.TransitionNotAllowedForModifier> {
                        rolloverAndAssert(testCase)
                    }
            },
            TestCase(
                testId = "${UUID.randomUUID()}",
                description = """
                    1. Role over to new period; triggered by PROVIDER;
                    2. Request activation: RollingOver -> PendingActivation; triggered by USER
                    3. Approve pending activation: PendingActivation -> Subscribed; triggered by PROVIDER
                """.trimIndent(),
                status = ShareStatus.Subscribed,
                modifier = ChangedBy.PROVIDER,
                humanModifierId = UUID_ONE,
                comment = "Rolling over to new period"
            ) {
                    testCase -> {
                        val rollingOver = rolloverAndAssert(testCase)
                        requestActivationAndAssert(
                            testCase,
                            rollingOver,
                            true
                        )
                    }
            },
        )
    }
}
