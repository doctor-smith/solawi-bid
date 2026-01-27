package org.evoleq.ktorx.client

actual fun encode(value: String): String =
    js("encodeURIComponent(value)") as String
