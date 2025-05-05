package org.evoleq.compose.dnd

import androidx.compose.runtime.*
import org.evoleq.compose.Markup
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text
import org.w3c.files.File
import org.w3c.files.FileReader
import org.w3c.files.get

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
                isDragging = true
            }
            onDragLeave {
                it.preventDefault()
                isDragging = false
            }
            onDrop { event ->
                event.preventDefault()
                if(isProcessing) return@onDrop
                isDragging = false
                isProcessing = true
                onProcessingStarted()
                val files = event.dataTransfer?.files
                if (files != null) {
                    val fileList = (0 until files.length).map { files[it]!! }
                    console.log(fileList.map { it.name })
                    onFilesDropped(fileList)
                }
                onProcessingStopped()
                isProcessing = false
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
        console.error("Error reading file: ${file.name}")
    }
    reader.readAsText(file) // You can also use `readAsArrayBuffer` or `readAsDataURL`
}