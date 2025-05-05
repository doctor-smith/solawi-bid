package org.solyton.solawi.bid.module.db.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import java.util.UUID

typealias SearchBiddersTable = SearchBidders
typealias SearchBidderEntity = SearchBidder

object SearchBidders : UUIDTable("search-bidders") {
    val firstname = varchar("firstname", 256)
    val lastname = varchar("lastname", 256)
    val email = varchar("email", 256)
    val relatedEmails = varchar("related_emails", 2048)
    val relatedNames = varchar("related_names", 2048)
}

class SearchBidder(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<SearchBidder>(SearchBidders)
    var firstname by SearchBidders.firstname
    var lastname by SearchBidders.lastname
    var email by SearchBidders.email
    var relatedEmails by SearchBidders.relatedEmails
    var relatedNames by SearchBidders.relatedNames
}