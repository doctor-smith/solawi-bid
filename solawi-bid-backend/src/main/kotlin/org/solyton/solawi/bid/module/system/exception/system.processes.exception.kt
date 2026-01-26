
sealed class SystemProcessException(override val message: String): Exception(message) {
    data class NoSuchProcess(val name: String): SystemProcessException(
        "No such system process $name"
    )
    data class DuplicateProcessName(val name: String): SystemProcessException(
        "Duplicate process name $name"
    )
}
