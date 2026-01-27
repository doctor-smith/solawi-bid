package org.solyton.solawi.bid.module.bid.service.shares

import org.evoleq.uuid.UUID_ZERO
import org.jetbrains.exposed.sql.Transaction
import org.solyton.solawi.bid.module.banking.schema.FiscalYearEntity
import org.solyton.solawi.bid.module.banking.schema.FiscalYearsTable
import org.solyton.solawi.bid.module.bid.schema.DistributionPointEntity
import org.solyton.solawi.bid.module.bid.schema.DistributionPointsTable
import org.solyton.solawi.bid.module.bid.schema.ShareOfferEntity
import org.solyton.solawi.bid.module.bid.schema.ShareOffersTable
import org.solyton.solawi.bid.module.user.schema.OrganizationEntity
import org.solyton.solawi.bid.module.user.schema.OrganizationsTable
import org.solyton.solawi.bid.module.user.schema.UserProfileEntity
import org.solyton.solawi.bid.module.user.schema.UserProfilesTable
import java.util.*

/**
 * Transforms a given ShareToImport instance by resolving its associated entities' IDs
 * based on the current transaction context.
 *
 * @param shareToImport The ShareToImport instance to be transformed.
 * @return A new ShareToImport instance with updated references using resolved IDs.
 */
fun Transaction.transform(shareToImport: ShareToImport): ShareToImport = with(shareToImport) {
    val shareOfferId = shareOfferByCreator(shareToImport.shareOfferId).id.value
    val userProfileId = userProfileByCreator(shareToImport.userProfileId).id.value
    val distributionPointId = distributionPointByCreator(shareToImport.distributionPointId?: UUID_ZERO)?.id?.value
    ShareToImport(
        shareOfferId,
        userProfileId,
        distributionPointId,
        shareToImport.numberOfShares,
        shareToImport.pricePerShare,
        shareToImport.ahcAuthorized,
        shareToImport.status,
        shareToImport.coSubscribers
    )
}


fun Transaction.providerByCreator(creatorId: UUID): OrganizationEntity = OrganizationEntity.find {
    OrganizationsTable.createdBy eq creatorId
}.first()
fun Transaction.fiscalYearByCreator(creatorId: UUID): FiscalYearEntity = FiscalYearEntity.find {
    FiscalYearsTable.createdBy eq creatorId
}.first()

fun Transaction.userProfileByCreator(creatorId: UUID) = UserProfileEntity.find {
    UserProfilesTable.createdBy eq creatorId
}.first()

fun Transaction.distributionPointByCreator(creatorId: UUID): DistributionPointEntity? = DistributionPointEntity.find {
    DistributionPointsTable.createdBy eq creatorId
}.firstOrNull()

fun Transaction.shareOfferByCreator(creatorId: UUID) = ShareOfferEntity.find {
    ShareOffersTable.createdBy eq creatorId
}.first()


infix fun <R, S, T> (Transaction.(S)->T).o(other: Transaction.(R)->S): Transaction.(R)->T = {
        r -> this@o(other(r))
}

