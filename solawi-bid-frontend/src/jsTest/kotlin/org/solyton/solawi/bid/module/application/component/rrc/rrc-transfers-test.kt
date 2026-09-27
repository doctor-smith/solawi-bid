package org.solyton.solawi.bid.module.application.component.rrc

import org.solyton.solawi.bid.module.permission.data.RightId
import org.solyton.solawi.bid.module.permissions.data.Right
import org.solyton.solawi.bid.module.permissions.data.Role
import org.solyton.solawi.bid.test.UUID_1
import org.solyton.solawi.bid.test.UUID_2
import org.solyton.solawi.bid.test.UUID_3
import org.solyton.solawi.bid.test.UUID_4
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@Suppress("LargeClass")
class RrcTransfersTest {

    @Suppress("VariableNaming")
    val right_1 = Right(
        UUID_1,
        "RIGHT_1",
        "description_1"
    )

    @Suppress("VariableNaming")
    val right_2 = Right(
        UUID_2,
        "RIGHT_2",
        "description_2"
    )

    @Suppress("VariableNaming")
    val right_3 = Right(
        UUID_3,
        "RIGHT_3",
        "description_3"
    )

    @Suppress("VariableNaming")
    val right_4 = Right(
        UUID_4,
        "RIGHT_4",
        "description_4"
    )



    @Test
    fun `exchange rights - move single right from role1 to role2`() {

        val role1 = Role(
            UUID_1,
            "ROLE_1",
            "",
            listOf(
                right_1,
                right_2
            )
        )

        val role2 = Role(
            UUID_2,
            "ROLE_2",
            "",
            listOf(
                right_3,
                right_4
            )
        )

        val roles = listOf(
            role1,
            role2
        )

        val result = roles.exchangeRights(
            RoleRightTransferData(
                TransferMode.Move,
                role1,
                role2,
                setOf(RightId(right_1.rightId)),
                setOf()
            ),
            ExchangeDirection.Right
        )

        val newRole1 = result.firstOrNull { it.roleId == role1.roleId }
        val newRole2 = result.firstOrNull { it.roleId == role2.roleId }

        assertNotNull(newRole1, "new role 1 not found")
        assertNotNull(newRole2, "new role 2 not found")

        assertEquals(
            1,
            newRole1.rights.size,
            "role1 should have 1 right after moving one out"
        )

        assertEquals(
            3,
            newRole2.rights.size,
            "role2 should have 3 rights after receiving one"
        )
    }

    @Test
    fun `exchange rights - copy single right from role1 to role2`() {

        val role1 = Role(
            UUID_1,
            "ROLE_1",
            "",
            listOf(
                right_1,
                right_2
            )
        )

        val role2 = Role(
            UUID_2,
            "ROLE_2",
            "",
            listOf(
                right_3,
                right_4
            )
        )

        val roles = listOf(
            role1,
            role2
        )

        val result = roles.exchangeRights(
            RoleRightTransferData(
                TransferMode.Copy,
                role1,
                role2,
                setOf(RightId(right_1.rightId)),
                setOf()
            ),
            ExchangeDirection.Right
        )

        val newRole1 = result.firstOrNull { it.roleId == role1.roleId }
        val newRole2 = result.firstOrNull { it.roleId == role2.roleId }

        assertNotNull(newRole1, "new role 1 not found")
        assertNotNull(newRole2, "new role 2 not found")

        assertEquals(
            2,
            newRole1.rights.size,
            "role1 should still have 2 rights after copying"
        )

        assertEquals(
            3,
            newRole2.rights.size,
            "role2 should have 3 rights after receiving copy"
        )
    }

    @Test
    fun `exchange rights - move multiple rights from role1 to role2`() {

        val role1 = Role(
            UUID_1,
            "ROLE_1",
            "",
            listOf(
                right_1,
                right_2
            )
        )

        val role2 = Role(
            UUID_2,
            "ROLE_2",
            "",
            listOf(
                right_3,
                right_4
            )
        )

        val roles = listOf(
            role1,
            role2
        )

        val result = roles.exchangeRights(
            RoleRightTransferData(
                TransferMode.Move,
                role1,
                role2,
                setOf(RightId(right_1.rightId), RightId(right_2.rightId)),
                setOf()
            ),
            ExchangeDirection.Right
        )

        val newRole1 = result.firstOrNull { it.roleId == role1.roleId }
        val newRole2 = result.firstOrNull { it.roleId == role2.roleId }

        assertNotNull(newRole1, "new role 1 not found")
        assertNotNull(newRole2, "new role 2 not found")

        assertEquals(
            0,
            newRole1.rights.size,
            "role1 should have no rights after moving all out"
        )

        assertEquals(
            4,
            newRole2.rights.size,
            "role2 should have 4 rights after receiving two"
        )
    }

