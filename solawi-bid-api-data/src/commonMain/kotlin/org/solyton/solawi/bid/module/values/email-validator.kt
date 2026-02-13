package org.solyton.solawi.bid.module.values

fun isValidEmail(email: String): Boolean = email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"))
