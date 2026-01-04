package org.solyton.solawi.bid.module.user.parser

import org.evoleq.csv.parseCsv
import org.solyton.solawi.bid.module.user.data.api.organization.ImportMembers
import kotlin.test.Test
import kotlin.test.assertEquals

class ImportMembersTest {

    val membersCSV = """
        username
        test-user_01@solyton.org
        test-user_02@solyton.org
        test-user_03@solyton.org
        test-user_04@solyton.org
        test-user_05@solyton.org
        test-user_06@solyton.org
        test-user_07@solyton.org
        test-user_08@solyton.org
        test-user_09@solyton.org
        test-user_10@solyton.org
    """.trimIndent()


    @Test
    fun parseMembersTest() {
        val result: List<Map<String, String>> = parseCsv(membersCSV, ",")
        val expected = listOf(
            mapOf("username" to "test-user_01@solyton.org"),
            mapOf("username" to "test-user_02@solyton.org"),
            mapOf("username" to "test-user_03@solyton.org"),
            mapOf("username" to "test-user_04@solyton.org"),
            mapOf("username" to "test-user_05@solyton.org"),
            mapOf("username" to "test-user_06@solyton.org"),
            mapOf("username" to "test-user_07@solyton.org"),
            mapOf("username" to "test-user_08@solyton.org"),
            mapOf("username" to "test-user_09@solyton.org"),
            mapOf("username" to "test-user_10@solyton.org"),
        )

        assertEquals(expected, result)
    }

    @Test
    fun parseMembersAndTransformToDomainTypeTest() {
        val usernames = parseCsv(membersCSV, ",").map{
            it["username"]!!
        }
        val importMembers = ImportMembers("id", usernames)

        val expected = ImportMembers(
            "id", listOf(
                "test-user_01@solyton.org",
                "test-user_02@solyton.org",
                "test-user_03@solyton.org",
                "test-user_04@solyton.org",
                "test-user_05@solyton.org",
                "test-user_06@solyton.org",
                "test-user_07@solyton.org",
                "test-user_08@solyton.org",
                "test-user_09@solyton.org",
                "test-user_10@solyton.org",
            )
        )

        assertEquals(expected, importMembers)
    }
}
