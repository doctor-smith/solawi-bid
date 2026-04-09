package org.solyton.solawi.bid.module.banking.service

import org.solyton.solawi.bid.module.banking.data.MandateReference
import org.solyton.solawi.bid.module.banking.data.MandateReferencePrefix
import kotlin.random.Random

fun MandateReferencePrefix.generateReference(
    padStart: Int = 4,
    randomSize: Int= 8,
    number: Int
): MandateReference {
    val random = generateRandomString(randomSize)
    val padded = "$number".padStart(padStart, '0')
    return MandateReference("$value-$random-$padded")
}

// GEMUESE-ANTEIL-2627-0001-rrrandom

private fun generateRandomString(length: Int): String {
    val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    return (1..length)
        .map { chars[Random.nextInt(chars.length)] }
        .joinToString("")
}
