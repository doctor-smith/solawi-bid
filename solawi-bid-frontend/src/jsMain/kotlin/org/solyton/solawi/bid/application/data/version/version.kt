package org.solyton.solawi.bid.application.data.version

import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.evoleq.axioms.definition.Lensify
import org.evoleq.axioms.definition.ReadWrite


@Serializable
@Lensify
data class VersionInfo(
    @ReadWrite val commit: String = "",
    @ReadWrite val version: String = ""
)


private val json by lazy {
    Json { ignoreUnknownKeys = true }
}
suspend fun getVersionInfo(): VersionInfo {
    val response = window.fetch("/version.json").await()
    return json.decodeFromString<VersionInfo>(response.text().await())
}