    @Test
    fun `exchange rights - copy multiple rights from role1 to role2`() {

        val role1 = Role(
            UUID_1,
            "ROLE_1",
            "",
            listOf(
                right_1,
                right_2
            )
        )

        val role2 = Role(
            UUID_2,
            "ROLE_2",
            "",
            listOf(
                right_3,
                right_4
            )
        )

        val roles = listOf(
            role1,
            role2
        )

        val result = roles.exchangeRights(
            RoleRightTransferData(
                TransferMode.Copy,
                role1,
                role2,
                setOf(RightId(right_1.rightId), RightId(right_2.rightId)),
                setOf()
            ),
            ExchangeDirection.Right
        )

        val newRole1 = result.firstOrNull { it.roleId == role1.roleId }
        val newRole2 = result.firstOrNull { it.roleId == role2.roleId }

        assertNotNull(newRole1, "new role 1 not found")
        assertNotNull(newRole2, "new role 2 not found")

        assertEquals(
            2,
            newRole1.rights.size,
            "role1 should still have 2 rights after copying"
        )

        assertEquals(
            4,
            newRole2.rights.size,
            "role2 should have 4 rights after receiving copies"
        )
    }

    @Test
    fun `exchange rights - move right from role2 to role1 (left direction)`() {

        val role1 = Role(
            UUID_1,
            "ROLE_1",
            "",
            listOf(
                right_1,
                right_2
            )
        )

        val role2 = Role(
            UUID_2,
            "ROLE_2",
            "",
            listOf(
                right_3,
                right_4
            )
        )

        val roles = listOf(
            role1,
            role2
        )

        val result = roles.exchangeRights(
            RoleRightTransferData(
                TransferMode.Move,
                role1,
                role2,
                setOf(),
                setOf(RightId(right_3.rightId))
            ),
            ExchangeDirection.Left
        )

        val newRole1 = result.firstOrNull { it.roleId == role1.roleId }
        val newRole2 = result.firstOrNull { it.roleId == role2.roleId }

        assertNotNull(newRole1, "new role 1 not found")
        assertNotNull(newRole2, "new role 2 not found")

        assertEquals(
            3,
            newRole1.rights.size,
            "role1 should have 3 rights after receiving one from role2"
        )

        assertEquals(
            1,
            newRole2.rights.size,
            "role2 should have 1 right after moving one to role1"
        )
    }

    @Test
    fun `exchange rights - move with empty selection should not change roles`() {

        val role1 = Role(
            UUID_1,
            "ROLE_1",
            "",
            listOf(
                right_1,
                right_2
            )
        )

        val role2 = Role(
            UUID_2,
            "ROLE_2",
            "",
            listOf(
                right_3,
                right_4
            )
        )

        val roles = listOf(
            role1,
            role2
        )

        val result = roles.exchangeRights(
            RoleRightTransferData(
                TransferMode.Move,
                role1,
                role2,
                setOf(),
                setOf()
            ),
            ExchangeDirection.Right
        )

        val newRole1 = result.firstOrNull { it.roleId == role1.roleId }
        val newRole2 = result.firstOrNull { it.roleId == role2.roleId }

        assertNotNull(newRole1, "new role 1 not found")
        assertNotNull(newRole2, "new role 2 not found")

        assertEquals(
            2,
            newRole1.rights.size,
            "role1 should remain unchanged with 2 rights"
        )

        assertEquals(
            2,
            newRole2.rights.size,
            "role2 should remain unchanged with 2 rights"
        )
    }

