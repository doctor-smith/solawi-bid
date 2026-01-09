// This file has been partially auto generated. 
// Please don't make any changes to the lenses.
// Feel free to add or remove annotated properties from
// the generator data class. The corresponding lenses 
// will be removed or added on the next run of the 
// lens generator. See below for more details.
package org.solyton.solawi.bid.module.user.data.user

import org.evoleq.axioms.definition.ReadWrite
import org.evoleq.axioms.definition.Lensify
import org.evoleq.axioms.definition.ReadOnly
import org.solyton.solawi.bid.module.permissions.data.Permissions
import org.solyton.solawi.bid.module.user.data.organization.Organization
import org.solyton.solawi.bid.module.user.data.profile.UserProfile

/**
 * Generator class.
 * Feel free to add or remove annotated properties from
 * the class. Make sure that they are annotated with
 * - @ReadOnly
 * - @ReadWrite
 * If you want that a property-lens will be generated
 * on the next run of the lens generator.
 * If not, just omit the annotation or annotate it with @Ignore.
 */
@Lensify data class User(
    @ReadWrite val username: String = "",
    @ReadWrite val password: String = "",
    @ReadWrite val accessToken: String = "",
    @ReadWrite val refreshToken: String = "",
    @ReadWrite val profile: UserProfile? = null,
    @ReadOnly val permissions: Permissions = Permissions(),
    @ReadWrite val organizations: List<Organization> = listOf()
)
