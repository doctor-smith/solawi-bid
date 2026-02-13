package org.solyton.solawi.bid.module.values

internal val UUID_REGEX = Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
internal fun isValidUUID(uuid: String): Boolean = UUID_REGEX.matches(uuid)
