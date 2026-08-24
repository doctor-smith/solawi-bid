package org.solyton.solawi.bid.application.ui.page.user.data

import org.evoleq.axioms.definition.Lensify
import org.evoleq.axioms.definition.ReadWrite
import org.solyton.solawi.bid.application.ui.page.user.data.filter.UserFilterQuery
import org.solyton.solawi.bid.application.ui.page.user.data.sort.UserSortOrder


@Lensify
data class UiState(
    @ReadWrite val filter: UserFilterQuery = UserFilterQuery(),
    @ReadWrite val sortOrder: UserSortOrder = UserSortOrder(),
    @ReadWrite val pageSize: Int = 20,
    @ReadWrite val pageOffset: Long = 0L
)
