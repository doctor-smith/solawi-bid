package org.solyton.solawi.bid.module.i18n.api

import io.ktor.client.*
import io.ktor.client.engine.js.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import org.solyton.solawi.bid.module.i18n.data.Environment

suspend fun Environment.i18n(locale: String): String =
    with(HttpClient(Js)) {
        get("${i18nResources.url}/$locale"){
            port = i18nResources.port
        }.bodyAsText()
    }


suspend fun Environment.i18n(): Any =
    with(HttpClient(Js)) {
        get("${i18nResources.url}/"){
            port = i18nResources.port
        }.bodyAsText()
    }

suspend fun Environment.localizeResource(resource: String): String =
    with(HttpClient(Js)) {
        get("${i18nResources.url}/$resource") {
            port = i18nResources.port
        }.bodyAsText()
    }