    @Test
    fun `exchange rights - move last remaining right from role`() {

        val role1 = Role(
            UUID_1,
            "ROLE_1",
            "",
            listOf(
                right_1
            )
        )

        val role2 = Role(
            UUID_2,
            "ROLE_2",
            "",
            listOf(
                right_3,
                right_4
            )
        )

        val roles = listOf(
            role1,
            role2
        )

        val result = roles.exchangeRights(
            RoleRightTransferData(
                TransferMode.Move,
                role1,
                role2,
                setOf(RightId(right_1.rightId)),
                setOf()
            ),
            ExchangeDirection.Right
        )

        val newRole1 = result.firstOrNull { it.roleId == role1.roleId }
        val newRole2 = result.firstOrNull { it.roleId == role2.roleId }

        assertNotNull(newRole1, "new role 1 not found")
        assertNotNull(newRole2, "new role 2 not found")

        assertEquals(
            0,
            newRole1.rights.size,
            "role1 should have no rights after moving the last one"
        )

        assertEquals(
            3,
            newRole2.rights.size,
            "role2 should have 3 rights after receiving one"
        )
    }

    @Test
    fun `exchange rights - copy single right from role2 to role1 (left direction)`() {

        val role1 = Role(
            UUID_1,
            "ROLE_1",
            "",
            listOf(
                right_1,
                right_2
            )
        )

        val role2 = Role(
            UUID_2,
            "ROLE_2",
            "",
            listOf(
                right_3,
                right_4
            )
        )

        val roles = listOf(
            role1,
            role2
        )

        val result = roles.exchangeRights(
            RoleRightTransferData(
                TransferMode.Copy,
                role1,
                role2,
                setOf(),
                setOf(RightId(right_3.rightId))
            ),
            ExchangeDirection.Left
        )

        val newRole1 = result.firstOrNull { it.roleId == role1.roleId }
        val newRole2 = result.firstOrNull { it.roleId == role2.roleId }

        assertNotNull(newRole1, "new role 1 not found")
        assertNotNull(newRole2, "new role 2 not found")

        assertEquals(
            3,
            newRole1.rights.size,
            "role1 should have 3 rights after receiving copy from role2"
        )

        assertEquals(
            2,
            newRole2.rights.size,
            "role2 should still have 2 rights after copying"
        )
    }

    @Test
    fun `exchange rights - move multiple rights from role2 to role1 (left direction)`() {

        val role1 = Role(
            UUID_1,
            "ROLE_1",
            "",
            listOf(
                right_1,
                right_2
            )
        )

        val role2 = Role(
            UUID_2,
            "ROLE_2",
            "",
            listOf(
                right_3,
                right_4
            )
        )

        val roles = listOf(
            role1,
            role2
        )

        val result = roles.exchangeRights(
            RoleRightTransferData(
                TransferMode.Move,
                role1,
                role2,
                setOf(),
                setOf(RightId(right_3.rightId), RightId(right_4.rightId))
            ),
            ExchangeDirection.Left
        )

        val newRole1 = result.firstOrNull { it.roleId == role1.roleId }
        val newRole2 = result.firstOrNull { it.roleId == role2.roleId }

        assertNotNull(newRole1, "new role 1 not found")
        assertNotNull(newRole2, "new role 2 not found")

        assertEquals(
            4,
            newRole1.rights.size,
            "role1 should have 4 rights after receiving two from role2"
        )

        assertEquals(
            0,
            newRole2.rights.size,
            "role2 should have no rights after moving all out"
        )
    }

    @Test
    fun `exchange rights - copy multiple rights from role2 to role1 (left direction)`() {

        val role1 = Role(
            UUID_1,
            "ROLE_1",
            "",
            listOf(
                right_1,
                right_2
            )
        )

        val role2 = Role(
            UUID_2,
            "ROLE_2",
            "",
            listOf(
                right_3,
                right_4
            )
        )

        val roles = listOf(
            role1,
            role2
        )

        val result = roles.exchangeRights(
            RoleRightTransferData(
                TransferMode.Copy,
                role1,
                role2,
                setOf(),
                setOf(RightId(right_3.rightId), RightId(right_4.rightId))
            ),
            ExchangeDirection.Left
        )

        val newRole1 = result.firstOrNull { it.roleId == role1.roleId }
        val newRole2 = result.firstOrNull { it.roleId == role2.roleId }

        assertNotNull(newRole1, "new role 1 not found")
        assertNotNull(newRole2, "new role 2 not found")

        assertEquals(
            4,
            newRole1.rights.size,
            "role1 should have 4 rights after receiving copies from role2"
        )

        assertEquals(
            2,
            newRole2.rights.size,
            "role2 should still have 2 rights after copying"
        )
    }

