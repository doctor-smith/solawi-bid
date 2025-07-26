package org.solyton.solawi.bid.module.user.component.modal

import org.evoleq.language.Lang
import org.evoleq.math.Source
import org.evoleq.math.emit
import org.evoleq.math.times
import org.solyton.solawi.bid.module.user.data.reader.newPasswordEqualsOld
import org.solyton.solawi.bid.module.user.data.reader.repeatedPasswordMismatch
import org.solyton.solawi.bid.module.user.data.reader.requirementsViolated
import org.solyton.solawi.bid.module.user.data.reader.wrongPassword
import org.solyton.solawi.bid.module.user.service.PasswordCombinationCheck

fun messageFrom(
    passwordCombinationCheck: PasswordCombinationCheck,
    errors: Source<Lang.Block>
): String? = when(passwordCombinationCheck) {
    PasswordCombinationCheck.Passed, PasswordCombinationCheck.Empty -> null
    PasswordCombinationCheck.WrongPassword -> (errors * wrongPassword).emit()
    PasswordCombinationCheck.RequirementsViolated -> (errors * requirementsViolated).emit()
    PasswordCombinationCheck.RepeatedPasswordMismatch -> (errors * repeatedPasswordMismatch).emit()
    PasswordCombinationCheck.NewPasswordEqualsStoredPassword -> (errors * newPasswordEqualsOld).emit()
}
