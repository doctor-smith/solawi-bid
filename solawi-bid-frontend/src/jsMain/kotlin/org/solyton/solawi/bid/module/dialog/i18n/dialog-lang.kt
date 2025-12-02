package org.solyton.solawi.bid.module.dialog.i18n

import org.evoleq.language.Lang
import org.evoleq.language.texts


fun dialogModalTexts(message: String):Lang.Block = texts{
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
