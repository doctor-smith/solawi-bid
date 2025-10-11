package org.solyton.solawi.bid.module.style.data

sealed class Side {
    data object Left : Side()
    data object Right : Side()
}
