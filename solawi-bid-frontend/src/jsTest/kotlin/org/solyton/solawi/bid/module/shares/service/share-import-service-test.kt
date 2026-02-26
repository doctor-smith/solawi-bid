package org.solyton.solawi.bid.module.shares.service

import org.solyton.solawi.bid.module.shares.data.mappings.ShareManagementMappings
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals


/*
class ComputeShareSubscriptionDataForImportTest {
    private lateinit var shareManagementMappings: ShareManagementMappings
    private lateinit var typedMemberMaps: List<Map<String, Map<String, String>>>

    private lateinit var csv: String
    @BeforeTest
    fun setup() {
        shareManagementMappings = ShareManagementMappings(
            shareOffers = mapOf("vegi" to "offer1", "eggs" to "offer2"),
            distributionPoints = mapOf("point1" to "dp1"),
            fiscalYearId = "fy2026",
            providerId = "provider1"
        )

        typedMemberMaps = listOf(
            mapOf(
                "user_profiles" to mapOf("username" to "user1"),
                "share_subscriptions.flexible?key=vegi" to mapOf(
                    "distribution_point" to "point1",
                    "number_of_shares" to "2",
                    "price_per_share" to "100.0",
                    "ahc_authorized" to "true",
                    "status" to "SUBSCRIBED",
                    "co_subscribers" to "user2, user3"
                ),
                "share_subscriptions.fixed?key=eggs" to mapOf(
                    "distribution_point" to "point1",
                    "number_of_shares" to "1",
                    "ahc_authorized" to "true",
                    "status" to "SUBSCRIBED",
                    "co_subscribers" to "user2, user3"
                )
            )
        )

        csv = csv()
    }

    @Test
    fun testSuccessfulImport() {
        val result = computeShareSubscriptionDataForImport(typedMemberMaps, shareManagementMappings)

        assertEquals(2, result.size)

        val vegi = result.find { it.shareOfferId == "offer1" }!!
        assertEquals("offer1", vegi.shareOfferId)
        assertEquals("user1", vegi.username)
        assertEquals("dp1", vegi.distributionPointId)
        assertEquals(2, vegi.numberOfShares)
        assertEquals(100.0, vegi.pricePerShare)
        assertEquals(true, vegi.ahcAuthorized)
        assertEquals(listOf("user2", "user3"), vegi.coSubscribers)

        val eggs = result.find { it.shareOfferId == "offer2" }!!
        assertEquals("offer2", eggs.shareOfferId)
        assertEquals("user1", eggs.username)
        assertEquals("dp1", eggs.distributionPointId)
        assertEquals(1, eggs.numberOfShares)
        assertEquals(null, eggs.pricePerShare)
        assertEquals(true, eggs.ahcAuthorized)
        assertEquals(listOf("user2", "user3"), eggs.coSubscribers)
    }

    @Test
    fun testInvalidColumnType() {
        val invalidMap = listOf(
            mapOf(
                "user_profiles" to mapOf("username" to "user1"),
                "invalid_column_format" to mapOf()
            )
        )

        val result = computeShareSubscriptionDataForImport(invalidMap, shareManagementMappings)
        assertEquals(0, result.size)
    }
}

fun csv():String = """
    user_profiles;;;;;;;;;;;;share_subscriptions.flexible?key=vegi;;;;;;;share_subscriptions.fixed?key=eggs;;;;;
    username; firstname; lastname; title; recipient_name; organization_name; address_line_1; address_line_2; city; state_or_province; postal_code; country_code;number_of_shares;price_per_share;ahc_autorized;status;co_subscribers;distribution_point;fiscal_year;number_of_shares;ahc_autorized;status;co_subscribers;distribution_point;fiscal_year
    test-user_01@solyton.org; Max; Mustermann; Dr.; Max Mustermann; Solyton GmbH; Musterstraße 1; ; Berlin; Berlin;10115; DE;1;80;true;SUBSCRIBED;;WH;25;1;ahc_autorized;SUBSCRIBED;;WH;25
    test-user_02@solyton.org; Erika; Musterfrau; Prof.; Erika Musterfrau; Solyton GmbH; Beispielweg 2; ; München; Bayern;80331; DE;1;90;true;SUBSCRIBED;;WH;25;2;ahc_autorized;SUBSCRIBED;;WH;25
    test-user_03@solyton.org; Hans; Schmidt; Mr.; Hans Schmidt; Schmidt & Co; Hauptstraße 10; Apt 4; Hamburg; Hamburg;20095; DE;2;50;true;SUBSCRIBED;;DS;25;1;ahc_autorized;SUBSCRIBED;;DS;25
    test-user_04@solyton.org; Julia; Müller; Ms.; Julia Müller; Müller IT; Schulgasse 5; ; Köln; NRW;50667; DE;1;80;true;SUBSCRIBED;;D1;25;0;ahc_autorized;SUBSCRIBED;;D1;25
    test-user_05@solyton.org; Thomas; Weber; Sir; Thomas Weber; Weber Logistik; Industriepark 12; Gebäude B; Frankfurt; Hessen;60311; DE;2;80;true;SUBSCRIBED;;D2;25;0;ahc_autorized;SUBSCRIBED;;D2;25
    test-user_06@solyton.org; Sarah; Wagner; Dr.; Sarah Wagner; Wagner Consult; Ringstraße 3; ; Stuttgart; BW;70173; DE;1;70;true;SUBSCRIBED;;SP;25;0;ahc_autorized;SUBSCRIBED;;SP;25
    test-user_07@solyton.org; Andreas; Becker; Mr.; Andreas Becker; Becker Bau; Waldweg 8; ; Leipzig; Sachsen;4109; DE;1;80;true;SUBSCRIBED;;RB;25;0;ahc_autorized;SUBSCRIBED;;RB;25
    test-user_08@solyton.org; Monika; Hoffmann; Mrs.; Monika Hoffmann; Hoffmann Design; Kunstplatz 1; Etage 2; Düsseldorf; NRW;40213; DE;1;80;true;SUBSCRIBED;;SP;25;0;ahc_autorized;SUBSCRIBED;;SP;25
    test-user_09@solyton.org; Stefan; Koch; Mr.; Stefan Koch; Koch Solutions; Hafenstraße 22; ; Bremen; Bremen;28195; DE;2;80;true;SUBSCRIBED;;WH;25;0;ahc_autorized;SUBSCRIBED;;WH;25
    test-user_10@solyton.org; Petra; Richter; Ms.; Petra Richter; Richter Recht; Markt 5; ; Dresden; Sachsen;1067; DE;1;100;true;SUBSCRIBED;;SP;25;0;ahc_autorized;SUBSCRIBED;;SP;25
""".trimIndent()
*/
