package org.evoleq.math.cat.gradle.optics

import org.gradle.api.Project
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JTextField

fun Project.showGenerateOptionsFromExistingSourcesDialog(extension: OpticsExtension) {
    // Create Swing components
    val panel = JPanel()
    val packageField = JTextField(20)
    packageField.text = extension.defaultPackage
    val fileField = JTextField(20)

    // Add labels and fields to the panel
    panel.add(JLabel("Package name (e.g., com.example):"))
    panel.add(packageField)
    panel.add(JLabel("File name (without extension):"))
    panel.add(fileField)

    // Show the dialog
    val result = JOptionPane.showConfirmDialog(
        null,
        panel,
        "Enter Details",
        JOptionPane.OK_CANCEL_OPTION,
        JOptionPane.PLAIN_MESSAGE
    )

    // Check if user clicked "OK"
    if (result == JOptionPane.OK_OPTION) {
        val packageInput = packageField.text.trim()
        val fileName = fileField.text.trim()

        // Validate inputs
        if (packageInput.isEmpty() || fileName.isEmpty()) {
            println("Package name and file name cannot be empty!")
            return
        }

        // Convert package name to directory path (e.g., com.example to com/example)
        val packagePath = packageInput.replace('.', '/')
        val filePath = projectDir.resolve("src/${extension.sourceSet}/kotlin/$packagePath/$fileName.kt")

        // Create directories if they don't exist
        // NO!!! filePath.parentFile.mkdirs()

        val lines = filePath.readLines().map{it.trim()}.filterNot{
                it.isEmpty() ||
                it.startsWith("*") ||
                it.startsWith("//") ||
                it.startsWith("/*")
        }

        // validate
        val numberOfClasses = lines.filter { it.containsStringsInOrderWithWhitespace("data", "class") }
        if(numberOfClasses.size > 1) {

            throw Exception("Too many classes in file")
        }

        val packageLine: String = lines.first { it.startsWith("package") }
        val imports: List<String> = listOf(
            *lines.filter { it.startsWith("import") }.toTypedArray(),
            "import org.evoleq.optics.lens.Lens"
        ).distinct().sortedWith(
            compareBy(
                { it.count { char -> char == '.' } },
                { it }
            )
        )

        // index where the data class closes + 1
        val index = lines.indexOfFirst { it.startsWith(")") } + 1
        val dataClass: List<String> = lines.take(index).filter { it !in imports && it != packageLine }

        val clazz = """// This file has been partially auto generated. 
            |// Please don't make any changes to the lenses.
            |// Feel free to add or remove annotated properties from
            |// the generator data class. The corresponding lenses 
            |// will be removed or added on the next run of the 
            |// lens generator. See below for more details.
            |$packageLine
            |
            |${imports.joinToString("\n") { it }}
            |
            |/**
            | * Generator class.
            | * Feel free to add or remove annotated properties from
            | * the class. Make sure that they are annotated with
            | * - @ReadOnly
            | * - @ReadWrite
            | * If you want that a property-lens will be generated
            | * on the next run of the lens generator.
            | * If not, just omit the annotation or annotate it with @Ignore.
            | */
            |${dataClass.first()}
            |${dataClass.drop(1).dropLast(1).joinToString("\n    ","    ") { it }}
            |${dataClass.last()}
            |
            |${dataClass.renderOptics()}
            |
        """.trimMargin()

        // Create the new file and add a simple class template
        filePath.writeText("")
        filePath.writeText(clazz)

        println("File created at: ${filePath.path}")
    } else {
        println("Operation cancelled.")
    }

}
fun String.containsStringsInOrderWithWhitespace(vararg searchStrings: String): Boolean {
    // Build a regex pattern like: "string1\\s+string2\\s+string3"
    val pattern = searchStrings.joinToString("\\s+") { Regex.escape(it) }.toRegex()

    // Check if the pattern matches anywhere in the string `s`
    return pattern.containsMatchIn(this)
}
fun List<String>.renderOptics(): String = parse().map {  lensDescriptor -> when(lensDescriptor.modifier) {
    is Modifier.Ignore -> ""
    is Modifier.ReadOnly -> readOnlyLens(lensDescriptor).commentReadOnlyLens(lensDescriptor)
    is Modifier.ReadWrite -> readWriteLens(lensDescriptor).commentReadWriteLens(lensDescriptor)+ "\n"+ pseudoLens(lensDescriptor).pseudoLensComment(lensDescriptor)
}}.joinToString("\n") { it }


fun List<String>.parse(): List<LensDescriptor> {
    val input = joinToString("\n") { it }

    val input1 = input
        .replace(",", "")
        .replace("(", "")
        .replace(")", "").split("\n")
        .map{item -> item.trim()}
        .map{ item -> item
            .replace(":" , " ")
            .replace ("val", "")
            .replace("  ", " ")
            .replace ("data class", "")
            .replace("  ", " ")
            .trim()
        }

// input1.first ~ type
    val typeOfTheWhole = Type(input1.first().split(" ") [1])
    val members: List<LensDescriptor> = input1.drop(1).filterNot {
        it.startsWith(Modifier.Ignore.toString())
    }.filter {
        it.startsWith(Modifier.ReadWrite.toString()) ||
        it.startsWith(Modifier.ReadOnly.toString())
    }.map { item: String ->

            val spilt = item.split(" ")
            LensDescriptor(
                type = typeOfTheWhole.name,
                modifier = Modifier.from(spilt[0]),
                focusName = spilt[1],
                focusType = spilt[2]
            )

    }


    return members
}

// fun List<String>.parse(): ClassDescriptor