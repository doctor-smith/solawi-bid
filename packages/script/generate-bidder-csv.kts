import java.io.File

// TODO("Set variable")
val targetFilename:String = TODO("Set variable")
val numberOfBidders: Int = TODO("Set variable")

fun generateBidderCsv(
    number: Int,
    targetFile: String
) {
    val contentData: String = (0..number).toList().joinToString("\n") { n ->
        "dev$n@provider.com;1"
    }

    val content = "Email;Anteile\n"+contentData
    File(targetFile).writeText(content)
}

generateBidderCsv(
    numberOfBidders,
    targetFilename
)
