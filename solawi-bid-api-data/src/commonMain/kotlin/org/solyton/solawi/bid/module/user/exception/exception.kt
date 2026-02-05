package org.solyton.solawi.bid.module.user.exception


sealed class UserManagementException(override val message: String?) : Exception(message) {
     sealed class UserDoesNotExist(override val message: String) : UserManagementException(message) {
         data class Id(val id: String) : UserDoesNotExist("User with id $id does not exists")
         data class Username(val username: String) : UserDoesNotExist("User with username $username does not exists")
     }
    sealed class UsersDoNotExist(override val message: String) : UserManagementException(message) {
        data class Usernames(val usernames: List<String>) : UserDoesNotExist("Users with usernames ${usernames.joinToString(", ") { it }} do not exists")
        data class Ids(val ids: List<String>) : UsersDoNotExist("Users with ids ${ids.joinToString()} do not exists")
    }
    data object WrongCredentials : UserManagementException("Wrong credentials")

    data class NoSuchUserProfile(val id: String): UserManagementException("No such user profile $id")
}

sealed class OrganizationException(override val message: String) : Exception(message) {
    data class NoSuchOrganization(val id: String) : OrganizationException("No such organization; id = $id")

    data class NoSuchChildOrganization(val id: String) : OrganizationException("No such child organization; id = $id")

    data class NoRoot(val id: String): OrganizationException("Child organization '$id' has not root!")

    data class DuplicateMember(val id: String): OrganizationException("Member already exists: id = $id")

    data class CannotDeleteOrganization(val id: String, val reason: String): OrganizationException(
        "Organization $id cannot be deleted. Reason: $reason"
    )
}
