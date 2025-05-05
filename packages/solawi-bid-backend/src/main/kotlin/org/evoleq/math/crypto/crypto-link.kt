package org.evoleq.math.crypto

import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

data class SecureLinkData(
    val encodedData: String,
    val signature: String
)

fun generateSecureLink(roundId: String, auctionId: String, secretKey: String): SecureLinkData {
    // Concatenate and encode the parameters
    val data = "roundId=$roundId&auctionId=$auctionId"
    val encodedData = Base64.getUrlEncoder().withoutPadding().encodeToString(data.toByteArray())

    // Generate HMAC SHA-256 signature
    val hmac = Mac.getInstance("HmacSHA256")
    val secretKeySpec = SecretKeySpec(secretKey.toByteArray(), "HmacSHA256")
    hmac.init(secretKeySpec)

    // Sign the data
    val signatureBytes = hmac.doFinal(encodedData.toByteArray())
    val signature = Base64.getUrlEncoder().withoutPadding().encodeToString(signatureBytes)

    // Generate the crypto portion of the final secure link
    return SecureLinkData(encodedData, signature)
}
