package org.evoleq.math.cat.gradle.optics

sealed class Modifier {
    object ReadWrite : Modifier() { override fun toString(): String = "@ReadWrite" }
    object ReadOnly : Modifier(){ override fun toString(): String = "@ReadOnly" }
    object Ignore : Modifier() { override fun toString(): String = "@Ignore" }

    companion object {
        fun from(value: String): Modifier = when {
            value.endsWith("ReadOnly") -> ReadOnly
            value.endsWith("ReadWrite") -> ReadWrite
            else -> Ignore
        }
    }
}



sealed class Optic {
    object Lens : Optic() { override fun toString(): String = "@Lensify" }

    object Prism : Optic() { override fun toString(): String = "@Prismify" }
}

data class Type(val name: String)

data class ClassDescriptor(
    val targetPackage: String,
    val name: String,
    val properties: List<PropertyDescriptor>,
    val optic: Optic = Optic.Lens,
)

data class PropertyDescriptor(
    val name: String,
    val type: String,
    val modifier: Modifier,
    val default: String?
)


data class LensDescriptor(
    val type: String,
    val modifier: Modifier,
    val focusName: String,
    val focusType: String,
)
