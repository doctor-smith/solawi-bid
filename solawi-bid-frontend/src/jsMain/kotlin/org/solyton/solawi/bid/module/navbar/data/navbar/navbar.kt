// This file has been partially auto generated. 
// Please don't make any changes to the lenses.
// Feel free to add or remove annotated properties from
// the generator data class. The corresponding lenses 
// will be removed or added on the next run of the 
// lens generator. See below for more details.
package org.solyton.solawi.bid.module.navbar.data.navbar

import org.evoleq.optics.Lensify
import org.evoleq.optics.ReadOnly
import org.evoleq.optics.ReadWrite
import org.evoleq.compose.modal.Modals
import org.evoleq.device.data.Device
import org.evoleq.optics.lens.Lens
import org.evoleq.optics.storage.ActionDispatcher
import org.solyton.solawi.bid.module.navbar.data.environment.Environment
import org.solyton.solawi.bid.module.i18n.data.I18N
import org.solyton.solawi.bid.module.navbar.data.user.User

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
@Lensify data class NavBar(
    @ReadOnly val actions: ActionDispatcher<NavBar> = ActionDispatcher {  },
    @ReadWrite val modals: Modals<Int>,
    @ReadOnly val deviceData: Device,
    @ReadWrite val i18n: I18N,
    @ReadOnly val environment: Environment,
    @ReadWrite val user: User,
)

/**
 * Autogenerated ReadOnly Lens.
 * Read [NavBar.actions]
 */
@ReadOnly val actions: Lens<NavBar, ActionDispatcher<NavBar>> by lazy{ Lens(
    get = {whole -> whole.actions},
    set = {{it}}
) }
/**
 * Autogenerated Lens.
 * Read and manipulate [NavBar.modals]
 */
@ReadWrite val modals: Lens<NavBar, Modals<Int>> by lazy{ Lens(
    get = {whole -> whole.modals},
    set = {part -> {whole -> whole.copy(modals = part)}}
) }
/**
 * Autogenerated Setter of a Pseudo Lens
 * Manipulate [NavBar.modals]
 */
@ReadWrite fun NavBar.modals(set: Modals<Int>.()->Modals<Int> ): NavBar = copy(modals = set(modals)) 
/**
 * Autogenerated ReadOnly Lens.
 * Read [NavBar.deviceData]
 */
@ReadOnly val deviceData: Lens<NavBar, Device> by lazy{ Lens(
    get = {whole -> whole.deviceData},
    set = {{it}}
) }
/**
 * Autogenerated Lens.
 * Read and manipulate [NavBar.i18n]
 */
@ReadWrite val i18n: Lens<NavBar, I18N> by lazy{ Lens(
    get = {whole -> whole.i18n},
    set = {part -> {whole -> whole.copy(i18n = part)}}
) }
/**
 * Autogenerated Setter of a Pseudo Lens
 * Manipulate [NavBar.i18n]
 */
@ReadWrite fun NavBar.i18n(set: I18N.()->I18N ): NavBar = copy(i18n = set(i18n)) 
/**
 * Autogenerated ReadOnly Lens.
 * Read [NavBar.environment]
 */
@ReadOnly val environment: Lens<NavBar, Environment> by lazy{ Lens(
    get = {whole -> whole.environment},
    set = {{it}}
) }
/**
 * Autogenerated Lens.
 * Read and manipulate [NavBar.user]
 */
@ReadWrite val user: Lens<NavBar, User> by lazy{ Lens(
    get = {whole -> whole.user},
    set = {part -> {whole -> whole.copy(user = part)}}
) }
/**
 * Autogenerated Setter of a Pseudo Lens
 * Manipulate [NavBar.user]
 */
@ReadWrite fun NavBar.user(set: User.()->User ): NavBar = copy(user = set(user)) 
