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

data class IO(
    val module: String,
    val inputs: Set<String>,
    val outputs: Set<String>,
)


data class Node(
    val source: String,
    val targets: List<String>
)

data class Nodes(
    val nodes: List<Node>
)

data class Path(
    val nodes: List<Node>
)

data class Paths(
    val list: List<Path>
)
