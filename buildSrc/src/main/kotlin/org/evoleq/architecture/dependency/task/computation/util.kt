package org.evoleq.architecture.dependency.task.computation

import java.io.File


fun readImportsFromFile(filePath: String): List<String> {
    val file = File(filePath)
    return readImportsFromFile(file)
}
fun readImportsFromFile(file: File): List<String> {

    return file.readLines()
        .filter { it.trim().startsWith("import ") }
        .map { it.replace("import ", "") }
}

fun getAllKtFilesInPackage(packagePath: String): List<File> {
    val dir = File(packagePath)
    if (!dir.exists() || !dir.isDirectory) {
        error("Invalid package path: $packagePath")
    }

    return dir.walkTopDown()
        .filter { it.isFile && it.extension == "kt" }
        .toList()
}
