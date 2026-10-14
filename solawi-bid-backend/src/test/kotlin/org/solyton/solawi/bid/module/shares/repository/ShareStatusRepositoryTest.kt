package org.solyton.solawi.bid.module.shares.repository

import org.evoleq.exposedx.test.runSimpleH2Test
import org.evoleq.uuid.UUID_ZERO
import org.jetbrains.exposed.dao.flushCache
import org.jetbrains.exposed.sql.Transaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.solyton.solawi.bid.DbFunctional
import org.solyton.solawi.bid.module.banking.data.*
import org.solyton.solawi.bid.module.banking.repository.*
import org.solyton.solawi.bid.module.banking.schema.*
import org.solyton.solawi.bid.module.permission.schema.ContextsTable
import org.solyton.solawi.bid.module.permission.schema.RightsTable
import org.solyton.solawi.bid.module.permission.schema.RolesTable
import org.solyton.solawi.bid.module.shares.data.internal.ChangedBy
import org.solyton.solawi.bid.module.shares.data.internal.ShareStatus
import org.solyton.solawi.bid.module.shares.data.values.ShareSubscriptionId
import org.solyton.solawi.bid.module.shares.exception.ShareStatusException
import org.solyton.solawi.bid.module.shares.schema.*
import org.solyton.solawi.bid.module.user.schema.*
import org.solyton.solawi.bid.module.values.UserId
import org.solyton.solawi.bid.module.values.UserProfileId
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class ShareStatusRepositoryTest {

    val tables = arrayOf(
        ShareStatusTable,
        ShareSubscriptionStatusHistory,
        ShareSubscriptionsTable,
        ShareOffersTable,
        ShareTypesTable,
        FiscalYearsTable,
        UserProfilesTable,
        UsersTable,
        CoSubscribersTable,
        SepaMandatesTable,
        SepaCollectionsTable,
        SepaMandateCollectionsTable,
        SepaMandateDataMappingsTable,
        SepaPaymentTemplatesTable,
        BankAccountsTable,
        BankAccountAccessorsTable,
        LegalEntitiesTable,
        CreditorIdentifiersTable,
        AddressesTable,
        RolesTable,
        RightsTable,
        ContextsTable
    )


    fun Transaction.setup() {
        createTestShareStatuses()
    }

    @DbFunctional@Test
    fun initStatus() = runSimpleH2Test(*tables) {
        setup()
        val initStatus = initStatus()
        assertEquals(initStatus.name, ShareStatus.PendingActivation.toString())
    }



    @DbFunctional@ParameterizedTest(name = "Test ShareStatus: {0}")
    @MethodSource("allShareStatuses")
    fun testShareStatusTransition(status: ShareStatus) = runSimpleH2Test(*tables){
            setup()
            val statusEntity = statusEntity(status)
            assertEquals("$status", statusEntity.name)

    }

    companion object {
        @JvmStatic
        fun allShareStatuses(): Array<ShareStatus> = shareStatuses

    }

    @DbFunctional@Test
    fun `transfer share subscription resets ahcAuthorized and removes mandate data mapping`() = runSimpleH2Test(*tables) {
        setup()

        val oldUser = UserEntity.new {
            username = "old_user"
            password = "password"
            status = UserStatus.ACTIVE
            createdBy = UUID_ZERO
        }
        val oldProfile = UserProfileEntity.new {
            user = oldUser
            firstName = "Old"
            lastName = "User"
            createdBy = UUID_ZERO
        }
        val newUser = UserEntity.new {
            username = "new_user"
            password = "password"
            status = UserStatus.ACTIVE
            createdBy = UUID_ZERO
        }
        val newProfile = UserProfileEntity.new {
            user = newUser
            firstName = "New"
            lastName = "User"
            createdBy = UUID_ZERO
        }

        val now = org.joda.time.DateTime.now()
        val fiscalYear = createFiscalYear(UUID_ONE, now, now.plusYears(1), UUID_ZERO)
        val shareType = ShareTypeEntity.new {
            createdBy = UUID_ZERO
            providerId = UUID_ONE
            name = "Type"
            key = "Key"
            description = "Desc"
        }
        val shareOffer = ShareOfferEntity.new {
            this.shareType = shareType
            this.fiscalYear = fiscalYear
            this.pricingType = PricingType.FIXED
            this.createdBy = UUID_ZERO
        }
        val subscription = ShareSubscriptionEntity.new {
            createdBy = UUID_ZERO
            this.shareOffer = shareOffer
            this.userProfile = oldProfile
            this.fiscalYear = fiscalYear
            this.status = statusEntity(ShareStatus.Subscribed)
            this.ahcAuthorized = true
        }

        val address = AddressEntity.new {
            recipientName = "Test "
            addressLine1 = "Street"
            addressLine2 = ""
            city = "City"
            postalCode = "12345"
            countryCode = "DE"
            stateOrProvince = "State"
            createdBy = UUID_ZERO
        }
        val legalEntity = createLegalEntity(oldUser.id.value, "Legal", "LF", LegalEntityType.HUMAN, address.id.value, UUID_ZERO)
        val bankAccount = createBankAccount(
            oldUser.id.value,
            IBAN("DE89370400440532013000"),
            BIC("COBADEFFXXX"),
            "Bank",
            true,
            AccountType.CREDITOR,
            emptyList(),
            "Acc",
            UUID_ZERO
        )
        val creditorIdentifier = CreditorIdentifier.new {
            createdBy = UUID_ZERO
            this.creditorId = "DE98ZZZ12345678901"
            this.legalEntity = legalEntity
            isActive = true
            validUntil = now.plusYears(1)
            validFrom = now.minusYears(1)
        }
        val collection = createSepaCollection(
            UUID_ZERO,
            CreditorIdentifierId(creditorIdentifier.id.value.toString()),
            BankAccountId(bankAccount.id.value.toString()),
            MandateReferencePrefix("MANDATE"),
            SepaCollectionKey("COLKEY"),
            RemittanceInformation("INFO"),
            SepaSequenceType.FRST,
            null
        )
        val mandate = createSepaMandateWithRetry(
            creatorId = UUID_ZERO,
            creditorIdentifierId = creditorIdentifier.id.value,
            debtorBankAccountId = bankAccount.id.value,
            debtorName = "Debtor",
            signedAt = now,
            validFrom = now,
            validUntil = null,
            collectionId = collection.id.value,
        )

        SepaMandateDataMapping.new {
            createdBy = UUID_ZERO
            this.mandate = mandate
            this.referenceId = subscription.id.value
            this.amount = 120.0
            this.collection = collection
        }
        flushCache()

        assertTrue(subscription.ahcAuthorized!!)
        assertEquals(1, SepaMandateDataMapping.find { SepaMandateDataMappings.referenceId eq subscription.id.value }.count())

        val updated = transfer(
            modifier = UserId(UUID_ONE.toString()),
            shareSubscriptionId = ShareSubscriptionId(subscription.id.value.toString()),
            newOwnerId = UserProfileId(newProfile.id.value.toString()),
            changedBy = ChangedBy.PROVIDER
        )
        flushCache()

        assertEquals(newProfile.id.value, updated.userProfile.id.value)
        assertFalse(updated.ahcAuthorized!!, "ahcAuthorized should be reset to false after transfer")
        assertEquals(0, SepaMandateDataMapping.find { SepaMandateDataMappings.referenceId eq subscription.id.value }.count(), "Mandate mapping should be deleted")
    }

    @DbFunctional@Test
    fun `transfer share subscription fails when changedBy is not PROVIDER`() = runSimpleH2Test(*tables) {
        setup()

        val user = UserEntity.new {
            username = "u"
            password = "p"
            status = UserStatus.ACTIVE
            createdBy = UUID_ZERO
        }
        val profile = UserProfileEntity.new {
            this.user = user
            firstName = "A"
            lastName = "B"
            createdBy = UUID_ZERO
        }
        val subscription = ShareSubscriptionEntity.new {
            createdBy = UUID_ZERO
            this.shareOffer = ShareOfferEntity.new {
                this.shareType = ShareTypeEntity.new {
                    createdBy = UUID_ZERO
                    providerId = UUID_ONE
                    name = "T"
                    key = "K"
                    description = "D"
                }
                this.fiscalYear = createFiscalYear(UUID_ONE, org.joda.time.DateTime.now(), org.joda.time.DateTime.now().plusYears(1), UUID_ZERO)
                this.pricingType = PricingType.FIXED
                this.createdBy = UUID_ZERO
            }
            this.userProfile = profile
            this.fiscalYear = this.shareOffer.fiscalYear
            this.status = statusEntity(ShareStatus.Subscribed)
            this.ahcAuthorized = true
        }

        assertThrows<ShareStatusException.MissingTransitionPermission> {
            transfer(
                modifier = UserId(UUID_ONE.toString()),
                shareSubscriptionId = ShareSubscriptionId(subscription.id.value.toString()),
                newOwnerId = UserProfileId(profile.id.value.toString()),
                changedBy = ChangedBy.USER
            )
        }
    }
}
