package org.solyton.solawi.bid.module.shares.repository

import org.evoleq.exposedx.NO_MESSAGE_PROVIDED
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.solyton.solawi.bid.module.auditable.markModifiedBy
import org.solyton.solawi.bid.module.banking.repository.validatedFiscalYear
import org.solyton.solawi.bid.module.banking.schema.FiscalYearEntity
import org.solyton.solawi.bid.module.distribution.repository.validatedDistributionPoint
import org.solyton.solawi.bid.module.shares.exception.ShareException
import org.solyton.solawi.bid.module.shares.schema.*
import org.solyton.solawi.bid.module.shares.schema.ShareOfferEntity
import org.solyton.solawi.bid.module.shares.schema.ShareSubscriptionEntity
import org.solyton.solawi.bid.module.shares.schema.ShareTypeEntity
import org.solyton.solawi.bid.module.user.exception.UserManagementException
import org.solyton.solawi.bid.module.user.schema.UserProfileEntity
import java.util.*
import org.evoleq.math.or as hasChanges

/**
 * A provider can create ShareTypes
 */
fun Transaction.createShareType(
    providerId: UUID,
    name: String,
    key: String,
    description: String,
    creator: UUID
): ShareTypeEntity {
    val exists = !ShareTypeEntity.find {
        ShareTypesTable.providerId eq providerId and (ShareTypesTable.name eq name)
    }.empty()
    if(exists) throw ShareException.DuplicateNameOfShareTypeAtProvider(name, providerId.toString())

    return ShareTypeEntity.new{
        createdBy = creator
        this.name = name
        this.key = key
        this.providerId = providerId
        this.description = description
    }
}

/**
 * A provider can create ShareTypes
 */
fun Transaction.updateShareType(
    shareTypeId: UUID,
    providerId: UUID,
    name: String,
    key: String,
    description: String,
    modifier: UUID
): ShareTypeEntity {
    val shareType = validatedShareType(shareTypeId)

    val providerIdChanged = shareType.providerId != providerId
    val nameChanged = shareType.name != name
    val keyChanged = shareType.key != key
    val descriptionChanged = shareType.description != description

    if(providerIdChanged) {
        shareType.providerId = providerId
    }
    if(nameChanged) {
        shareType.name = name
    }
    if(keyChanged) {
        shareType.key = key
    }
    if(descriptionChanged) {
        shareType.description = description
    }
    if( nameChanged || descriptionChanged || providerIdChanged ) {
        shareType.markModifiedBy(modifier)
    }

    return shareType
}
fun Transaction.readShareTypesByProvider(
    providerId: UUID
) = ShareTypeEntity.find {  ShareTypesTable.providerId eq providerId }.toList()

fun Transaction.deleteShareType(shareTypeId: UUID): UUID {
    try {
        ShareTypesTable.deleteWhere { ShareTypesTable.id eq shareTypeId }
    } catch(exception: Exception) {
        throw ShareException.CannotDeleteShareType(
            id, exception.message?: NO_MESSAGE_PROVIDED
        )
    }
    return shareTypeId
}

fun Transaction.deleteShareTypeOfProvider(providerId: UUID) {
    try {
        ShareTypesTable.deleteWhere { ShareTypesTable.providerId eq providerId }
    } catch(exception: Exception) {
        throw ShareException.CannotDeleteShareTypesOfProvider(
            providerId.toString(),
            exception.message?: NO_MESSAGE_PROVIDED
        )
    }

    return
}


/**
 *
 */
fun Transaction.createShareOffer(
    shareTypeId: UUID,
    fiscalYearId: UUID,
    price: Double?,
    pricingType: PricingType,
    ahcAuthorizationRequired: Boolean,
    creator: UUID
): ShareOfferEntity {
    val shareType = validatedShareType(shareTypeId)
    val fiscalYear = validatedFiscalYear(fiscalYearId)
    validatePriceAndPricingType(price, pricingType)

    return ShareOfferEntity.new {
        createdBy = creator
        this.shareType = shareType
        this.fiscalYear = fiscalYear
        this.price = price
        this.pricingType = pricingType
        this.ahcAuthorizationRequired = ahcAuthorizationRequired
    }
}

