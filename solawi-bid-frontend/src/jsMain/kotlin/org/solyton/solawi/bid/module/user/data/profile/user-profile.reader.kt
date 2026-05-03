package org.solyton.solawi.bid.module.user.data.profile

import org.evoleq.math.Reader

val FullName: Reader<UserProfile, String> = Reader { with(it) {"$firstname $lastname" }}
val FullNameOrBlank: Reader<UserProfile?, String> = Reader { it?.let { with(it) {"$firstname $lastname" } } ?: "" }
