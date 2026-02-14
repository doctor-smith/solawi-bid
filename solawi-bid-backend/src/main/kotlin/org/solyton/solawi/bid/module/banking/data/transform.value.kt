package org.solyton.solawi.bid.module.banking.data

import org.solyton.solawi.bid.module.values.AccessorId
import java.util.*

fun BankAccountId.toUUID(): UUID = UUID.fromString(value)
fun UUID.toBankAccountId() = BankAccountId(toString())

fun AccessorId.toUUID(): UUID = UUID.fromString(value)
fun UUID.toAccessorId() = AccessorId(toString())
