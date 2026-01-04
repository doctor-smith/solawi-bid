package org.solyton.solawi.bid.module.user.parser

import org.evoleq.csv.parseCsv
import org.solyton.solawi.bid.module.user.data.api.userprofile.CreateAddress
import org.solyton.solawi.bid.module.user.data.api.userprofile.UserProfileToImport
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ImportUserProfilesTest {
    val userProfilesCSV = """
        username; firstname; lastname; title; phone_number; recipient_name; organization_name; address_line_1; address_line_2; city; state_or_province; postal_code; country_code
        test-user_01@solyton.org; Max; Mustermann; Dr.;017522299944; Max Mustermann; Solyton GmbH; Musterstraße 1; ; Berlin; Berlin; 10115; DE
        test-user_02@solyton.org; Erika; Musterfrau; Prof.;; Erika Musterfrau; Solyton GmbH; Beispielweg 2; ; München; Bayern; 80331; DE
        test-user_03@solyton.org; Hans; Schmidt; Mr.;; Hans Schmidt; Schmidt & Co; Hauptstraße 10; Apt 4; Hamburg; Hamburg; 20095; DE
        test-user_04@solyton.org; Julia; Müller; Ms.;; Julia Müller; Müller IT; Schulgasse 5; ; Köln; NRW; 50667; DE
        test-user_05@solyton.org; Thomas; Weber; Sir;; Thomas Weber; Weber Logistik; Industriepark 12; Gebäude B; Frankfurt; Hessen; 60311; DE
        test-user_06@solyton.org; Sarah; Wagner; Dr.;; Sarah Wagner; Wagner Consult; Ringstraße 3; ; Stuttgart; BW; 70173; DE
        test-user_07@solyton.org; Andreas; Becker; Mr.;; Andreas Becker; Becker Bau; Waldweg 8; ; Leipzig; Sachsen; 04109; DE
        test-user_08@solyton.org; Monika; Hoffmann; Mrs.;; Monika Hoffmann; Hoffmann Design; Kunstplatz 1; Etage 2; Düsseldorf; NRW; 40213; DE
        test-user_09@solyton.org; Stefan; Koch; Mr.;; Stefan Koch; Koch Solutions; Hafenstraße 22; ; Bremen; Bremen; 28195; DE
        test-user_10@solyton.org; Petra; Richter; Ms.;; Petra Richter; Richter Recht; Markt 5; ; Dresden; Sachsen; 01067; DE
    """.trimIndent()

    @Test
    fun parseUserProfilesTest() {
        val result: List<Map<String, String>> = parseCsv(userProfilesCSV, ";")
        val keys = listOf<String>(
            "username", "firstname", "lastname", "title", "phone_number","recipient_name", "organization_name", "address_line_1", "address_line_2", "city", "state_or_province", "postal_code", "country_code"
        )

        val defectProfiles = result.filter{it.keys.size != keys.size}
        assertTrue { defectProfiles.isEmpty() }


        val userProfiles = result.map {
            entry -> UserProfileToImport(
                username = entry["username"]!!,
                firstName = entry["firstname"]!!,
                lastName = entry["lastname"]!!,
                title = entry["title"]!!,
                phoneNumber = entry["phone_number"],
                address = CreateAddress(
                    recipientName = entry["recipient_name"]!!,
                    organizationName = entry["organization_name"],
                    addressLine1 =  entry["address_line_1"]!!,
                    addressLine2 = entry["address_line_2"]!!,
                    city = entry["city"]!!,
                    stateOrProvince = entry["state_or_province"]!!,
                    postalCode = entry["postal_code"]!!,
                    countryCode = entry["country_code"]!!
                )
            )
        }

        assertEquals(10, userProfiles.size)

        assertEquals(userProfiles[0], UserProfileToImport(
            username = "test-user_01@solyton.org",
            firstName ="Max",
            lastName = "Mustermann",
            title = "Dr.",
            phoneNumber = "017522299944",
            CreateAddress(
                recipientName = "Max Mustermann",
                organizationName = "Solyton GmbH" ,
                addressLine1 = "Musterstraße 1",
                addressLine2 = "",
                city = "Berlin",
                stateOrProvince = "Berlin",
                postalCode = "10115",
                countryCode = "DE"
            )
        ))
    }
}
