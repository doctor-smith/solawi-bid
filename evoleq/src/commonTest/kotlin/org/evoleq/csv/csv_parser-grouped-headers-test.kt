package org.evoleq.csv

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class CsvParserTestGroupedHeaders {

    @Test
    fun `parseCsvWithGroupedHeaders - valid input with single area`() {
        val csv = """
            Area1;;;;
            Header1;Header2;Header3;;
            Value1;Value2;Value3;;
        """.trimIndent()

        val expected = listOf(
            mapOf(
                "Area1" to mapOf(
                    "Header1" to "Value1",
                    "Header2" to "Value2",
                    "Header3" to "Value3"
                )
            )
        )

        val result = parseCsvWithGroupedHeaders(csv, ";")
        assertEquals(expected, result)
    }

    @Test
    fun `parseCsvWithGroupedHeaders - valid input with multiple areas`() {
        val csv = """
            Area1;;;Area2;;
            H1;H2;H3;H4;H5;
            V1;V2;V3;V4;V5;
        """.trimIndent()

        val expected = listOf(
            mapOf(
                "Area1" to mapOf(
                    "H1" to "V1",
                    "H2" to "V2",
                    "H3" to "V3"
                ),
                "Area2" to mapOf(
                    "H4" to "V4",
                    "H5" to "V5"
                )
            )
        )

        val result = parseCsvWithGroupedHeaders(csv, ";")
        assertEquals(expected, result)
    }

    @Test
    fun `parseCsvWithGroupedHeaders - missing data row`() {
        val csv = """
            Area1;;;Area2;;
            H1;H2;H3;H4;H5;
        """.trimIndent()

        val expected = emptyList<Map<String, Map<String, String>>>()

        val result = parseCsvWithGroupedHeaders(csv, ";")
        assertEquals(expected, result)
    }

    @Test
    fun `parseCsvWithGroupedHeaders - valid input with matched area and header counts`() {
        val csv = """
            Area1;;Area2;;
            H1;H2;H3;H4;
            V1;V2;V3;V4;
        """.trimIndent()

        val expected = listOf(
            mapOf(
                "Area1" to mapOf(
                    "H1" to "V1",
                    "H2" to "V2"
                ),
                "Area2" to mapOf(
                    "H3" to "V3",
                    "H4" to "V4"
                )
            )
        )

        val result = parseCsvWithGroupedHeaders(csv, ";")
        assertEquals(expected, result)
    }

    @Test
    fun `parseCsvWithGroupedHeaders - invalid input with less than two rows`() {
        val csv = """
            Area1;;;;
        """.trimIndent()

        val exception = assertFailsWith<IllegalArgumentException> {
            parseCsvWithGroupedHeaders(csv, ";")
        }

        assertEquals("Expecting at least 2 lines (Areas + Header line).", exception.message)
    }

    @Test
    fun `parseCsvWithGroupedHeaders - valid input with empty header cells`() {
        val csv = """
            Area1;;;Area2;;
            H1;;H3;H4;;
            V1;V2;V3;V4;V5;
        """.trimIndent()

        val expected = listOf(
            mapOf(
                "Area1" to mapOf(
                    "H1" to "V1",
                    "H3" to "V3"
                ),
                "Area2" to mapOf(
                    "H4" to "V4"
                )
            )
        )

        val result = parseCsvWithGroupedHeaders(csv, ";")
        assertEquals(expected, result)
    }


    /**
     * Tests for the `groupOnNonEmptyStart` function.
     *
     * This function groups elements of a list into sublists based on non-empty start
     * markers, continuing to group empty elements into the current group.
     */

    @Test
    fun `groupOnNonEmptyStart with multiple groups`() {
        // Arrange
        val input = listOf("Group1", "", "", "Group2", "", "Group3", "")

        // Act
        val result = input.groupOnNonEmptyStart()

        // Assert
        val expected = listOf(
            listOf("Group1", "", ""),
            listOf("Group2", ""),
            listOf("Group3", "")
        )
        assertEquals(expected, result)
    }

    @Test
    fun `groupOnNonEmptyStart with no non-empty start`() {
        // Arrange
        val input = listOf("", "", "")

        // Act
        val result = input.groupOnNonEmptyStart()

        // Assert
        val expected = emptyList<List<String>>()
        assertEquals(expected, result)
    }

    @Test
    fun `groupOnNonEmptyStart with only one group`() {
        // Arrange
        val input = listOf("Group1", "", "", "")

        // Act
        val result = input.groupOnNonEmptyStart()

        // Assert
        val expected = listOf(listOf("Group1", "", "", ""))
        assertEquals(expected, result)
    }

    @Test
    fun `groupOnNonEmptyStart with all elements empty`() {
        // Arrange
        val input = listOf("", "", "")

        // Act
        val result = input.groupOnNonEmptyStart()

        // Assert
        val expected = emptyList<List<String>>()
        assertEquals(expected, result)
    }

    @Test
    fun `groupOnNonEmptyStart with multiple non-empty starts and no trailing empty strings`() {
        // Arrange
        val input = listOf("Group1", "Group2", "Group3")

        // Act
        val result = input.groupOnNonEmptyStart()

        // Assert
        val expected = listOf(
            listOf("Group1"),
            listOf("Group2"),
            listOf("Group3")
        )
        assertEquals(expected, result)
    }

    @Test
    fun `chunkBySizes should split list into chunks of given sizes`() {
        val input = listOf(1, 2, 3, 4, 5)
        val sizes = intArrayOf(2, 3)

        val result = input.chunkBySizes(*sizes)

        assertEquals(listOf(listOf(1, 2), listOf(3, 4, 5)), result)
    }

    @Test
    fun `chunkBySizes should handle an empty list`() {
        val input = emptyList<Int>()
        val sizes = intArrayOf(0)

        val result = input.chunkBySizes(*sizes)

        assertEquals(listOf(emptyList<Int>()), result)
    }

    @Test
    fun `chunkBySizes should fail if sizes contain negative numbers`() {
        val input = listOf(1, 2, 3, 4, 5)
        val sizes = intArrayOf(2, -1, 3)

        assertFailsWith<IllegalArgumentException> {
            input.chunkBySizes(*sizes)
        }
    }

    @Test
    fun `chunkBySizes should fail if there's not enough elements for given sizes`() {
        val input = listOf(1, 2, 3)
        val sizes = intArrayOf(2, 3)

        assertFailsWith<IllegalArgumentException> {
            input.chunkBySizes(*sizes)
        }
    }

    @Test
    fun `chunkBySizes should fail if there are unprocessed elements left`() {
        val input = listOf(1, 2, 3, 4)
        val sizes = intArrayOf(2, 1)

        assertFailsWith<IllegalArgumentException> {
            input.chunkBySizes(*sizes)
        }
    }

    @Test fun `parseCsvWithGroupedHeaders - real world example`() {
        val result:List<Map<String, Map<String, String>>> = parseCsvWithGroupedHeaders(realWordExample, ",")
        val jsonResult = Json {
            prettyPrint = true
            encodeDefaults = true
        }.encodeToString(result)
        println(jsonResult)
        assertEquals(10, result.size)


        result.forEach {
            val user = it["user_profiles"]!!
            val username = user["username"]!!
            val firstname = user["firstname"]!!
            val lastname = user["lastname"]!!
            val title = user["title"]!!
            val recipientName = user["recipient_name"]!!
            val organizationName = user["organization_name"]!!
            val addressLine1 = user["address_line_1"]!!
            val addressLine2 = user["address_line_2"]!!
            val city = user["city"]!!
            val stateOrProvince = user["state_or_province"]!!
            val postalCode = user["postal_code"]!!
            val countryCode = user["country_code"]!!
            val vegiShare = it["share_subscriptions.flexible?key=vegi"]!!
            val egsShare = it["share_subscriptions.fixed?key=eggs"]!!

            assertEquals(true, username.startsWith("test-user_"))
            assertEquals(true, username.endsWith("@solyton.org"))
            assertNotNull( firstname)
            assertNotNull( lastname)
            assertNotNull( title)
            assertNotNull( recipientName)
            assertNotNull( organizationName)
            assertNotNull( addressLine1)
            assertNotNull( addressLine2)
            assertNotNull( city)
            assertNotNull( stateOrProvince)
            assertNotNull( countryCode )
            assertNotNull( postalCode )
            assertNotNull( vegiShare["ahc_autorized"])
            assertEquals("SUBSCRIBED", vegiShare["status"])
            assertEquals("25", vegiShare["fiscal_year"])
            assertEquals("SUBSCRIBED", egsShare["status"])
            assertEquals("25", egsShare["fiscal_year"])
        }

    }
}


