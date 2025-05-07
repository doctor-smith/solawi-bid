package org.solyton.solawi.bid.module.localstorage.api

import kotlinx.browser.localStorage
import org.w3c.dom.get

fun write(key: String, value: String) {
    localStorage.setItem(key, value)
}

fun read(key: String): String? = localStorage[key]