package org.evoleq.permission

import org.evoleq.value.StringValueWithDescription


object Create : StringValueWithDescription {
    override val value  = "CREATE"
    override val description: String = "General right to create something in a context"
}

object Read : StringValueWithDescription {
    override val value  = "READ"
    override val description: String = "General right to read something in a context"
}

object Update : StringValueWithDescription {
    override val value  = "UPDATE"
    override val description: String = "General right to update something in a context"
}

object Delete : StringValueWithDescription {
    override val value  = "DELETE"
    override val description: String = "General right to delete something in a context"
}
