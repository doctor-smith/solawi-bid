// This file has been partially auto generated. 
// Please don't make any changes to the lenses.
// Feel free to add or remove annotated properties from
// the generator data class. The corresponding lenses 
// will be removed or added on the next run of the 
// lens generator. See below for more details.
package org.solyton.solawi.bid.module.authentication.data

import org.evoleq.language.Lang
import org.evoleq.optics.Lensify
import org.evoleq.optics.ReadOnly
import org.evoleq.optics.ReadWrite
import org.evoleq.optics.lens.Lens
import org.evoleq.compose.style.data.device.DeviceType

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
@Lensify data class LoginForm(
    @ReadWrite val user: User,
    @ReadOnly val texts: Lang.Block,
    @ReadOnly val deviceType: DeviceType
)

/**
 * Autogenerated Lens.
 * Read and manipulate [LoginForm.user]
 */
@ReadWrite val user: Lens<LoginForm, User> by lazy{ Lens(
    get = {whole -> whole.user},
    set = {part -> {whole -> whole.copy(user = part)}}
) }
/**
 * Autogenerated Setter of a Pseudo Lens
 * Manipulate [LoginForm.user]
 */
@ReadWrite fun LoginForm.user(set: User.()->User ): LoginForm = copy(user = set(user)) 
/**
 * Autogenerated ReadOnly Lens.
 * Read [LoginForm.texts]
 */
@ReadOnly val texts: Lens<LoginForm, Lang.Block> by lazy{ Lens(
    get = {whole -> whole.texts},
    set = {{it}}
) }
/**
 * Autogenerated ReadOnly Lens.
 * Read [LoginForm.deviceType]
 */
@ReadOnly val deviceType: Lens<LoginForm, DeviceType> by lazy{ Lens(
    get = {whole -> whole.deviceType},
    set = {{it}}
) }
