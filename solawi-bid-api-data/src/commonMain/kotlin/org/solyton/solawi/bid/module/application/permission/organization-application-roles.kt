package org.solyton.solawi.bid.module.application.permission

fun delegableOrganizationApplicationRoles(applicationName: String): Set<String> = when (applicationName) {
    "AUCTIONS" -> setOf("AUCTION_MANAGER")
    "BANKING" -> setOf("LEGAL_ENTITY", "BANK_ACCOUNT_MANAGER", "BANK_TRANSACTION_MANAGER")
    "DISTRIBUTION" -> setOf("DISTRIBUTION_POINT_MANAGER")
    "SHARE_MANAGEMENT" -> setOf("SHARE_MANAGER")
    else -> emptySet()
}
