// This file has been partially auto-generated.
// Please don't make any changes to the lenses.
// Feel free to add or remove annotated properties from
// the generator data class. The corresponding lenses 
// will be removed or added on the next run of the 
// lens generator. See below for more details.
package org.solyton.solawi.bid.module.navbar.data.navbar

import org.evoleq.axioms.definition.Lensify
import org.evoleq.axioms.definition.ReadOnly
import org.evoleq.axioms.definition.ReadWrite
import org.evoleq.compose.modal.Modals
import org.evoleq.device.data.Device
import org.evoleq.optics.storage.ActionDispatcher
import org.solyton.solawi.bid.module.i18n.data.I18N
import org.solyton.solawi.bid.module.navbar.data.environment.Environment
import org.solyton.solawi.bid.module.navbar.data.user.User

@Lensify
data class NavBar(
    @ReadOnly val actions: ActionDispatcher<NavBar> = ActionDispatcher {  },
    @ReadWrite val modals: Modals<Int>,
    @ReadOnly val deviceData: Device,
    @ReadWrite val i18n: I18N,
    @ReadOnly val environment: Environment,
    @ReadWrite val user: User,
)
