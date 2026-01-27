package org.evoleq.ktorx.client

actual fun encode(value: String): String =
    java.net.URLEncoder.encode(value, Charsets.UTF_8.name())
