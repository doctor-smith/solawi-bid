const val CHECK_TRUE = "☑\uFE0F"
const val CHECK_FALSE ="❌"

fun Boolean.checkIcon(): String = if (this) CHECK_TRUE else CHECK_FALSE

fun String.boolean(icon: String = CHECK_TRUE): Boolean = this == icon