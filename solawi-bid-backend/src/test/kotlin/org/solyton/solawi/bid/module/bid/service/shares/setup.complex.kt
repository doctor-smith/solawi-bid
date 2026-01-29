package org.solyton.solawi.bid.module.bid.service.shares

import org.evoleq.uuid.UUID_ZERO
import org.jetbrains.exposed.dao.flushCache
import org.jetbrains.exposed.sql.Transaction
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.banking.repository.createFiscalYear
import org.solyton.solawi.bid.module.bid.repository.createShareType
import org.solyton.solawi.bid.module.bid.repository.statusEntity
import org.solyton.solawi.bid.module.bid.schema.CoSubscriberEntity
import org.solyton.solawi.bid.module.bid.schema.DistributionPointEntity
import org.solyton.solawi.bid.module.bid.schema.PricingType
import org.solyton.solawi.bid.module.bid.schema.ShareOfferEntity
import org.solyton.solawi.bid.module.bid.schema.ShareSubscriptionEntity
import org.solyton.solawi.bid.module.permission.repository.createRight
import org.solyton.solawi.bid.module.permission.repository.createRole
import org.solyton.solawi.bid.module.user.schema.UserEntity
import org.solyton.solawi.bid.module.user.schema.UserProfileEntity
import org.solyton.solawi.bid.module.user.schema.UserStatus
import org.solyton.solawi.bid.module.user.schema.repository.createRootOrganization
import java.util.UUID


// Complex Setup
////////////////////////////////
/**
 * Performs a complex setup routine within a database transaction for the given test case.
 * It creates and maps various entities including users, user profiles, providers, distribution points,
 * share types, share offers, and share subscriptions. Finally, it flushes the cache
 * and returns the updated test case.
 *
 * @param testCase The test case containing identifiers and configurations necessary
 *                 to generate and persist the related entities for the setup process.
 * @return The updated test case with its setup completed and all entities created and mapped.
 */
@Throws(SetupException::class)
@Suppress("MapGetWithNotNullAssertionOperator")
fun Transaction.complexSetup(testCase: TestCase): TestCase {
    require(testCase is TestCase.Complex)
    return with(testCase) {
        try {
            // rights and roles
            createRole("MANAGER", "", UUID_ZERO)
            createRight("MANAGE_USERS", "MANAGER", UUID_ZERO)
            createRight("CREATE_ORGANIZATION", "MANAGER", UUID_ZERO)
            createRight("READ_ORGANIZATION", "MANAGER", UUID_ZERO)
            createRight("UPDATE_ORGANIZATION", "MANAGER", UUID_ZERO)
            createRight("DELETE_ORGANIZATION", "MANAGER", UUID_ZERO)

            val userProfilesMap = userProfileIds.map { (name, id) ->
                val user = UserEntity.new {
                    this.createdBy = id
                    username = name
                    password = "password"
                    status = UserStatus.ACTIVE
                }
                name to UserProfileEntity.new {
                    this.createdBy = id
                    this.user = user
                    firstName = "firstname"
                    lastName = "lastname"
                    phoneNumber = "12345"
                    title = "title"
                }
            }.associateBy({ it.first }, { it.second })

            val now = DateTime.now()
            val fiscalYearsMap = providerIds.map { (name, creatorId) ->
                name to createFiscalYear(
                    creatorId,
                    now,
                    now.plusYears(1),
                    creatorId,
                )
            }.associateBy({ it.first }, { it.second })

            val providersMap = providerIds.map { (name, id) ->
                val organization = createRootOrganization(
                    name,
                    id
                )
                name to organization
            }.associateBy({ it.first }, { it.second })

            val distributionPointsMap = distributionPointIds.map { (dpName, pair) ->
                dpName to DistributionPointEntity.new {
                    this.createdBy = pair.first
                    this.organization = providersMap[pair.second]!!
                    name = dpName
                }
            }.associateBy({ it.first }, { it.second })

            val shareTypesMap = shareTypeIds.map { (name, pair) ->
                val (creatorId, provider) = pair
                name to createShareType(
                    providersMap[provider]!!.id.value as UUID,
                    name,
                    name,
                    "description",
                    creatorId
                )
            }.associateBy({ it.first }, { it.second })


            val shareOffersMap = shareOfferIds.map { (name, pair) ->
                val (creatorId, type) = pair
                name to ShareOfferEntity.new {
                    this.createdBy = creatorId
                    this.shareType = shareTypesMap[type]!!
                    this.fiscalYear = with(shareTypesMap[type]!!) {
                        val providerName = providersMap.entries.first { it.value.id.value == this.providerId }.key
                        fiscalYearsMap[providerName]!!
                    }
                    this.price = null
                    this.pricingType = PricingType.FLEXIBLE
                }
            }.associateBy({ it.first }, { it.second })


            val shareSubscriptionsMap = shareSubscriptionIds.map { (name, triple) ->
                val (creatorId, offer, depot) = triple
                name to ShareSubscriptionEntity.new {
                    createdBy = creatorId
                    shareOffer = shareOffersMap[offer]!!
                    this.fiscalYear = shareOffer.fiscalYear
                    status = statusEntity(shareStatuses[name]!!)
                    userProfile = userProfilesMap[name]!!
                    distributionPoint = distributionPointsMap[depot]
                    numberOfShares = DEFAULT_NUMBER_OF_SHARES
                    pricePerShare = DEFAULT_PRICE_PER_SHARE
                    ahcAuthorized = DEFAULT_AHC_AUTHORIZED
                }
            }.associateBy({ it.first }, { it.second })

            coSubscribers.forEach { (subscriber, coSubscribers) ->
                val subscription = shareSubscriptionsMap[subscriber]!!
                coSubscribers.map { coSubscriber ->
                    val user = userProfilesMap[coSubscriber]!!.user
                    CoSubscriberEntity.new {
                        createdBy = UUID_ZERO
                        this.user = user
                        this.shareSubscription = subscription
                    }
                }
            }

            flushCache()
            this
        } catch (e: Exception) {
            throw SetupException(e)
        }
    }
}

const val DEFAULT_PRICE_PER_SHARE = 10.0
const val DEFAULT_NUMBER_OF_SHARES = 1
const val DEFAULT_AHC_AUTHORIZED = false
