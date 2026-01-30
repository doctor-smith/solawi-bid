package org.solyton.solawi.bid.module.shares.repository

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
import org.solyton.solawi.bid.module.bid.schema.*
import org.solyton.solawi.bid.module.shares.exception.ShareStatusException
import org.solyton.solawi.bid.module.shares.schema.PricingType
import org.solyton.solawi.bid.module.shares.schema.ShareOfferEntity
import org.solyton.solawi.bid.module.shares.schema.ShareSubscription
import org.solyton.solawi.bid.module.shares.schema.ShareSubscriptionEntity
import org.solyton.solawi.bid.module.shares.schema.ShareSubscriptionStatusHistory
import org.solyton.solawi.bid.module.shares.schema.ShareSubscriptionStatusHistoryEntry
import org.solyton.solawi.bid.module.shares.schema.ShareTypeEntity
import org.solyton.solawi.bid.module.user.schema.UserEntity
import org.solyton.solawi.bid.module.user.schema.UserProfileEntity
import org.solyton.solawi.bid.module.user.schema.UserStatus
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


class ShareStatusTransitionTests {

    data class TestCase(
        override val testId: String,
        override val description: String,
        val fromStatus: ShareStatus,
        val toStatus: ShareStatus,
        val reason: ChangeReason,
        val modifier: ChangedBy,
        val humanModifierId: UUID? = null,
        val comment: String? = null,
        val shareSubscription: ShareSubscription? = null,
        val actAndAssert: Transaction.(TestCase) -> Unit
    ) : TestCaseSpecification

    /**
     * Want to test the status transition function [next], which transforms the status of share-subscription.
     * It depends on the properties
     * - shareSubscription: [ShareSubscription], with a current status
     * - nextState: [ShareStatus],
     * - reason: ChangeReason,
     * - changedBy: ChangedBy,
     * - modifier: UUID?,
     * - comment: String?
     *
     *  Needs
     *  - Create ShareSubscriptions with arbitrary status
     *  - Create Share
     */
    @DbFunctional@ParameterizedTest
    @MethodSource("testCases")
    fun test(testCase: TestCase) = with(testCase) {
        runSimpleH2Test(testCase.testId, *tables) {
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

            val statusEntity = statusEntity(fromStatus)
            val shareSubscription = ShareSubscriptionEntity.new{
                this.createdBy = UUID_ZERO
                this.shareOffer = shareOffer
                this.userProfile = userProfile
                this.fiscalYear = fiscalYear
                this.status = statusEntity
            }

            flushCache()
            /*
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

             */


            actAndAssert(testCase.copy(
                shareSubscription = shareSubscription
            ))
        }
    }

