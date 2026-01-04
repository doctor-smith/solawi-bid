package org.evoleq.compose.dnd

import androidx.compose.runtime.*
import org.evoleq.compose.Markup
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.get
import org.w3c.files.File
import org.w3c.files.FileReader

@Markup
@Composable
@Suppress("FunctionName")
fun Dropzone(
    onProcessingStarted: ()->Unit = {},
    onProcessingStopped: ()->Unit = {},
    onFilesDropped: (List<File>) -> Unit
) {
    var isDragging by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }

    Div(
        attrs = {
            style {
                height(70.vh)
                padding(16.px)
                border(2.px, LineStyle.Dashed, if (isDragging) Color.green else Color.gray)
                textAlign("center")
                backgroundColor(if (isDragging) Color.lightgray else Color.white)
            }
            onDragOver {
                it.preventDefault()
                it.stopPropagation()
                isDragging = true
            }
            onDragLeave {
                it.preventDefault()
                it.stopPropagation()
                it.dataTransfer?.dropEffect = "copy"
                isDragging = false
            }
            onDrop { event ->
                event.preventDefault()
                event.stopPropagation()

                if (isProcessing) return@onDrop
                isDragging = false

                val dt = event.dataTransfer ?: return@onDrop
                val fileList = mutableListOf<File>()

                console.log("Dropzone: Drop-Event gefangen. dt = ${dt.files}")

                // 1. Versuch: Über dt.files
                val files = dt.files
                if (files.length > 0) {
                    for (i in 0 until files.length) {
                        files.item(i)?.let { fileList.add(it) }
                    }
                }

                // 2. Versuch: Fallback auf dt.items (falls files leer ist)
                if (fileList.isEmpty()) {
                    val items = dt.items
                    for (i in 0 until items.length) {
                        val item = items[i]
                        if (item?.kind == "file") {
                            item.getAsFile()?.let { fileList.add(it) }
                        }
                    }
                }

                if (fileList.isNotEmpty()) {
                    isProcessing = true
                    onProcessingStarted()

                    // Wir übergeben die Liste.
                    // WICHTIG: Die Verarbeitung (FileReader) sollte sofort starten.
                    onFilesDropped(fileList)

                    isProcessing = false
                    onProcessingStopped()
                } else {
                    console.warn("Keine Dateien im Drop-Event gefunden.")
                }

            }
        }
    ) {
        // todo:i18n
        Text(if (isDragging) "Drop files here" else "Drag and drop files here or click to upload")
    }
}

fun readFileContent(file: File, onContentRead: (String) -> Unit) {
    val reader = FileReader()
    reader.onload = {
        val content = reader.result as? String
        if (content != null) {
            onContentRead(content)
        }
    }
    reader.onerror = {
        console.error("Error reading file: ${file.name}", reader.error)
    }
    reader.readAsText(file) // You can also use `readAsArrayBuffer` or `readAsDataURL`
}
