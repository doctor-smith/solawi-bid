package org.solyton.solawi.bid.module.bid.data.validation

sealed class ValidationException(override val message: String): Exception(message) {
    sealed class AuctionDetailsSolawiTuebingen(override val message: String): ValidationException(message) {
        data object ValueOutOfRange : AuctionDetailsSolawiTuebingen("Value out of range")
    }
}