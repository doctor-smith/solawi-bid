package org.solyton.solawi.bid.application.ui.page.user.data.sort

import org.evoleq.axioms.definition.Lensify
import org.evoleq.axioms.definition.ReadWrite
import org.evoleq.iql.dsl.QueryBuilder
import org.evoleq.iql.dsl.map
import org.evoleq.iql.dsl.min
import org.evoleq.iql.dsl.relation
import org.solyton.solawi.bid.module.list.component.SortOrder


@Lensify
data class UserSortOrder(
    @ReadWrite val username: SortOrder = SortOrder.ASC,
    @ReadWrite val status: SortOrder = SortOrder.NONE,

    @ReadWrite val firstName: SortOrder = SortOrder.NONE,
    @ReadWrite val lastName: SortOrder = SortOrder.NONE,

) {
    fun asQuerySortOrder(): QueryBuilder.()->Unit = {
        when(username){
            SortOrder.ASC -> asc("user.username")
            SortOrder.DESC -> desc("user.username")
            SortOrder.NONE -> {}
        }
        when(status){
            SortOrder.ASC -> asc("user.status")
            SortOrder.DESC -> desc("user.status")
            SortOrder.NONE -> {}
        }
        /*
        when(firstName){
            SortOrder.ASC -> asc("userProfile.first_name")
            SortOrder.DESC -> desc("userProfile.first_name")
            SortOrder.NONE -> {}
        }
        */
        when(lastName) {
            SortOrder.ASC -> asc(relation("userProfiles")
                .map("last_name").min()
            )
            SortOrder.DESC -> desc(relation("userProfiles")
                .map("last_name").min()
            )
            SortOrder.NONE -> {}
        }
    }
}
