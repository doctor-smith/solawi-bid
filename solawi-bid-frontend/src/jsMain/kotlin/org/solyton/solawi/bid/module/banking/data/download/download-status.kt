package org.solyton.solawi.bid.module.banking.data.download

sealed class Download {
    data object None : Download()
    data object Start : Download()
    data object Done : Download()
}
