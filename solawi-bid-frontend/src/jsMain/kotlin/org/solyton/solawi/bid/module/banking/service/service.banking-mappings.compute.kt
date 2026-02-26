package org.solyton.solawi.bid.module.banking.service

import org.solyton.solawi.bid.module.banking.data.BIC
import org.solyton.solawi.bid.module.banking.data.IBAN
import org.solyton.solawi.bid.module.banking.data.api.ImportBankAccount
import org.solyton.solawi.bid.module.banking.data.mappings.BankingMappings
import org.solyton.solawi.bid.module.shared.parser.csv.toColumnType
import org.solyton.solawi.bid.module.values.Username

@Suppress("UnusedParameter")
fun computeBankAccountsDataForImport(
    typedDataMaps: List<Map<String, Map<String, String>>>,
    bankingMappings: BankingMappings
): List<ImportBankAccount> {
     return typedDataMaps.map {
        val userProfiles = requireNotNull( it["user_profiles"] ) { "User profiles are empty" }
        it.filterKeys { key -> key.startsWith("banking") }.map { (key, value) ->
            val (name, type, keyOfShareType) = key.toColumnType()
            requireNotNull(name) { "Part name is null: $key" }
            requireNotNull(type) { "Part type is null: $key" }

            val usernameRaw = requireNotNull( userProfiles["username"] ) { "No username provided" }
            val ibanRaw = requireNotNull(value["iban"]) { "No iban provided" }
            val bicRaw = requireNotNull(value["bic"]) { "No bic provided" }

            val username = Username(usernameRaw)
            val iban = IBAN(ibanRaw)
            val bic = BIC(bicRaw)

            ImportBankAccount(
                username,
                bic,
                iban
            )
        }
    }.flatten()
}
