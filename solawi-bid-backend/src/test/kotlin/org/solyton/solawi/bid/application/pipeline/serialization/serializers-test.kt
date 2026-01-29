package org.solyton.solawi.bid.application.pipeline.serialization

import org.evoleq.ktorx.client.Parameters
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.serializers
import org.solyton.solawi.bid.Api
import org.solyton.solawi.bid.application.pipeline.installSerializers
import org.solyton.solawi.bid.module.authentication.data.api.Identifier
import org.solyton.solawi.bid.module.authentication.data.api.IsLoggedIn
import org.solyton.solawi.bid.module.authentication.data.api.LoggedIn
import org.solyton.solawi.bid.module.authentication.data.api.LoggedInAs
import org.solyton.solawi.bid.module.authentication.data.api.Login
import org.solyton.solawi.bid.module.authentication.data.api.Logout
import org.solyton.solawi.bid.module.authentication.data.api.RefreshToken
import org.solyton.solawi.bid.module.banking.data.api.BankAccount
import org.solyton.solawi.bid.module.banking.data.api.CreateBankAccount
import org.solyton.solawi.bid.module.banking.data.api.CreateFiscalYear
import org.solyton.solawi.bid.module.banking.data.api.DeleteBankAccount
import org.solyton.solawi.bid.module.banking.data.api.DeleteFiscalYear
import org.solyton.solawi.bid.module.banking.data.api.FiscalYear
import org.solyton.solawi.bid.module.banking.data.api.FiscalYears
import org.solyton.solawi.bid.module.banking.data.api.ReadBankAccount
import org.solyton.solawi.bid.module.banking.data.api.ReadBankAccounts
import org.solyton.solawi.bid.module.banking.data.api.ReadFiscalYear
import org.solyton.solawi.bid.module.banking.data.api.ReadFiscalYears
import org.solyton.solawi.bid.module.banking.data.api.UpdateBankAccount
import org.solyton.solawi.bid.module.banking.data.api.UpdateFiscalYear
import org.solyton.solawi.bid.module.bid.data.api.Auction
import org.solyton.solawi.bid.module.bid.data.api.Auctions
import org.solyton.solawi.bid.module.bid.data.api.CreateAuction
import org.solyton.solawi.bid.module.bid.data.api.CreateDistributionPoint
import org.solyton.solawi.bid.module.bid.data.api.CreateOrUseAddress
import org.solyton.solawi.bid.module.bid.data.api.CreateShareOffer
import org.solyton.solawi.bid.module.bid.data.api.CreateShareSubscription
import org.solyton.solawi.bid.module.bid.data.api.CreateShareType
import org.solyton.solawi.bid.module.bid.data.api.DeleteDistributionPoint
import org.solyton.solawi.bid.module.bid.data.api.DistributionPoint
import org.solyton.solawi.bid.module.bid.data.api.DistributionPoints
import org.solyton.solawi.bid.module.bid.data.api.ImportShareSubscriptions
import org.solyton.solawi.bid.module.bid.data.api.PricingType
import org.solyton.solawi.bid.module.bid.data.api.ReadDistributionPoint
import org.solyton.solawi.bid.module.bid.data.api.ReadDistributionPoints
import org.solyton.solawi.bid.module.bid.data.api.ReadShareOffers
import org.solyton.solawi.bid.module.bid.data.api.ReadShareSubscriptions
import org.solyton.solawi.bid.module.bid.data.api.ReadShareTypes
import org.solyton.solawi.bid.module.bid.data.api.Share
import org.solyton.solawi.bid.module.bid.data.api.ShareOffer
import org.solyton.solawi.bid.module.bid.data.api.ShareOffers
import org.solyton.solawi.bid.module.bid.data.api.ShareStatus
import org.solyton.solawi.bid.module.bid.data.api.ShareSubscription
import org.solyton.solawi.bid.module.bid.data.api.ShareSubscriptions
import org.solyton.solawi.bid.module.bid.data.api.ShareType
import org.solyton.solawi.bid.module.bid.data.api.ShareTypes
import org.solyton.solawi.bid.module.bid.data.api.Shares
import org.solyton.solawi.bid.module.bid.data.api.UpdateDistributionPoint
import org.solyton.solawi.bid.module.bid.data.api.UpdateShareOffer
import org.solyton.solawi.bid.module.bid.data.api.UpdateShareSubscription
import org.solyton.solawi.bid.module.bid.data.api.UpdateShareType
import org.solyton.solawi.bid.module.user.data.api.userprofile.Address
import org.solyton.solawi.bid.module.user.data.api.userprofile.CreateAddress
import org.solyton.solawi.bid.module.user.data.api.userprofile.CreateUserProfile
import org.solyton.solawi.bid.module.user.data.api.userprofile.DeleteAddress
import org.solyton.solawi.bid.module.user.data.api.userprofile.DeleteUserProfile
import org.solyton.solawi.bid.module.user.data.api.userprofile.ImportUserProfiles
import org.solyton.solawi.bid.module.user.data.api.userprofile.ReadAddress
import org.solyton.solawi.bid.module.user.data.api.userprofile.ReadAddresses
import org.solyton.solawi.bid.module.user.data.api.userprofile.ReadUserProfile
import org.solyton.solawi.bid.module.user.data.api.userprofile.ReadUserProfiles
import org.solyton.solawi.bid.module.user.data.api.userprofile.UpdateAddress
import org.solyton.solawi.bid.module.user.data.api.userprofile.UpdateUserProfile
import org.solyton.solawi.bid.module.user.data.api.userprofile.UserProfile
import org.solyton.solawi.bid.module.user.data.api.userprofile.UserProfileToImport
import org.solyton.solawi.bid.module.user.data.api.userprofile.UserProfiles
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals

// todo:test Add missing classes
class SerializersTest {
    @Api@Test
    fun installSerializersTest() {
        installSerializers()
        val classes = listOf<KClass<*>>(
            String::class,
            Int::class,
            Double::class,
            Boolean::class,
            Unit::class,

            Result::class,
            Result.Failure::class,
            Result.Failure.Message::class,

            Identifier::class,

            Parameters::class,

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

