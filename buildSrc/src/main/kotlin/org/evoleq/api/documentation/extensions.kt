package org.evoleq.api.documentation

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property

interface ApiDocExtension {
    /**
     * Fully qualified path to the API instance getter.
     * Example: "org.solyton.solawi.bid.application.api.Solawi_apiKt.getSolawiApi"
     */
    val apiPath: Property<String>

    /**
     * target file for the generated documentation.
     */
    val outputFile: RegularFileProperty
}