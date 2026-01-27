package org.solyton.solawi.bid.module.bid.service.shares

import org.evoleq.exposedx.test.runSimpleH2Test
import org.jetbrains.exposed.sql.Transaction
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.solyton.solawi.bid.DbFunctional
import org.solyton.solawi.bid.module.banking.schema.FiscalYearsTable
import org.solyton.solawi.bid.module.bid.data.internal.ShareStatus
import org.solyton.solawi.bid.module.bid.data.internal.internalStatuses
import org.solyton.solawi.bid.module.bid.processes.AuctionProcesses
import org.solyton.solawi.bid.module.bid.repository.TestCaseSpecification
import org.solyton.solawi.bid.module.bid.repository.createTestShareStatuses
import org.solyton.solawi.bid.module.bid.schema.CoSubscribersTable
import org.solyton.solawi.bid.module.bid.schema.ShareOffersTable
import org.solyton.solawi.bid.module.bid.schema.ShareStatusTable
import org.solyton.solawi.bid.module.bid.schema.ShareSubscriptionEntity
import org.solyton.solawi.bid.module.bid.schema.ShareSubscriptionStatusHistory
import org.solyton.solawi.bid.module.bid.schema.ShareSubscriptionsTable
import org.solyton.solawi.bid.module.bid.schema.ShareTypesTable
import org.solyton.solawi.bid.module.permission.schema.ContextsTable
import org.solyton.solawi.bid.module.permission.schema.RightsTable
import org.solyton.solawi.bid.module.permission.schema.RoleRightContexts
import org.solyton.solawi.bid.module.permission.schema.RolesTable
import org.solyton.solawi.bid.module.permission.schema.UserRoleContext
import org.solyton.solawi.bid.module.system.repository.createSystemProcess
import org.solyton.solawi.bid.module.system.schema.SystemProcesses
import org.solyton.solawi.bid.module.user.schema.OrganizationsTable
import org.solyton.solawi.bid.module.user.schema.UserOrganization
import org.solyton.solawi.bid.module.user.schema.UserProfilesTable
import org.solyton.solawi.bid.module.user.schema.UsersTable
import java.util.*

sealed class TestCase(
    override val testId: String,
    override val description: String,
    open val setupTest: Transaction.(TestCase) -> TestCase,
    open val actAndAssert: Transaction.(TestCase) -> Unit,
) : TestCaseSpecification {
    data class Simple(
        override val testId: String,
        override val description: String,
        // Setup
        val currentStatus: ShareStatus,
        // Setup Test
        override val setupTest: Transaction.(TestCase) -> TestCase,
        // Created during setup
        val shareSubscription: ShareSubscriptionEntity? = null,
        // Act and assert
        override val actAndAssert: Transaction.(TestCase) -> Unit,
    ) : TestCase(
        testId,
        description,
        setupTest,
        actAndAssert
    )
    data class Complex(
        override val testId: String,
        override val description: String,
        // Setup
        val providerIds: Map<String, UUID> = emptyMap(),
        val userProfileIds: Map<String, UUID> = emptyMap(),
        val distributionPointIds: Map<String, Pair</* creator */ UUID,/* provider */ String>> = emptyMap(),
        val shareTypeIds: Map<String, Pair</* creator */UUID, /* provider */ String>> = emptyMap(),
        val shareOfferIds: Map<String, Pair</* creator */UUID,/* type */ String>> = emptyMap(),
        val shareSubscriptionIds: Map<String, Triple</* creator */ UUID, /* offer */ String, /*depot*/ String>> = emptyMap(),
        val coSubscribers: Map<String, List<String>> = emptyMap(),
        val shareStatuses: Map<String, ShareStatus> = emptyMap(),
        // Setup Test
        override val setupTest: Transaction.(TestCase) -> TestCase,
        // Act and Assert
        val sharesToImport: List<ShareToImport>,
        override val actAndAssert: Transaction.(TestCase) -> Unit,
    ): TestCase(
        testId,
        description,
        setupTest,
        actAndAssert
    )
}

data class SetupException(val e: Exception): Exception ("""Setup failed. See stacktrace for details.
    |${e.stackTraceToString()}
""".trimMargin())

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
    RoleRightContexts,
    UserRoleContext,
    OrganizationsTable,
    UserOrganization
)

class ShareSubscriptionImportServiceTest {

