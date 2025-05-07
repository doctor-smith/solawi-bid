package org.solyton.solawi.bid.module.error.lang

import org.evoleq.language.Lang
import org.evoleq.language.texts

fun errorModalTexts(message: String):Lang.Block = texts{
    key = "error"
    variable {
        key = "title"
        value = "Error"
    }
    block{
        key = "okButton"
        variable {
            key = "title"
            value = "Ok"
        }

    }
    block{
        key = "cancelButton"
        variable {
            key = "title"
            value = "Cancel"
        }
    }
    block {
        key = "content"
        variable {
            key = "message"
            value = message
        }
    }
}

val operationNotImplementedTexts by lazy {
    errorModalTexts("Operation not implemented")
}