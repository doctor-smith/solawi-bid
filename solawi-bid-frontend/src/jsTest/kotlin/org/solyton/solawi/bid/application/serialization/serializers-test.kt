package org.solyton.solawi.bid.application.serialization

import org.evoleq.ktorx.client.Parameters
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.serializers
import org.solyton.solawi.bid.module.authentication.data.api.*
import org.solyton.solawi.bid.module.banking.data.api.*
import org.solyton.solawi.bid.module.bid.data.api.Auction
import org.solyton.solawi.bid.module.bid.data.api.Auctions
import org.solyton.solawi.bid.module.bid.data.api.CreateAuction
import org.solyton.solawi.bid.module.bid.data.api.PricingType
import org.solyton.solawi.bid.module.distribution.data.api.*
import org.solyton.solawi.bid.module.shares.data.api.*
import org.solyton.solawi.bid.module.user.data.api.userprofile.*
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals

// todo:test add the missing classes
class SerializersTest {
    @Test
    fun installSerializersTest() {
        installSerializers()
        val classes = listOf<KClass<*>>(
            String::class,
            Int::class,
            Double::class,
            Boolean::class,
            Unit::class,

            Parameters::class,

            Result::class,
            Result.Failure::class,
            Result.Failure.Message::class,

            Identifier::class,

            Login::class,
            LoggedIn::class,
            RefreshToken::class,
            // AccessToken::class,
            IsLoggedIn::class,
            LoggedInAs::class,
            Logout::class,

            Auctions::class,
            Auction::class,
            CreateAuction::class,

            // ...

            UserProfile::class,
            UserProfiles::class,
            ReadUserProfiles::class,
            ReadUserProfile::class,
            CreateUserProfile::class,
            UpdateUserProfile::class,
            DeleteUserProfile::class,
            UserProfileToImport::class,
            ImportUserProfiles::class,

            Address::class,
            CreateAddress::class,
            ReadAddress::class,
            ReadAddresses::class,
            UpdateAddress::class,
            DeleteAddress::class,

            Share::class,
            Shares::class,
            ShareType::class,
            ShareTypes::class,
            CreateShareType::class,
            ReadShareTypes::class,
            UpdateShareType::class,
            ShareOffer::class,
            ShareOffers::class,
            CreateShareOffer::class,
            ReadShareOffers::class,
            UpdateShareOffer::class,
            ShareSubscription::class,
            ShareSubscriptions::class,
            CreateShareSubscription::class,
            ReadShareSubscriptions::class,
            UpdateShareSubscription::class,
            ImportShareSubscriptions::class,
            ImportShareSubscriptions::class,
            ShareStatus::class,
            PricingType::class,

            DistributionPoint::class,
            DistributionPoints::class,
            CreateDistributionPoint::class,
            UpdateDistributionPoint::class,
            ReadDistributionPoint::class,
            ReadDistributionPoints::class,
            DeleteDistributionPoint::class,
            CreateOrUseAddress::class,
            CreateOrUseAddress.Create::class,
            CreateOrUseAddress.Use::class,

            BankAccount::class,
            ReadBankAccount::class,
            ReadBankAccounts::class,
            CreateBankAccount::class,
            UpdateBankAccount::class,
            DeleteBankAccount::class,

            FiscalYear::class,
            FiscalYears::class,
            ReadFiscalYear::class,
            ReadFiscalYears::class,
            CreateFiscalYear::class,
            UpdateFiscalYear::class,
            DeleteFiscalYear::class,

            *organizationSerializers.toTypedArray()
        )
        val installed = serializers.keys.toList()
        classes.forEach { assertEquals(true, installed.contains(it), "Serializer for Class $it not installed") }
    }
}