fun Transaction.updateShareOffer(
    shareOfferId: UUID,
    shareTypeId: UUID,
    fiscalYearId: UUID,
    price: Double?,
    pricingType: PricingType,
    ahcAuthorizationRequired: Boolean,
    modifier: UUID
): ShareOfferEntity {
    val shareOffer = validatedShareOffer(shareOfferId)
    val shareTypeChanged = shareOffer.shareType.id.value != shareTypeId
    val fiscalYearChanged = shareOffer.fiscalYear.id.value != fiscalYearId
    val priceChanged = shareOffer.price != price
    val pricingTypeChanged = shareOffer.pricingType != pricingType
    val ahcAuthorizationRequiredChanged = shareOffer.ahcAuthorizationRequired != ahcAuthorizationRequired

    if(shareTypeChanged) {
        val newShareType = validatedShareType(shareTypeId)
        shareOffer.shareType = newShareType
    }
    if(fiscalYearChanged) {
        val newFiscalYear = validatedFiscalYear(fiscalYearId)
        shareOffer.fiscalYear = newFiscalYear
    }
    if(priceChanged || pricingTypeChanged) {
        validatePriceAndPricingType(price, pricingType)
        shareOffer.price = price
        shareOffer.pricingType = pricingType
    }
    if(ahcAuthorizationRequiredChanged) {
        shareOffer.ahcAuthorizationRequired = ahcAuthorizationRequired
    }
    if( hasChanges(
        shareTypeChanged,
        fiscalYearChanged,
        priceChanged,
        pricingTypeChanged,
        ahcAuthorizationRequired
    )) {
        shareOffer.markModifiedBy(modifier)
    }

    return shareOffer
}

/**
 * Read ShareOffers by provider and filter by fiscal years.
 * If the list of provided filterYearIds is empty, no filter will be applied
 */
fun Transaction.readShareOffersByProvider(
        providerId: UUID,
        fiscalYearIds: Set<UUID> = emptySet()
): List<ShareOfferEntity> {
    val shareTypes = readShareTypesByProvider(providerId)
    val allShareOffers = shareTypes.flatMap {type -> type.shareOffers }
    return when{
        fiscalYearIds.isEmpty() -> allShareOffers
        else -> allShareOffers.filterNot{ offer -> offer.fiscalYear.id.value in fiscalYearIds }
    }
}

fun Transaction.deleteShareOffer(shareOfferId: UUID): UUID {
    try {
        ShareOffersTable.deleteWhere { ShareOffersTable.id eq shareOfferId }
    } catch (exception: Exception) {
        throw ShareException.CannotDeleteShareOffer(
            shareOfferId.toString(),
            exception.message?: NO_MESSAGE_PROVIDED
        )
    }

    return shareOfferId
}



fun Transaction.createShareSubscription(
    shareOfferId: UUID,
    userProfileId: UUID,
    distributionPointId: UUID,
    fiscalYearId: UUID,
    numberOfShares: Int,
    pricePerShare: Double?,
    ahcAuthorized: Boolean?,
    creator: UUID
): ShareSubscriptionEntity {
    val shareOffer = validatedShareOffer(shareOfferId)
    val fiscalYear = validatedFiscalYear(fiscalYearId)
    validateSubscription(shareOffer, fiscalYear)
    validateNumberOfShares(numberOfShares)
    validatePricePerShare(pricePerShare, shareOffer)

    val userProfile = validatedUserProfile(userProfileId)
    val distributionPoint = validatedDistributionPoint(distributionPointId)

    val initialStatus = initStatus()

    return ShareSubscription.new {
        createdBy = creator
        this.shareOffer = shareOffer
        this.pricePerShare = pricePerShare
        this.numberOfShares = numberOfShares
        this.fiscalYear = fiscalYear
        this.distributionPoint = distributionPoint
        this.userProfile = userProfile
        this.ahcAuthorized = ahcAuthorized
        this.status = initialStatus
    }
}

/**
 * Update [ShareSubscription]
 * Note: This function does not apply status changes
 */
