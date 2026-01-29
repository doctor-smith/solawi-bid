package org.solyton.solawi.bid.module.bid.service.shares

import org.evoleq.uuid.UUID_ZERO
import org.jetbrains.exposed.dao.flushCache
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.deleteAll
import org.joda.time.DateTime
import org.junit.jupiter.api.assertDoesNotThrow
import org.solyton.solawi.bid.module.banking.repository.UUID_ONE
import org.solyton.solawi.bid.module.banking.repository.createFiscalYear
import org.solyton.solawi.bid.module.bid.repository.statusEntity
import org.solyton.solawi.bid.module.bid.schema.PricingType
import org.solyton.solawi.bid.module.bid.schema.ShareOfferEntity
import org.solyton.solawi.bid.module.bid.schema.ShareSubscriptionEntity
import org.solyton.solawi.bid.module.bid.schema.ShareSubscriptionsTable
import org.solyton.solawi.bid.module.bid.schema.ShareTypeEntity
import org.solyton.solawi.bid.module.user.schema.UserEntity
import org.solyton.solawi.bid.module.user.schema.UserProfileEntity
import org.solyton.solawi.bid.module.user.schema.UserProfilesTable
import org.solyton.solawi.bid.module.user.schema.UserStatus
import kotlin.test.assertEquals


/**
 * Configures and sets up the initial data required for a given test case by creating
 * user, user profile, fiscal year, share type, share offer, and share subscription entities.
 *
 * @param testCase The test case object containing the current setup and test data
 *                 with details such as provider IDs, user profiles, current status,
 *                 and shares to be imported.
 * @return A copy of the test case object with the newly created share subscription entity.
 */
fun Transaction.simpleSetup(testCase: TestCase): TestCase {
    require(testCase is TestCase.Simple)
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

    val shareSubscription = ShareSubscriptionEntity.new{
        this.createdBy = UUID_ZERO
        this.shareOffer = shareOffer
        this.userProfile = userProfile
        this.fiscalYear = fiscalYear
        this.status = statusEntity(testCase.currentStatus)
    }

    flushCache()
    return testCase.copy(shareSubscription = shareSubscription)
}

/**
 * Overrides the share subscription for the given test case by importing a new subscription setup and
 * validates its properties through a series of assertions. This ensures that the subscription details
 * match the expected setup in the test case.
 *
 * @param testCase The test case containing all the required data for importing and asserting the share subscription.
 *                 This includes the provider ID, fiscal year, share offer, distribution point, user profile,
 *                 current subscription status, and other related details.
 */
fun Transaction.overrideSubscriptionAndAssert(testCase: TestCase) {
    require(testCase is TestCase.Simple)
    requireNotNull(testCase.shareSubscription)

    // collect data
    val providerId = testCase.shareSubscription.shareOffer.shareType.providerId
    val fiscalYearId = testCase.shareSubscription.fiscalYear.id.value
    val shareOfferId = testCase.shareSubscription.shareOffer.id.value
    val userProfile = UserProfileEntity.find { UserProfilesTable.createdBy eq UUID_ZERO }.first()
    val distributionPointId = testCase.shareSubscription.distributionPoint?.id?.value
    val status = testCase.currentStatus

    val numberOfShares = 1
    val pricePerShare = 10.0
    val ahcAuthorized = true

    val imported = assertDoesNotThrow {
        importShareSubscriptions(
            true,
            providerId,
            fiscalYearId,
            listOf(
                ShareToImport(
                    shareOfferId,
                    userProfile.id.value,
                    distributionPointId,
                    numberOfShares,
                    pricePerShare,
                    ahcAuthorized,
                    status,
                    emptyList()
                )
            ),
            UUID_ONE
        )
    }
    assertEquals(1, imported.size, "Wrong number of imports")
    val testObject = imported.first()

    assertEquals(shareOfferId, testObject.shareOffer.id.value)
    assertEquals(providerId, testObject.shareOffer.shareType.providerId)
    assertEquals(fiscalYearId, testObject.fiscalYear.id.value)
    assertEquals(userProfile.id.value, testObject.userProfile.id.value)
    assertEquals(distributionPointId, testObject.distributionPoint?.id?.value)
    assertEquals(numberOfShares, testObject.numberOfShares, "Wrong number of shares")
    assertEquals(pricePerShare, testObject.pricePerShare, "Wrong price per chare")
    assertEquals(ahcAuthorized, testObject.ahcAuthorized,"Wrong value of ahcAuthorized")

}

/**
 * Imports a share subscription based on the provided test case and validates the result by asserting the properties
 * of the imported subscription.
 *
 * @param testCase The test case containing data required for importing a single share subscription. This includes
 *                 information about the share offer, provider, fiscal year, distribution point, user profile, and
 *                 the expected share subscription status.
 */
fun Transaction.importSubscriptionAndAssert(testCase: TestCase) {
    require(testCase is TestCase.Simple)
    requireNotNull(testCase.shareSubscription)

    // collect data
    val providerId = testCase.shareSubscription.shareOffer.shareType.providerId
    val fiscalYearId = testCase.shareSubscription.fiscalYear.id.value
    val shareOfferId = testCase.shareSubscription.shareOffer.id.value
    val userProfile = UserProfileEntity.find { UserProfilesTable.createdBy eq UUID_ZERO }.first()
    val distributionPointId = testCase.shareSubscription.distributionPoint?.id?.value
    val status = testCase.currentStatus

    // delete all shares
    ShareSubscriptionsTable.deleteAll()


    val numberOfShares = 1
    val pricePerShare = 10.0
    val ahcAuthorized = true

    val imported = assertDoesNotThrow {
        importShareSubscriptions(
            false,
            providerId,
            fiscalYearId,
            listOf(
                ShareToImport(
                    shareOfferId,
                    userProfile.id.value,
                    distributionPointId,
                    numberOfShares,
                    pricePerShare,
                    ahcAuthorized,
                    status,
                    emptyList()
                )
            ),
            UUID_ONE
        )
    }
    assertEquals(1, imported.size, "Wrong number of imports")
    val testObject = imported.first()

    assertEquals(shareOfferId, testObject.shareOffer.id.value)
    assertEquals(providerId, testObject.shareOffer.shareType.providerId)
    assertEquals(fiscalYearId, testObject.fiscalYear.id.value)
    assertEquals(userProfile.id.value, testObject.userProfile.id.value)
    assertEquals(distributionPointId, testObject.distributionPoint?.id?.value)
    assertEquals(numberOfShares, testObject.numberOfShares, "Wrong number of shares")
    assertEquals(pricePerShare, testObject.pricePerShare, "Wrong price per chare")
    assertEquals(ahcAuthorized, testObject.ahcAuthorized,"Wrong value of ahcAuthorized")

}
