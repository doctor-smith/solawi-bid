package org.solyton.solawi.bid.application.ui.page.user.data.filter

import org.evoleq.axioms.definition.Lensify
import org.evoleq.axioms.definition.ReadWrite
import org.evoleq.iql.dsl.FilterBuilder


@Lensify
data class UserFilterQuery(
    @ReadWrite val username: String? = null,
    @ReadWrite val status: String? = null,
    @ReadWrite val firstName: String? = null,
    @ReadWrite val lastName : String? = null,
) {
    fun isNotNull() = listOfNotNull(username, status, firstName, lastName).isNotEmpty()

    fun asQuery(): (FilterBuilder.() -> Unit)? = when {
        isNotNull() -> {
            {
                username?.let {
                    p("user.username") contains it
                }
                status?.let { p("user.status") containsIgnoreCase   it }
                firstName?.let {
                    any("userProfiles") {
                        p("userProfile.first_name") containsIgnoreCase   it
                    }
                }
                lastName?.let {
                    any("userProfiles") {
                        p("userProfile.last_name") containsIgnoreCase  it
                    }
                }
            }
        }

        else -> null
    }
}
