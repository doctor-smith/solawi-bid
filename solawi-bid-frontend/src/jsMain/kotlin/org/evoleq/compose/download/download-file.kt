package org.evoleq.compose.download

import kotlinx.browser.document
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag

/**
 *
 */
fun downloadCsv(csvData: String, fileName: String) {
    // Create a Blob from the CSV string
    val blob = Blob(arrayOf(csvData), BlobPropertyBag(type = "text/csv"))

    // Create an object URL
    val url = URL.createObjectURL(blob)

    // Create an anchor element and trigger download
    val anchor = document.createElement("a") as HTMLAnchorElement
    anchor.href = url
    anchor.download = fileName
    document.body?.appendChild(anchor)
    anchor.click()

    // Cleanup: remove the anchor and revoke the object URL
    document.body?.removeChild(anchor)
    URL.revokeObjectURL(url)
}
