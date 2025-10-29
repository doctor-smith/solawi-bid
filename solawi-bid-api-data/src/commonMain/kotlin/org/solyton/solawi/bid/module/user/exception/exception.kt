package org.solyton.solawi.bid.module.user.exception

import org.solyton.solawi.bid.module.user.data.api.organization.Organization

sealed class UserManagementException(override val message: String?) : Exception(message) {
     sealed class UserDoesNotExist(override val message: String) : UserManagementException(message) {
         data class Id(val id: String) : UserDoesNotExist("User with id $id does not exists")
         data class Username(val username: String) : UserDoesNotExist("User with username $username does not exists")
     }
    data object WrongCredentials : UserManagementException("Wrong credentials")
}

sealed class OrganizationException(override val message: String) : Exception(message) {
    data class NoSuchOrganization(val id: String) : OrganizationException("No such organization; id = $id")

    data class NoSuchChildOrganization(val id: String) : OrganizationException("No such child organization; id = $id")

    data class NoRoot(val id: String): OrganizationException("Child organization '$id' has not root!")

    data class DuplicateMember(val id: String): OrganizationException("Member already exists: id = $id")
}