    @Test
    fun `exchange rights - move rights in both directions simultaneously`() {

        val role1 = Role(
            UUID_1,
            "ROLE_1",
            "",
            listOf(
                right_1,
                right_2
            )
        )

        val role2 = Role(
            UUID_2,
            "ROLE_2",
            "",
            listOf(
                right_3,
                right_4
            )
        )

        val roles = listOf(
            role1,
            role2
        )

        val resultRight = roles.exchangeRights(
            RoleRightTransferData(
                TransferMode.Move,
                role1,
                role2,
                setOf(RightId(right_1.rightId)),
                setOf()
            ),
            ExchangeDirection.Right
        )

        val resultBoth = resultRight.exchangeRights(
            RoleRightTransferData(
                TransferMode.Move,
                role1,
                role2,
                setOf(),
                setOf(RightId(right_3.rightId))
            ),
            ExchangeDirection.Left
        )

        val newRole1 = resultBoth.firstOrNull { it.roleId == role1.roleId }
        val newRole2 = resultBoth.firstOrNull { it.roleId == role2.roleId }

        assertNotNull(newRole1, "new role 1 not found")
        assertNotNull(newRole2, "new role 2 not found")

        assertEquals(
            2,
            newRole1.rights.size,
            "role1 should have 2 rights after bidirectional exchange"
        )

        assertEquals(
            2,
            newRole2.rights.size,
            "role2 should have 2 rights after bidirectional exchange"
        )
    }

    @Test
    fun `exchange rights - copy rights in both directions simultaneously`() {

        val role1 = Role(
            UUID_1,
            "ROLE_1",
            "",
            listOf(
                right_1,
                right_2
            )
        )

        val role2 = Role(
            UUID_2,
            "ROLE_2",
            "",
            listOf(
                right_3,
                right_4
            )
        )

        val roles = listOf(
            role1,
            role2
        )

        val resultRight = roles.exchangeRights(
            RoleRightTransferData(
                TransferMode.Copy,
                role1,
                role2,
                setOf(RightId(right_1.rightId)),
                setOf()
            ),
            ExchangeDirection.Right
        )

        val resultBoth = resultRight.exchangeRights(
            RoleRightTransferData(
                TransferMode.Copy,
                role1,
                role2,
                setOf(),
                setOf(RightId(right_3.rightId))
            ),
            ExchangeDirection.Left
        )

        val newRole1 = resultBoth.firstOrNull { it.roleId == role1.roleId }
        val newRole2 = resultBoth.firstOrNull { it.roleId == role2.roleId }

        assertNotNull(newRole1, "new role 1 not found")
        assertNotNull(newRole2, "new role 2 not found")

        assertEquals(
            3,
            newRole1.rights.size,
            "role1 should have 3 rights after bidirectional copy"
        )

        assertEquals(
            3,
            newRole2.rights.size,
            "role2 should have 3 rights after bidirectional copy"
        )
    }

    @Test
    fun `exchange rights - move duplicate right should not add duplicates`() {

        val role1 = Role(
            UUID_1,
            "ROLE_1",
            "",
            listOf(
                right_1,
                right_2
            )
        )

        val role2 = Role(
            UUID_2,
            "ROLE_2",
            "",
            listOf(
                right_1,
                right_3,
                right_4
            )
        )

        val roles = listOf(
            role1,
            role2
        )

        val result = roles.exchangeRights(
            RoleRightTransferData(
                TransferMode.Move,
                role1,
                role2,
                setOf(RightId(right_1.rightId)),
                setOf()
            ),
            ExchangeDirection.Right
        )

        val newRole1 = result.firstOrNull { it.roleId == role1.roleId }
        val newRole2 = result.firstOrNull { it.roleId == role2.roleId }

        assertNotNull(newRole1, "new role 1 not found")
        assertNotNull(newRole2, "new role 2 not found")

        assertEquals(
            1,
            newRole1.rights.size,
            "role1 should have 1 right after moving one out"
        )

        assertEquals(
            3,
            newRole2.rights.size,
            "role2 should still have 3 rights (no duplicate added)"
        )
    }

