package org.solyton.solawi.bid.test.base

import org.jetbrains.compose.web.testutils.ComposeWebExperimentalTestsApi
import org.jetbrains.compose.web.testutils.TestScope
import org.jetbrains.compose.web.testutils.runTest

@OptIn(ComposeWebExperimentalTestsApi::class)
fun runComposeTest(block: suspend TestScope.() -> Unit): dynamic = runTest {
    try {
        block()
    } finally {

        // Ganz wichtig: DOM zwischen Tests zurücksetzen,
        // damit sich nichts über die Suite aufsummiert.
        root.innerHTML = ""
    }
}
