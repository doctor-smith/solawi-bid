package org.solyton.solawi.bid.module.user.data.transform

import org.solyton.solawi.bid.module.permission.data.api.ApiRight
import org.solyton.solawi.bid.module.permission.data.api.ApiRole
import org.solyton.solawi.bid.module.permissions.data.transform.toDomainType
import org.solyton.solawi.bid.module.user.data.api.organization.ApiMember
import org.solyton.solawi.bid.module.user.data.api.organization.ApiOrganization
import org.solyton.solawi.bid.module.user.data.member.Member
import org.solyton.solawi.bid.module.user.data.organization.Organization
import kotlin.test.Test
import kotlin.test.assertEquals

class TransformTests {

    fun apiMember(userId: String,  roles: List<ApiRole>) = ApiMember(
        userId,
        roles
    )

    fun apiRole(name: String, rights: List<ApiRight>) = ApiRole(name, name, name, rights )

    fun apiRight(name: String) = ApiRight(name, name, name)


    @Test fun transformMember() {
        val apiMember = apiMember(
            "user_id",
            listOf(
                apiRole(
                    "ROLE",
                    listOf(apiRight("RIGHT"))
                )
            )
        )

        val expected = Member(
            "user_id",
            apiMember.roles.map { it.toDomainType() }
        )

        val result = apiMember.toDomainType()

        assertEquals(expected, result)
    }

    @Test fun transformOrganization() {
        val apiOrganization = ApiOrganization(
            "id",
            "name",
            "id",
            listOf(),
            listOf(
                apiMember(
                    "user_id",
                    listOf(
                        apiRole(
                            "ROLE",
                            listOf(apiRight("RIGHT"))
                        )
                    )
                )
            )
        )

        val expected = Organization(
            "id",
            "name",
            "id",
            listOf(),
            apiOrganization.members.map { it.toDomainType() }
        )

        val result = apiOrganization.toDomainType()

        assertEquals(expected, result)
    }
}
