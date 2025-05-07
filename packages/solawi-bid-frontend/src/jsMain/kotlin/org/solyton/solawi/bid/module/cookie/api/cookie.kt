package org.solyton.solawi.bid.module.cookie.api

import io.ktor.http.*
import kotlinx.browser.document
import org.evoleq.parser.*

fun readCookie(): Cookie? {
    val parser: Parser<Cookie> = StartsWith("solawi-bid.cookie_consent=true") map {
        Cookie("solawi-bid.cookie_consent", "true")
    }
    return document.cookie
        .split(";")
        .mapNotNull { parser.run(it.trim()).result }
        .firstOrNull()
    }


fun writeCookie() {
    document.cookie = with(Cookie("solawi-bid.cookie_consent", "true")){
        "$name=$value"
    }
}

fun readLang(): String? {
    val parser: Parser<String> = seqA(
        StartsWith("solawi-bid.lang"),
        Between('=',';')
    ) map {
        it[1].trim()
    }
    return document.cookie
        .split(";")
        .mapNotNull { parser.run(it.trim()+";").result }
        .firstOrNull()
}

fun writeLang(locale: String) {
    document.cookie="solawi-bid.lang=$locale; SameSite=None; Secure"
}