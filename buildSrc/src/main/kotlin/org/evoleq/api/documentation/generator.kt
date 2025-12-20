package org.evoleq.api.documentation

import java.io.File

data class Line(
    val group: String,
    val url: String,
    val httpMethod: String,
    val requestType: String,
    val responseType: String,
    val key: String
)

object UniversalDocGenerator {
    @JvmStatic
    fun main(args: Array<String>) {
        val apiPath = args[0]
        val outputPath = args[1]

        try {
            val lastDot = apiPath.lastIndexOf('.')
            val className = apiPath.substring(0, lastDot)
            val methodName = apiPath.substring(lastDot + 1)

            val clazz = Class.forName(className)
            val method = clazz.getMethod(methodName)
            val apiObj = method.invoke(null) ?: throw Exception("API Object null")

            val endPointsField = apiObj.javaClass.getDeclaredField("endPoints")
            endPointsField.isAccessible = true
            val endPoints = endPointsField.get(apiObj) as Map<*, *>

            val sortedEntries = endPoints.entries.sortedBy { (_, ep) ->
                val url = ep!!.javaClass.getMethod("getUrl").invoke(ep).toString()
                val group = ep!!.javaClass.getMethod("getGroup").invoke(ep)?.toString()
                if(group == null) {
                    "z_unclassified/$url"
                } else {
                    "$group/$url"
                }
            }

            fun StringBuilder.tableHead() {
                appendLine("| Methode | URL | Key | Request Type | Response Type |")
                appendLine("| :--- | :--- | :--- | :--- | :--- |")
            }

            val markdown = buildString {
                appendLine("# API Documentation")

                sortedEntries.map { (key, ep) ->
                    val url = ep!!.javaClass.getMethod("getUrl").invoke(ep).toString()
                    val group = ep.javaClass.getMethod("getGroup").invoke(ep).toString()
                    val httpMethod = ep.javaClass.simpleName.uppercase()

                    // Auslesen der Typnamen über die gespeicherten KClasses
                    val sName = getKClassName(ep, "getRequestType")
                    val tName = getKClassName(ep, "getResponseType")
                    val keyName = key?.let { getKClassNameFromObj(it) } ?: "Any"


                    // appendLine("| **$httpMethod** | `$url` | `$sName` | `$tName` | `$keyName` |")

                    Line(group, url, httpMethod, sName, tName, keyName)

                }.groupBy { it.group }.forEach { (group, lines) ->
                    appendLine("## $group")
                    appendLine()
                    tableHead()
                    lines.forEach { line ->
                        appendLine("| ${line.httpMethod} | ${line.url} | ${line.key}  | ${line.requestType} | ${line.responseType} |")
                    }
                }

            }

            File(outputPath).apply {
                parentFile.mkdirs()
                writeText(markdown)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            System.exit(1)
        }
    }

    private fun getKClassName(obj: Any, methodName: String): String {
        return try {
            val kClass = obj.javaClass.getMethod(methodName).invoke(obj)
            getKClassNameFromObj(kClass)
        } catch (e: Exception) { "Any" }
    }

    private fun getKClassNameFromObj(kClass: Any): String {
        return try {
            kClass.javaClass.getMethod("getSimpleName").invoke(kClass) as String
        } catch (e: Exception) { "Any" }
    }
}