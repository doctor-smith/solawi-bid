package org.solyton.solawi.bid.module.banking.exception

sealed class FiscalYearException(override val message: String): Exception(message) {
    data class NoSuchFiscalYear(val id: String): FiscalYearException("No such fiscal year $id")
    data object StartAfterEnd : FiscalYearException("Start date after end date") {
        @Suppress("UnusedPrivateMember")
        private fun readResolve(): Any = StartAfterEnd
    }
    data object Overlaps : FiscalYearException("Overlaps") {
        @Suppress("UnusedPrivateMember")
        private fun readResolve(): Any = Overlaps
    }
}