    @DbFunctional@ParameterizedTest
    @MethodSource("testCases")
    fun test(testCase: TestCase) = with(testCase) testCase@{
        runSimpleH2Test(testId, *tables) {
            val basicSetup : Transaction.(TestCase) -> TestCase = { basicSetup(it) }
            (actAndAssert o setupTest o basicSetup)  (this@testCase)
        }
    }

    companion object {

        @JvmStatic
        fun testCases(): List<TestCase> = testCases.mapIndexed { index, case ->
            when(case){
                is TestCase.Simple -> case.copy(testId = "$index-${case.testId}")
                is TestCase.Complex -> case.copy(testId = "$index-${case.testId}")
            }
        }

        val testCases = listOf(
            *simpleTestCases.toTypedArray(),
            *complexTestCases.toTypedArray()
        )
    }

    fun Transaction.basicSetup(testCase: TestCase): TestCase = with(testCase) {
        createSystemProcess(
            AuctionProcesses.SHARE_MANAGEMENT,
            ""
        )
        createTestShareStatuses()

        testCase
    }

}

val simpleTestCases: List<TestCase.Simple> = listOf(
    *internalStatuses.map { status ->
        TestCase.Simple(
            testId = "${UUID.randomUUID()}",
            description = """
                Import new share with status = $status with override
            """.trimIndent(),
            setupTest = { tC -> simpleSetup(tC) },
            currentStatus = status,
            actAndAssert = { testCase -> overrideSubscriptionAndAssert(testCase) },
        )
    }.toTypedArray(),
    *internalStatuses.map{status ->
        TestCase.Simple(
            testId = "${UUID.randomUUID()}",
            description = """
                Import new share with status = $status without override
            """.trimIndent(),
            setupTest = { tC -> simpleSetup(tC) },
            currentStatus = status,
            actAndAssert = { testCase -> importSubscriptionAndAssert(testCase) },
        )
    }.toTypedArray()
)

val complexTestCases = listOf(
    *listOf(true, false).map { override ->
        TestCase.Complex(
            testId = "${UUID.randomUUID()}",
            description = """
            |Import shares with different definition
       """.trimMargin(),
            setupTest = { tC -> complexSetup(tC) },
            providerIds = mapOf(
                "provider_1" to UUID_1
            ),
            userProfileIds = mapOf(
                "user_1" to UUID_1,
                "user_2" to UUID_2,
                "user_3" to UUID_3,
                "user_4" to UUID_4,
                "user_5" to UUID_5,
                "user_6" to UUID_6,
                "user_7" to UUID_7,
                "user_8" to UUID_8,
                "user_9" to UUID_9,
                "user_10" to UUID_10,

                // co subscribers
                "co_subscriber_1" to UUID_51,
                "co_subscriber_2" to UUID_52,
                "co_subscriber_3" to UUID_53,
                "co_subscriber_4" to UUID_54,
                "co_subscriber_5" to UUID_55,

                ),
            distributionPointIds = mapOf(
                "depot_1" to Pair(UUID_1, "provider_1"),
                "depot_2" to Pair(UUID_2, "provider_1")
            ),
            shareTypeIds = mapOf(
                "type_1" to Pair(UUID_1, "provider_1"),
                "type_2" to Pair(UUID_2, "provider_1")
            ),
            shareOfferIds = mapOf(
                "offer_1" to Pair(UUID_1, "type_1"),
                "offer_2" to Pair(UUID_2, "type_2")
            ),
            shareSubscriptionIds = mapOf(
                "user_1" to Triple(UUID_1, "offer_1", "depot_1"),
                "user_2" to Triple(UUID_2, "offer_2", "depot_2"),
                "user_3" to Triple(UUID_2, "offer_2", "depot_2")
            ),
            shareStatuses = mapOf(
                "user_1" to ShareStatus.PendingActivation,
                "user_2" to ShareStatus.PendingActivation,
                "user_3" to ShareStatus.PendingActivation
            ),
            coSubscribers = mapOf(
                "user_1" to listOf("co_subscriber_1", "co_subscriber_3"),
                "user_2" to listOf("co_subscriber_4",)
            ),
            sharesToImport = listOf(
                ShareToImport(
                    UUID_1,
                    UUID_1,
                    UUID_1,
                    2,
                    20.0,
                    true,
                    ShareStatus.Subscribed,
                    listOf("co_subscriber_1", "co_subscriber_2")
                ),

            ),
            actAndAssert = { testCase -> actAndAssertInComplexCase(override, testCase) }
        )
    }.toTypedArray()
)