val realWordExample = """
    user_profiles,,,,,,,,,,,,share_subscriptions.flexible?key=vegi,,,,,,,share_subscriptions.fixed?key=eggs,,,,,
    username, firstname, lastname, title, recipient_name, organization_name, address_line_1, address_line_2, city, state_or_province, postal_code, country_code,number_of_shares,price_per_share,ahc_autorized,status,co_subscribers,distribution_point,fiscal_year,number_of_shares,ahc_autorized,status,co_subscribers,distribution_point,fiscal_year
    test-user_01@solyton.org, Max, Mustermann, Dr., Max Mustermann, Solyton GmbH, Musterstraße 1, , Berlin, Berlin,10115, DE,1,80,true,SUBSCRIBED,,WH,25,1,ahc_autorized,SUBSCRIBED,,WH,25
    test-user_02@solyton.org, Erika, Musterfrau, Prof., Erika Musterfrau, Solyton GmbH, Beispielweg 2, , München, Bayern,80331, DE,1,90,true,SUBSCRIBED,,WH,25,2,ahc_autorized,SUBSCRIBED,,WH,25
    test-user_03@solyton.org, Hans, Schmidt, Mr., Hans Schmidt, Schmidt & Co, Hauptstraße 10, Apt 4, Hamburg, Hamburg,20095, DE,2,50,true,SUBSCRIBED,,DS,25,1,ahc_autorized,SUBSCRIBED,,DS,25
    test-user_04@solyton.org, Julia, Müller, Ms., Julia Müller, Müller IT, Schulgasse 5, , Köln, NRW,50667, DE,1,80,true,SUBSCRIBED,,D1,25,0,ahc_autorized,SUBSCRIBED,,D1,25
    test-user_05@solyton.org, Thomas, Weber, Sir, Thomas Weber, Weber Logistik, Industriepark 12, Gebäude B, Frankfurt, Hessen,60311, DE,2,80,true,SUBSCRIBED,,D2,25,0,ahc_autorized,SUBSCRIBED,,D2,25
    test-user_06@solyton.org, Sarah, Wagner, Dr., Sarah Wagner, Wagner Consult, Ringstraße 3, , Stuttgart, BW,70173, DE,1,70,true,SUBSCRIBED,,SP,25,0,ahc_autorized,SUBSCRIBED,,SP,25
    test-user_07@solyton.org, Andreas, Becker, Mr., Andreas Becker, Becker Bau, Waldweg 8, , Leipzig, Sachsen,4109, DE,1,80,true,SUBSCRIBED,,RB,25,0,ahc_autorized,SUBSCRIBED,,RB,25
    test-user_08@solyton.org, Monika, Hoffmann, Mrs., Monika Hoffmann, Hoffmann Design, Kunstplatz 1, Etage 2, Düsseldorf, NRW,40213, DE,1,80,true,SUBSCRIBED,,SP,25,0,ahc_autorized,SUBSCRIBED,,SP,25
    test-user_09@solyton.org, Stefan, Koch, Mr., Stefan Koch, Koch Solutions, Hafenstraße 22, , Bremen, Bremen,28195, DE,2,80,true,SUBSCRIBED,,WH,25,0,ahc_autorized,SUBSCRIBED,,WH,25
    test-user_10@solyton.org, Petra, Richter, Ms., Petra Richter, Richter Recht, Markt 5, , Dresden, Sachsen,1067, DE,1,100,true,SUBSCRIBED,,SP,25,0,ahc_autorized,SUBSCRIBED,,SP,25
""".trimIndent()
