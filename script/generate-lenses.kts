/**
 * Generate Lenses from a given data class. Copy the output printed to the console and use it in your class - file.
 */

val input = "@Lensify data class DragEvent(\n" +
        "    @ReadWrite val coordinates: Coordinates,\n" +
        "    @ReadWrite val slice: Int,\n" +
        "    @ReadWrite val target: Int? = null,\n" +
        "    @Ignore val drop: (slice: Int)->Unit = { _ -> Unit}\n" +
        ")"

/*
val input = """|@Lensify data class HanoiCheatPage(
    |   @ReadOnly val texts: Lang.Block,
    |   @ReadWrite val cheat: HanoiCheat,
    |   @ReadWrite val numberOfSlices: Int,
    |   @ReadWrite val numberOfMoves: Int,
    |   @ReadWrite val moves: Moves,
    |   @ReadWrite val isComputingMoves: Boolean,
    |   @ReadWrite val indexOfCurrentMove: Int,
    |   @ReadWrite val error: String?,
    |)
""".trimMargin()
*/

// Types in play
data class Type(val name: String)
sealed class Modifier {
    object ReadWrite : Modifier() { override fun toString(): String = "@ReadWrite" }
    object ReadOnly : Modifier(){ override fun toString(): String = "@ReadOnly" }
    object Ignore : Modifier() { override fun toString(): String = "@Ignore" }
}


val modifiers = listOf(
    "${Modifier.ReadWrite}",
    "${Modifier.ReadOnly}",
    "${Modifier.Ignore}"
)

// Contruct Modifiers
fun Modifier(name: String): Modifier {
    val n = name
    return when {
        n == Modifier.ReadOnly.toString() -> Modifier.ReadOnly
        n == Modifier.ReadWrite.toString() -> Modifier.ReadWrite
        else -> Modifier.Ignore
    }

}
data class LensDescriptor(
    val type: String,
    val modifier: Modifier,
    val focusName: String,
    val focusType: String,
)

// create read only lens
fun readOnlylens(lensDescriptor: LensDescriptor): String = """val ${lensDescriptor.focusName}: Lens<${lensDescriptor.type}, ${lensDescriptor.focusType}> by lazy{ Lens(
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
    | @ReadWrite $this
""".trimMargin()

fun String.commentReadOnlyLens(lensDescriptor: LensDescriptor) = """
    |/**
    | * Autogenerated ReadOnly Lens.
    | * Read [${lensDescriptor.type}.${lensDescriptor.focusName}]
    | */
    | @ReadOnly $this
""".trimMargin()

// Go generate lenses :-)
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

val members: List<String> = input1.drop(1).filter {item ->
    modifiers.map { item.startsWith(it) }.reduce{x,y -> x || y}
}.filter{
    item -> !item.startsWith("${Modifier.Ignore}")
}.map{ item: String ->
        val spilt = item.split(" ")
        LensDescriptor(
            type = typeOfTheWhole.name,
            modifier = Modifier(spilt[0]),
            focusName = spilt[1],
            focusType = spilt[2]
        )
}.map{
    lensDescriptor -> when(lensDescriptor.modifier) {
        is Modifier.Ignore -> "ignore"
        is Modifier.ReadOnly -> readOnlylens(lensDescriptor).commentReadOnlyLens(lensDescriptor)
        is Modifier.ReadWrite -> readWriteLens(lensDescriptor).commentReadWriteLens(lensDescriptor)
    }
}
println (members.joinToString("\n\n"))