    @Test
    fun `exchange rights - copy to empty role`() {

        val role1 = Role(
            UUID_1,
            "ROLE_1",
            "",
            listOf(
                right_1,
                right_2
            )
        )

        val role2 = Role(
            UUID_2,
            "ROLE_2",
            "",
            listOf()
        )

        val roles = listOf(
            role1,
            role2
        )

        val result = roles.exchangeRights(
            RoleRightTransferData(
                TransferMode.Copy,
                role1,
                role2,
                setOf(RightId(right_1.rightId), RightId(right_2.rightId)),
                setOf()
            ),
            ExchangeDirection.Right
        )

        val newRole1 = result.firstOrNull { it.roleId == role1.roleId }
        val newRole2 = result.firstOrNull { it.roleId == role2.roleId }

        assertNotNull(newRole1, "new role 1 not found")
        assertNotNull(newRole2, "new role 2 not found")

        assertEquals(
            2,
            newRole1.rights.size,
            "role1 should still have 2 rights after copying"
        )

        assertEquals(
            2,
            newRole2.rights.size,
            "role2 should have 2 rights after receiving copies"
        )
    }

    @Test
    fun `exchange rights - move from empty role should not change anything`() {

        val role1 = Role(
            UUID_1,
            "ROLE_1",
            "",
            listOf()
        )

        val role2 = Role(
            UUID_2,
            "ROLE_2",
            "",
            listOf(
                right_3,
                right_4
            )
        )

        val roles = listOf(
            role1,
            role2
        )

        val result = roles.exchangeRights(
            RoleRightTransferData(
                TransferMode.Move,
                role1,
                role2,
                setOf(RightId(right_1.rightId)),
                setOf()
            ),
            ExchangeDirection.Right
        )

        val newRole1 = result.firstOrNull { it.roleId == role1.roleId }
        val newRole2 = result.firstOrNull { it.roleId == role2.roleId }

        assertNotNull(newRole1, "new role 1 not found")
        assertNotNull(newRole2, "new role 2 not found")

        assertEquals(
            0,
            newRole1.rights.size,
            "role1 should remain empty"
        )

        assertEquals(
            2,
            newRole2.rights.size,
            "role2 should remain unchanged with 2 rights"
        )
    }

    @Test
    fun `exchange rights - move all rights from both roles leaving both empty`() {

        val role1 = Role(
            UUID_1,
            "ROLE_1",
            "",
            listOf(
                right_1,
                right_2
            )
        )

        val role2 = Role(
            UUID_2,
            "ROLE_2",
            "",
            listOf(
                right_3,
                right_4
            )
        )

        val roles = listOf(
            role1,
            role2
        )

        val resultRight = roles.exchangeRights(
            RoleRightTransferData(
                TransferMode.Move,
                role1,
                role2,
                setOf(RightId(right_1.rightId), RightId(right_2.rightId)),
                setOf()
            ),
            ExchangeDirection.Right
        )

        val resultBoth = resultRight.exchangeRights(
            RoleRightTransferData(
                TransferMode.Move,
                role1,
                role2,
                setOf(),
                setOf(
                    RightId(right_1.rightId),
                    RightId(right_2.rightId),
                    RightId(right_3.rightId),
                    RightId(right_4.rightId)
                )
            ),
            ExchangeDirection.Left
        )

        val newRole1 = resultBoth.firstOrNull { it.roleId == role1.roleId }
        val newRole2 = resultBoth.firstOrNull { it.roleId == role2.roleId }

        assertNotNull(newRole1, "new role 1 not found")
        assertNotNull(newRole2, "new role 2 not found")

        assertEquals(
            4,
            newRole1.rights.size,
            "role1 should have all 4 rights after moving back"
        )

        assertEquals(
            0,
            newRole2.rights.size,
            "role2 should be empty after moving all rights back"
        )
    }

    @Test
    fun `exchange rights - move non-existent right should not affect roles`() {

        val role1 = Role(
            UUID_1,
            "ROLE_1",
            "",
            listOf(
                right_1,
                right_2
            )
        )

        val role2 = Role(
            UUID_2,
            "ROLE_2",
            "",
            listOf(
                right_3
            )
        )

        val roles = listOf(
            role1,
            role2
        )

        val result = roles.exchangeRights(
            RoleRightTransferData(
                TransferMode.Move,
                role1,
                role2,
                setOf(RightId(right_4.rightId)),
                setOf()
            ),
            ExchangeDirection.Right
        )

        val newRole1 = result.firstOrNull { it.roleId == role1.roleId }
        val newRole2 = result.firstOrNull { it.roleId == role2.roleId }

        assertNotNull(newRole1, "new role 1 not found")
        assertNotNull(newRole2, "new role 2 not found")

        assertEquals(
            2,
            newRole1.rights.size,
            "role1 should remain unchanged with 2 rights"
        )

        assertEquals(
            1,
            newRole2.rights.size,
            "role2 should remain unchanged with 1 right"
        )
    }
}
