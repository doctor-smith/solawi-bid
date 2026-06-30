package org.solyton.solawil.bid.module.banking.data

import org.solyton.solawi.bid.module.banking.data.SepaCollectionId
import org.solyton.solawi.bid.module.banking.data.SepaMandateId
import org.solyton.solawi.bid.module.banking.data.SepaPaymentId
import java.util.*

fun SepaPaymentId.toUUID(): UUID = UUID.fromString(value)
fun SepaPaymentId.Companion.fromUUID(uuid: UUID): SepaPaymentId = SepaPaymentId(uuid.toString())

fun SepaCollectionId.toUUID(): UUID = UUID.fromString(value)
fun SepaCollectionId.Companion.fromUUID(uuid: UUID): SepaCollectionId = SepaCollectionId(uuid.toString())

fun SepaMandateId.toUUID(): UUID = UUID.fromString(value)
fun SepaMandateId.Companion.fromUUID(uuid: UUID): SepaMandateId = SepaMandateId(uuid.toString())

