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
        error("Invalid package path: ${packagePath.onEmpty("EMPTY PACKAGE PATH")}")
    }

    return dir.walkTopDown()
        .filter { it.isFile && it.extension == "kt" }
        .toList()
}

fun String.cleanPath(separator: Char = '/', keepFirst: Boolean = false): String = when {
        keepFirst -> this
        else -> dropWhile { it == separator }
        }
        .dropLastWhile { it == separator }
        .replace("$separator$separator", "$separator")


fun String.asPath(): String = replace(".", "/")

fun String.asPackage(): String = replace("/", ".")

fun String.onEmpty(alternative: String): String = when{
    isEmpty() -> alternative
    else -> this
}