    companion object {

        fun Transaction.moveToNextStateAndAssert(testCase: TestCase) {
            val share = next(
                testCase.shareSubscription!!.id.value,
                testCase.toStatus,
                testCase.reason,
                testCase.modifier,
                testCase.humanModifierId,
                testCase.comment,
            )
            flushCache()

            assertEquals(testCase.toStatus.toString() , share.status.name)
            val historyEntry = ShareSubscriptionStatusHistoryEntry.find {
                ShareSubscriptionStatusHistory.shareSubscriptionId eq share.id
            }.firstOrNull()

            assertNotNull(historyEntry)
            assertEquals(testCase.fromStatus.toString(), historyEntry.fromStatus?.name, "Wrong from status!")
            assertEquals(testCase.toStatus.toString(), historyEntry.toStatus.name, "Wrong to status!")
            assertEquals(testCase.comment, historyEntry.comment, "Wrong comment!")
            assertEquals(testCase.reason, historyEntry.reason, "Wrong reason!")
            assertEquals(testCase.modifier, historyEntry.changedBy, "Wrong modifier!")
            assertEquals(testCase.humanModifierId, historyEntry.humanModifierId, "Wrong humanModifierId!")
            assertEquals(null, historyEntry.rollingOverFromShareSubscription, "Wrong value of rollingOverFromShareSubscription!")
        }

        @JvmStatic
        fun testCases(): List<TestCase> {
            return testCases.mapIndexed { index, testCase -> testCase.copy(
                testId = "$index-${testCase.testId}"
            ) }
        }

        val testCases = listOf(
            *((arrayOf(ChangeReason.INITIAL_CREATION, ChangeReason.ROLLOVER) cross (shareStatuses cross shareStatuses)).map{ (reason, statusPair) ->
                    TestCase(
                        testId = UUID.randomUUID().toString(),
                        description = """
                            ${statusPair.first} -> ${statusPair.second}: failure due to forbidden change reason
                        """.trimIndent(),
                        fromStatus = statusPair.first,
                        toStatus = statusPair.second,
                        reason = reason,
                        modifier = ChangedBy.USER,
                        humanModifierId = UUID_ONE,
                        comment = "Want to get share",
                        actAndAssert = {
                                testCase -> assertThrows<ShareStatusException.ForbiddenChangeReason> { moveToNextStateAndAssert(testCase) }
                        },
                    )
                }
            ).toTypedArray(),

            TestCase(
                testId = UUID.randomUUID().toString(),
                description = """
                    PendingActivation -> Subscribed: triggered by PROVIDER
                """.trimIndent(),
                fromStatus = ShareStatus.PendingActivation,
                toStatus = ShareStatus.Subscribed,
                reason = ChangeReason.SUBSCRIPTION_APPROVED,
                modifier = ChangedBy.PROVIDER,
                humanModifierId = UUID_ONE,
                comment = "Want to get share",
                actAndAssert = {
                        testCase ->  moveToNextStateAndAssert(testCase)
                },
            ),
            TestCase(
                testId = UUID.randomUUID().toString(),
                description = """
                    PendingActivation -> Subscribed: triggered by PROVIDER
                """.trimIndent(),
                fromStatus = ShareStatus.PendingActivation,
                toStatus = ShareStatus.Subscribed,
                reason = ChangeReason.NO_PAYMENT_MANDATE_REQUIRED,
                modifier = ChangedBy.PROVIDER,
                humanModifierId = UUID_ONE,
                comment = "Want to get share",
                actAndAssert = {
                        testCase ->  moveToNextStateAndAssert(testCase)
                },
            ),
            TestCase(
                testId = UUID.randomUUID().toString(),
                description = """
                    PendingActivation -> Subscribed: triggered by SYSTEM
                """.trimIndent(),
                fromStatus = ShareStatus.PendingActivation,
                toStatus = ShareStatus.Subscribed,
                reason = ChangeReason.PAYMENT_MANDATE_APPROVED,
                modifier = ChangedBy.SYSTEM,
                humanModifierId = null,
                comment = "Want to get share",
                actAndAssert = {
                        testCase ->  moveToNextStateAndAssert(testCase)
                },
            ),
            TestCase(
                testId = UUID.randomUUID().toString(),
                description = """
                    PendingActivation -> Subscribed: triggered by SYSTEM
                """.trimIndent(),
                fromStatus = ShareStatus.PendingActivation,
                toStatus = ShareStatus.Subscribed,
                reason = ChangeReason.NO_PAYMENT_MANDATE_REQUIRED,
                modifier = ChangedBy.SYSTEM,
                humanModifierId = null,
                comment = "Want to get share",
                actAndAssert = {
                        testCase ->  moveToNextStateAndAssert(testCase)
                },
            ),
            // Should fail for each change reason with the same exception
            *ChangeReason.entries.filter { it !in listOf(ChangeReason.ROLLOVER, ChangeReason.INITIAL_CREATION) }.map { reason -> TestCase(
                testId = UUID.randomUUID().toString(),
                description = """
                    PendingActivation -> Subscribed: triggered by USER fails
                """.trimIndent(),
                fromStatus = ShareStatus.PendingActivation,
                toStatus = ShareStatus.Subscribed,
                reason = reason,
                modifier = ChangedBy.USER,
                humanModifierId = UUID_ONE,
                comment = "Want to get share",
                actAndAssert = {
                        testCase ->  assertThrows<ShareStatusException.TransitionNotAllowedForModifier> { moveToNextStateAndAssert(testCase) }
                },
            )}.toTypedArray(),
        )
    }
}
