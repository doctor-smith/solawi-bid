package org.solyton.solawi.bid.module.application.i18n


fun String.camelCase() = split("_").mapIndexed { index, s ->
    if(index==0) s.lowercase() else s.first().uppercase() + s.drop(1).lowercase()
}.joinToString("")
