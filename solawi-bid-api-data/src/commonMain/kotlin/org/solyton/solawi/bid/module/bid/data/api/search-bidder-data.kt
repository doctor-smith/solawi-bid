package org.solyton.solawi.bid.module.bid.data.api

import kotlinx.serialization.Serializable

@Serializable
data class SearchBidderData(
    val firstname: String,
    val lastname: String,
    val relatedEmails: List<String> = listOf(),
    val relatedFirstnames: List<String> = listOf(),
    val relatedLastnames: List<String> = listOf(),
)

@Serializable
data class BidderData(
    val firstname: String,
    val lastname: String,
    val email: String,
    val numberOfShares: Int,
    val numberOfEggShares: Int,
    val relatedEmails: List<String> = listOf(),
    val relatedFirstnames: List<String> = listOf(),
    val relatedLastnames: List<String> = listOf(),
)

@Serializable
data class BidderMails(
    val emails: List<String> = listOf(),
)