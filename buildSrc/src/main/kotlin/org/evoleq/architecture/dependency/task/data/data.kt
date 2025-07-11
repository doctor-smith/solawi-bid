package org.evoleq.architecture.dependency.task.data

import java.io.File


data class Dependency(
    val module: String,
    val dependsOn: String,
    val uses: List<UsingObj>
)

data class UsingObj(
    val file: File,
    val imports: Set<String>
)
