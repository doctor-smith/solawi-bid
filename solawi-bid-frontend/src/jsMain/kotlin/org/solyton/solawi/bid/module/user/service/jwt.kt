package org.solyton.solawi.bid.module.user.service

import kotlinx.browser.window
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

fun decodeBase64Url(base64Url: String): String {
    val base64 = base64Url
        .replace('-', '+')
        .replace('_', '/')
        .let {
            when (it.length % 4) {
                2 -> "$it=="
                3 -> "$it="
                else -> it
            }
        }

    return window.atob(base64)
}

fun getSubjectFromJwt(token: String): String? {
    val parts = token.split(".")
    require(parts.size == 3)
    
    val payloadJson = decodeBase64Url(parts[1])
    val jsonElement = Json.parseToJsonElement(payloadJson)
    return jsonElement.jsonObject["sub"]?.jsonPrimitive?.content
}