val UUID_1: UUID =  UUID.fromString("00000000-0000-0000-0000-000000000001")
val UUID_2 : UUID=  UUID.fromString("00000000-0000-0000-0000-000000000002")
val UUID_3 : UUID=  UUID.fromString("00000000-0000-0000-0000-000000000003")
val UUID_4 : UUID=  UUID.fromString("00000000-0000-0000-0000-000000000004")
val UUID_5 : UUID =  UUID.fromString("00000000-0000-0000-0000-000000000005")
val UUID_6 : UUID=  UUID.fromString("00000000-0000-0000-0000-000000000006")
val UUID_7 : UUID=  UUID.fromString("00000000-0000-0000-0000-000000000007")
val UUID_8 : UUID=  UUID.fromString("00000000-0000-0000-0000-000000000008")
val UUID_9 : UUID=  UUID.fromString("00000000-0000-0000-0000-000000000009")
val UUID_10: UUID = UUID.fromString("00000000-0000-0000-0000-000000000010")
val UUID_11: UUID = UUID.fromString("00000000-0000-0000-0000-000000000011")
val UUID_12: UUID = UUID.fromString("00000000-0000-0000-0000-000000000012")
val UUID_13: UUID = UUID.fromString("00000000-0000-0000-0000-000000000013")
val UUID_14: UUID = UUID.fromString("00000000-0000-0000-0000-000000000014")
val UUID_15: UUID = UUID.fromString("00000000-0000-0000-0000-000000000015")
val UUID_16: UUID = UUID.fromString("00000000-0000-0000-0000-000000000016")
val UUID_17: UUID = UUID.fromString("00000000-0000-0000-0000-000000000017")
val UUID_18: UUID = UUID.fromString("00000000-0000-0000-0000-000000000018")
val UUID_19: UUID = UUID.fromString("00000000-0000-0000-0000-000000000019")
val UUID_20: UUID = UUID.fromString("00000000-0000-0000-0000-000000000020")
val UUID_21: UUID = UUID.fromString("00000000-0000-0000-0000-000000000021")
val UUID_22: UUID = UUID.fromString("00000000-0000-0000-0000-000000000022")
val UUID_23: UUID = UUID.fromString("00000000-0000-0000-0000-000000000023")
val UUID_24: UUID = UUID.fromString("00000000-0000-0000-0000-000000000024")
val UUID_25: UUID = UUID.fromString("00000000-0000-0000-0000-000000000025")
val UUID_26: UUID = UUID.fromString("00000000-0000-0000-0000-000000000026")
val UUID_27: UUID = UUID.fromString("00000000-0000-0000-0000-000000000027")
val UUID_28: UUID = UUID.fromString("00000000-0000-0000-0000-000000000028")
val UUID_29: UUID = UUID.fromString("00000000-0000-0000-0000-000000000029")
val UUID_30: UUID = UUID.fromString("00000000-0000-0000-0000-000000000030")
val UUID_31: UUID = UUID.fromString("00000000-0000-0000-0000-000000000031")
val UUID_32: UUID = UUID.fromString("00000000-0000-0000-0000-000000000032")
val UUID_33: UUID = UUID.fromString("00000000-0000-0000-0000-000000000033")
val UUID_34: UUID = UUID.fromString("00000000-0000-0000-0000-000000000034")
val UUID_35: UUID = UUID.fromString("00000000-0000-0000-0000-000000000035")
val UUID_36: UUID = UUID.fromString("00000000-0000-0000-0000-000000000036")
val UUID_37: UUID = UUID.fromString("00000000-0000-0000-0000-000000000037")
val UUID_38: UUID = UUID.fromString("00000000-0000-0000-0000-000000000038")
val UUID_39: UUID = UUID.fromString("00000000-0000-0000-0000-000000000039")
val UUID_40: UUID = UUID.fromString("00000000-0000-0000-0000-000000000040")
val UUID_41: UUID = UUID.fromString("00000000-0000-0000-0000-000000000041")
val UUID_42: UUID = UUID.fromString("00000000-0000-0000-0000-000000000042")
val UUID_43: UUID = UUID.fromString("00000000-0000-0000-0000-000000000043")
val UUID_44: UUID = UUID.fromString("00000000-0000-0000-0000-000000000044")
val UUID_45: UUID = UUID.fromString("00000000-0000-0000-0000-000000000045")
val UUID_46: UUID = UUID.fromString("00000000-0000-0000-0000-000000000046")
val UUID_47: UUID = UUID.fromString("00000000-0000-0000-0000-000000000047")
val UUID_48: UUID = UUID.fromString("00000000-0000-0000-0000-000000000048")
val UUID_49: UUID = UUID.fromString("00000000-0000-0000-0000-000000000049")
val UUID_50: UUID = UUID.fromString("00000000-0000-0000-0000-000000000050")
val UUID_51: UUID = UUID.fromString("00000000-0000-0000-0000-000000000051")
val UUID_52: UUID = UUID.fromString("00000000-0000-0000-0000-000000000052")
val UUID_53: UUID = UUID.fromString("00000000-0000-0000-0000-000000000053")
val UUID_54: UUID = UUID.fromString("00000000-0000-0000-0000-000000000054")
val UUID_55: UUID = UUID.fromString("00000000-0000-0000-0000-000000000055")
val UUID_56: UUID = UUID.fromString("00000000-0000-0000-0000-000000000056")
val UUID_57: UUID = UUID.fromString("00000000-0000-0000-0000-000000000057")
val UUID_58: UUID = UUID.fromString("00000000-0000-0000-0000-000000000058")
val UUID_59: UUID = UUID.fromString("00000000-0000-0000-0000-000000000059")
val UUID_60: UUID = UUID.fromString("00000000-0000-0000-0000-000000000060")
val UUID_61: UUID = UUID.fromString("00000000-0000-0000-0000-000000000061")
val UUID_62: UUID = UUID.fromString("00000000-0000-0000-0000-000000000062")
val UUID_63: UUID = UUID.fromString("00000000-0000-0000-0000-000000000063")
val UUID_64: UUID = UUID.fromString("00000000-0000-0000-0000-000000000064")
val UUID_65: UUID = UUID.fromString("00000000-0000-0000-0000-000000000065")
val UUID_66: UUID = UUID.fromString("00000000-0000-0000-0000-000000000066")
val UUID_67: UUID = UUID.fromString("00000000-0000-0000-0000-000000000067")
val UUID_68: UUID = UUID.fromString("00000000-0000-0000-0000-000000000068")
val UUID_69: UUID = UUID.fromString("00000000-0000-0000-0000-000000000069")
val UUID_70: UUID = UUID.fromString("00000000-0000-0000-0000-000000000070")
val UUID_71: UUID = UUID.fromString("00000000-0000-0000-0000-000000000071")
val UUID_72: UUID = UUID.fromString("00000000-0000-0000-0000-000000000072")
val UUID_73: UUID = UUID.fromString("00000000-0000-0000-0000-000000000073")
val UUID_74: UUID = UUID.fromString("00000000-0000-0000-0000-000000000074")
val UUID_75: UUID = UUID.fromString("00000000-0000-0000-0000-000000000075")
val UUID_76: UUID = UUID.fromString("00000000-0000-0000-0000-000000000076")
val UUID_77: UUID = UUID.fromString("00000000-0000-0000-0000-000000000077")
val UUID_78: UUID = UUID.fromString("00000000-0000-0000-0000-000000000078")
val UUID_79: UUID = UUID.fromString("00000000-0000-0000-0000-000000000079")
val UUID_80: UUID = UUID.fromString("00000000-0000-0000-0000-000000000080")
val UUID_81: UUID = UUID.fromString("00000000-0000-0000-0000-000000000081")
val UUID_82: UUID = UUID.fromString("00000000-0000-0000-0000-000000000082")
val UUID_83: UUID = UUID.fromString("00000000-0000-0000-0000-000000000083")
val UUID_84: UUID = UUID.fromString("00000000-0000-0000-0000-000000000084")
val UUID_85: UUID = UUID.fromString("00000000-0000-0000-0000-000000000085")
val UUID_86: UUID = UUID.fromString("00000000-0000-0000-0000-000000000086")
val UUID_87: UUID = UUID.fromString("00000000-0000-0000-0000-000000000087")
val UUID_88: UUID = UUID.fromString("00000000-0000-0000-0000-000000000088")
val UUID_89: UUID = UUID.fromString("00000000-0000-0000-0000-000000000089")
val UUID_90: UUID = UUID.fromString("00000000-0000-0000-0000-000000000090")
val UUID_91: UUID = UUID.fromString("00000000-0000-0000-0000-000000000091")
val UUID_92: UUID = UUID.fromString("00000000-0000-0000-0000-000000000092")
val UUID_93: UUID = UUID.fromString("00000000-0000-0000-0000-000000000093")
val UUID_94: UUID = UUID.fromString("00000000-0000-0000-0000-000000000094")
val UUID_95: UUID = UUID.fromString("00000000-0000-0000-0000-000000000095")
val UUID_96: UUID = UUID.fromString("00000000-0000-0000-0000-000000000096")
val UUID_97: UUID = UUID.fromString("00000000-0000-0000-0000-000000000097")
val UUID_98: UUID = UUID.fromString("00000000-0000-0000-0000-000000000098")
val UUID_99: UUID = UUID.fromString("00000000-0000-0000-0000-000000000099")
val UUID_100: UUID = UUID.fromString("00000000-0000-0000-0000-000000000100")

