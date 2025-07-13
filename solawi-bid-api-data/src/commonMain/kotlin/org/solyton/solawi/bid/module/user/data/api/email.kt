package org.solyton.solawi.bid.module.user.data.api

import kotlinx.serialization.Serializable


// Dummy data
@Serializable
data class SendMailForRegistrationConfirmation(
    val recipient: String,
    val confirmationLink: String
)


// Dummy data
@Serializable
data class MailForRegistrationConfirmationSent(
    val recipient: String
)