fun Transaction.updateShareSubscription(
    shareSubscriptionId: UUID,
    shareOfferId: UUID,
    userProfileId: UUID,
    distributionPointId: UUID,
    fiscalYearId: UUID,
    numberOfShares: Int,
    pricePerShare: Double?,
    ahcAuthorized: Boolean?,
    modifier: UUID
): ShareSubscriptionEntity {
    val shareSubscription = validatedShareSubscription(shareSubscriptionId)

    val shareOfferChanged = shareSubscription.shareOffer.id.value != shareOfferId
    val fiscalYearChanged = shareSubscription.fiscalYear.id.value != fiscalYearId
    val userProfileChanged = shareSubscription.userProfile.id.value != userProfileId
    val distributionPointChanged = shareSubscription.distributionPoint?.id?.value != distributionPointId
    val numberOfSharesChanged = shareSubscription.numberOfShares != numberOfShares
    val pricePerShareChanged = shareSubscription.pricePerShare != pricePerShare
    val ahcAuthorizedChanged = shareSubscription.ahcAuthorized != ahcAuthorized

    if(shareOfferChanged || fiscalYearChanged || pricePerShareChanged) {
        val shareOffer = validatedShareOffer(shareOfferId)
        val fiscalYear = validatedFiscalYear(fiscalYearId)
        validateSubscription(shareOffer, fiscalYear)
        validateNumberOfShares(numberOfShares)
        validatePricePerShare(pricePerShare, shareOffer)

        shareSubscription.shareOffer = shareOffer
        shareSubscription.fiscalYear = fiscalYear
        shareSubscription.pricePerShare = pricePerShare
    }
    if(userProfileChanged) {
        val userProfile = validatedUserProfile(userProfileId)
        shareSubscription.userProfile = userProfile
    }
    if(distributionPointChanged) {
        val distributionPoint = validatedDistributionPoint(distributionPointId)
        shareSubscription.distributionPoint = distributionPoint
    }

    if(hasChanges(
        shareOfferChanged,
        fiscalYearChanged,
        userProfileChanged,
        distributionPointChanged,
        numberOfSharesChanged,
        pricePerShareChanged,
        ahcAuthorizedChanged
    )) shareSubscription.markModifiedBy(modifier)

    return shareSubscription
}
/**
* Read personal ShareOffers by userProfile and filter by fiscal years.
* If the list of provided filterYearIds is empty, no filter will be applied
*/
fun Transaction.readPersonalShareSubscriptions(
    userProfileId: UUID,
    fiscalYearIds: Set<UUID> = emptySet()
): List<ShareSubscription> {

    val allShareSubscriptions = ShareSubscriptionEntity.find { ShareSubscriptionsTable.userProfileId eq userProfileId }.toList()
    return when{
        fiscalYearIds.isEmpty() -> allShareSubscriptions
        else -> allShareSubscriptions.filter {
            shareSubscription -> shareSubscription.fiscalYear.id.value in fiscalYearIds
        }
    }
}

/**
 * Read ShareOffers by providerId and filter by fiscal years.
 * If the list of provided filterYearIds is empty, no filter will be applied
 */
fun Transaction.readShareSubscriptionsOfProvider(
    providerId: UUID,
    fiscalYearIds: Set<UUID> = emptySet()
): List<ShareSubscription> {

    val shareOffers = readShareOffersByProvider(providerId, fiscalYearIds)
    val allShareSubscriptions = shareOffers.flatMap { it.shareSubscriptions }
    return allShareSubscriptions
}

fun Transaction.validatePriceAndPricingType(price: Double?, pricingType: PricingType) {
    when{
        price == null && pricingType == PricingType.FIXED -> throw ShareException.InvalidPricing(price, pricingType)
        (price != null && (price < 0 || pricingType == PricingType.FLEXIBLE)) -> throw ShareException.InvalidPricing(price,pricingType)
    }
}

fun Transaction.validateSubscription(shareOffer: ShareOfferEntity, fiscalYear: FiscalYearEntity) {
    if(shareOffer.fiscalYear != fiscalYear) throw ShareException.FiscalYearMismatch(
        fiscalYear.id.value.toString(),
        shareOffer.id.value.toString()
    )
}

fun Transaction.validatedShareSubscription(shareSubscriptionId: UUID): ShareSubscriptionEntity =
    ShareSubscriptionEntity.findById(shareSubscriptionId)?: throw ShareException.NoSuchShareSubscription(shareSubscriptionId.toString())

fun Transaction.validatedShareType(shareTypeId: UUID): ShareTypeEntity =
    ShareTypeEntity.findById(shareTypeId)?: throw ShareException.NoSuchShareType(shareTypeId.toString())

fun Transaction.validatedShareOffer(shareOfferId: UUID): ShareOfferEntity =
    ShareOfferEntity.findById(shareOfferId)?: throw ShareException.NoSuchShareOffer(shareOfferId.toString())

fun Transaction.validatedUserProfile(userProfileId: UUID): UserProfileEntity =
    UserProfileEntity.findById(userProfileId)?: throw UserManagementException.NoSuchUserProfile(userProfileId.toString())

fun validateNumberOfShares(numberOfShares: Int) {
    if(numberOfShares <= 0) throw ShareException.InvalidNumberOfShares(numberOfShares)
}

fun Transaction.validatePricePerShare(pricePerShare: Double?, shareOffer: ShareOfferEntity) {
    val pricingType = shareOffer.pricingType
    when{
        pricePerShare == null && pricingType == PricingType.FLEXIBLE -> ShareException.InvalidPricePerShare(pricePerShare, pricingType)
        pricePerShare != null && pricingType == PricingType.FIXED -> ShareException.InvalidPricePerShare(pricePerShare, pricingType)
        (pricePerShare != null && pricePerShare < 0) -> ShareException.InvalidPricePerShare(pricePerShare, pricingType)
    }
}

