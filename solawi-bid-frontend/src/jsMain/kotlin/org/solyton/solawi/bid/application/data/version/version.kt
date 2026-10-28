package org.solyton.solawi.bid.application.data.version

import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


@Serializable
data class VersionInfo(
    val commit: String,
    val version: String
)

suspend fun getVersionInfo(): VersionInfo {
    val response = window.fetch("/version.json").await()
    return Json.decodeFromString<VersionInfo>(response.text().await())
}
