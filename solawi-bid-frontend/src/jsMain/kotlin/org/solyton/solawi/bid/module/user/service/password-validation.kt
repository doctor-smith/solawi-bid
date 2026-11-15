package org.solyton.solawi.bid.module.user.service

import org.solyton.solawi.bid.module.user.data.api.PasswordRequirements

fun <T> onPasswordCombinationValid(
    value: T,
    storedPassword: String?,
    oldPassword: String?,
    newPassword: String,
    newPasswordRepeat: String,
    act: (T)->Unit
): PasswordCombinationCheck = when {
    storedPassword != null && oldPassword != null && oldPassword != storedPassword -> PasswordCombinationCheck.WrongPassword
    newPassword.isNotEmpty() && newPassword.length < PasswordRequirements.MINIMUM_LENGTH -> PasswordCombinationCheck.RequirementsViolated
    newPassword == storedPassword -> PasswordCombinationCheck.NewPasswordEqualsStoredPassword
    newPassword != newPasswordRepeat -> PasswordCombinationCheck.RepeatedPasswordMismatch
    else -> {
        act(value)
        PasswordCombinationCheck.Passed
    }
}


sealed class PasswordCombinationCheck {
    data object Empty: PasswordCombinationCheck()
    data object Passed: PasswordCombinationCheck()
    data object RepeatedPasswordMismatch: PasswordCombinationCheck()
    data object NewPasswordEqualsStoredPassword: PasswordCombinationCheck()
    data object WrongPassword: PasswordCombinationCheck()
    data object RequirementsViolated: PasswordCombinationCheck()
}
