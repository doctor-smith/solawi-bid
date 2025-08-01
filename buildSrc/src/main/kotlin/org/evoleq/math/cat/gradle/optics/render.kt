package org.evoleq.math.cat.gradle.optics

import java.io.File

val defaultImports by lazy { listOf(
    "import org.evoleq.optics.Lensify",
    "import org.evoleq.optics.lens.Lens"
) }

val modifierImports by lazy {  listOf(
    "import org.evoleq.optics.ReadOnly",
    "import org.evoleq.optics.ReadWrite",
    "import org.evoleq.optics.Ignore",
)}



fun ClassDescriptor.writeToFile(root: String) {

    val imports = renderImports()
    val clazz = renderClass("", "    ")
    val optics = renderOptics()

    val result = """
        |// This file has been partially auto generated. 
        |// Please don't make any changes to the lenses.
        |// Feel free to add or remove annotated properties from
        |// the generator data class. The corresponding lenses 
        |// will be removed or added on the next run of the 
        |// lens generator. See below for more details.
        |package $targetPackage
        |
        |$imports
        |
        |$clazz
        |
        |$optics
        |
    """.trimMargin()

    val file = File(root+"/"+targetPackage.replace(".", "/")+"/"+ name + ".kt")

    file.writeText(result)
}

fun ClassDescriptor.renderImports(): String {
    val result: List<String> = listOf(
        *defaultImports.toTypedArray(),
        *modifierImports.filter {importStatement ->
            properties.map { m -> m.modifier.toString().drop(1) }.any{importStatement.endsWith(it)}
        }.toTypedArray()
    )
    return result.joinToString("\n") { it }
}

fun ClassDescriptor.renderClass(offset: String, indent: String): String = """
    |${renderComment()}
    |${optic} data class $name (
    |${properties.joinToString(",\n") { it.renderProperty(offset, indent)}}
    |)
""".trimMargin()

fun PropertyDescriptor.renderProperty(offset: String, indent: String): String = """
    |$offset$indent$modifier val $name: $type${ if( default != null ){" = $default" } else{ "" }}
""".trimMargin()

fun ClassDescriptor.renderComment() = """
    |/**
    | * Generator class.
    | * Feel free to add or remove annotated properties from
    | * the class. Make sure that they are annotated with
    | * - @ReadOnly
    | * - @ReadWrite
    | *
    | * if you want that a property-lens will be generated
    | * on the next run of the lens generator.
    | * If not, just omit the annotation or annotate it with @Ignore.
    | */
""".trimMargin()





// create read only lens
fun readOnlyLens(lensDescriptor: LensDescriptor): String = """val ${lensDescriptor.focusName}: Lens<${lensDescriptor.type}, ${lensDescriptor.focusType}> by lazy{ Lens(
    |    get = {whole -> whole.${lensDescriptor.focusName}},
    |    set = {{it}}
    |) }""".trimMargin()
// create read write lens
fun readWriteLens(lensDescriptor: LensDescriptor): String = """val ${lensDescriptor.focusName}: Lens<${lensDescriptor.type}, ${lensDescriptor.focusType}> by lazy{ Lens(
    |    get = {whole -> whole.${lensDescriptor.focusName}},
    |    set = {part -> {whole -> whole.copy(${lensDescriptor.focusName} = part)}}
    |) }""".trimMargin()

fun String.commentReadWriteLens(lensDescriptor: LensDescriptor) = """
    |/**
    | * Autogenerated Lens.
    | * Read and manipulate [${lensDescriptor.type}.${lensDescriptor.focusName}]
    | */
    |@ReadWrite $this
""".trimMargin()

fun pseudoLens(lensDescriptor: LensDescriptor): String = with(lensDescriptor){"""fun $type.$focusName(set: $focusType.()->$focusType ): $type = copy($focusName = set($focusName))""" }

fun String.pseudoLensComment(lensDescriptor: LensDescriptor) = """
    |/**
    | * Autogenerated Setter of a Pseudo Lens
    | * Manipulate [${lensDescriptor.type}.${lensDescriptor.focusName}]
    | */
    |@ReadWrite $this 
""".trimMargin()

fun String.commentReadOnlyLens(lensDescriptor: LensDescriptor) = """
    |/**
    | * Autogenerated ReadOnly Lens.
    | * Read [${lensDescriptor.type}.${lensDescriptor.focusName}]
    | */
    |@ReadOnly $this
""".trimMargin()

fun ClassDescriptor.renderOptics(): String  {
    val typeOfTheWhole = Type(name)

    val members: List<String> =
        properties.filterNot{
            item -> item.name.startsWith("${Modifier.Ignore}")
    }.map{ item: PropertyDescriptor ->

        LensDescriptor(
            type = typeOfTheWhole.name,
            modifier = item.modifier,
            focusName = item.name,
            focusType = item.type
        )
    }.map{
            lensDescriptor -> when(lensDescriptor.modifier) {
        is Modifier.Ignore -> ""
        is Modifier.ReadOnly -> readOnlyLens(lensDescriptor).commentReadOnlyLens(lensDescriptor)
        is Modifier.ReadWrite -> readWriteLens(lensDescriptor).commentReadWriteLens(lensDescriptor)
    }
    }
    return members.joinToString("\n") { it }
}
