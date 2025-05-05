package org.solyton.solawi.bid.module.user.exception

sealed class UserManagementException(override val message: String?) : Exception(message) {
     sealed class UserDoesNotExist(override val message: String) : UserManagementException(message) {
         data class Id(val id: String) : UserDoesNotExist("User with id $id does not exists")
         data class Username(val username: String) : UserDoesNotExist("User with username $username does not exists")
     }
    data object WrongCredentials : UserManagementException("Wrong credentials")
}