package org.solyton.solawi.bid.module.bid.permission

import org.evoleq.value.StringValueWithDescription

object BidRight {

    object BidRound {
        val manage = object : StringValueWithDescription {
            override val value: String= "MANAGE_BID_ROUND"
            override val description: String = "Manage Bid round"
        }

    }
    object Auction {
        val manage = object : StringValueWithDescription {
            override val value: String= "MANAGE_AUCTION"
            override val description: String = "Manage Auction"
        }
        val create = object : StringValueWithDescription {
            override val value: String= "CREATE_AUCTION"
            override val description: String = "Create Auction"
        }
        val read = object : StringValueWithDescription {
            override val value: String= "READ_AUCTION"
            override val description: String = "Read Auction"
        }
        val update = object : StringValueWithDescription {
            override val value: String= "UPDATE_AUCTION"
            override val description: String = "Update Auction"
        }
        val delete = object : StringValueWithDescription {
            override val value: String= "DELETE_AUCTION"
            override val description: String = "Delete Auction"
        }
        val importBidders = object : StringValueWithDescription {
            override val value: String= "IMPORT_BIDDERS"
            override val description: String = "Import Bidders"
        }

        // Moderation Rights
        val moderateAuction = object : StringValueWithDescription {
            override val value: String= "MODERATE_AUCTION"
            override val description: String = "Moderate Auction"
        }


    }
}
