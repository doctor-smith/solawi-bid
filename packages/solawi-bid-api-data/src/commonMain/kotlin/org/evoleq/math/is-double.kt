package org.evoleq.math


fun onIsDouble(value: String, dispatch: String.()->Unit) {
    if(value.isDouble()) {
        dispatch(fixDoubleString(value))
    }
}

fun fixDoubleString(value:String):String =
    when{
        value.isEmpty() -> "0.0"
        value == "." -> "0.0"
        value.startsWith(".") -> "0.$value"
        value.endsWith(".") -> "$value.0"
        else -> value
    }


val digits = listOf( "0", "1", "2", "3", "4", "5", "6", "7", "8", "9")
fun String.isDouble(precision: Int = 2, dotHit: Boolean = false): Boolean {
    return when {
        isEmpty() -> true
        dotHit && length > 2 -> false
        dotHit && contains(".") -> false
        else -> {


            val first = "${first()}"
            val rest = drop(1)
            if( digits.contains(first)) {
                return rest.isDouble(precision,dotHit)
            }
            if(first == "."){
                return rest.isDouble(precision, true)
            }
            false
        }
    }
}