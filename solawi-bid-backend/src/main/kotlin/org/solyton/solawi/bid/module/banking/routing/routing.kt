package org.solyton.solawi.bid.module.banking.routing

import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.evoleq.exposedx.data.DbEnv
import org.evoleq.ktorx.Base
import org.evoleq.ktorx.NotImplemented
import org.evoleq.ktorx.ReceiveContextual
import org.evoleq.ktorx.Respond
import org.evoleq.ktorx.data.KTorEnv
import org.evoleq.math.state.runOn
import org.evoleq.math.state.times
import org.solyton.solawi.bid.module.banking.action.*
import org.solyton.solawi.bid.module.banking.data.api.*
import org.solyton.solawi.bid.module.permission.action.db.IsGranted
import org.solyton.solawi.bid.module.permission.action.db.no

fun <BankingEnv> Routing.banking (
    environment: BankingEnv,
    authenticate: Routing.(Route.() -> Route)-> Route
) where BankingEnv : KTorEnv, BankingEnv : DbEnv {
    val transform = environment.transformException
    authenticate {
        route("banking") {
            route("fiscal-years") {
                get("all") {
                    @Suppress("UnsafeCallOnNullableType")
                    ReceiveContextual<String>{
                        parameters -> parameters["legal_entity"]!!
                    } *
                    IsGranted("READ_FISCAL_YEARS") *
                    ReadFiscalYearsByLegalEntity() *
                    Respond{ transform() } runOn Base(call, environment)
                }
                post("create") {
                    ReceiveContextual<CreateFiscalYear>() *
                    IsGranted("CREATE_FISCAL_YEAR") *
                    CreateFiscalYear() *
                    Respond{ transform() } runOn Base(call, environment)
                }
                patch("update") {
                    ReceiveContextual<UpdateFiscalYear>() *
                    IsGranted("UPDATE_FISCAL_YEAR") *
                    UpdateFiscalYear() *
                    Respond{ transform() } runOn Base(call, environment)
                }
            }
            route("bank-accounts") {
                get("all"){
                    @Suppress("UnsafeCallOnNullableType")
                    ReceiveContextual<String>{
                        parameters -> parameters["legal_entity"]!!
                    } *
                    IsGranted("READ_BANK_ACCOUNTS", no) *
                    ReadBankAccountsByLegalEntity() *
                    Respond{ transform() } runOn Base(call, environment)
                }
                post("create") {
                    ReceiveContextual<CreateBankAccount>() *
                    IsGranted("CREATE_BANK_ACCOUNT", no) *
                    CreateBankAccount() *
                    Respond{ transform() } runOn Base(call, environment)
                }
                patch("update") {
                    ReceiveContextual<UpdateBankAccount>() *
                    IsGranted("UPDATE_BANK_ACCOUNT", no) *
                    UpdateBankAccount() *
                    Respond{ transform() } runOn Base(call, environment)
                }
                post("import") {
                    ReceiveContextual<ImportBankAccounts>() *
                    IsGranted("IMPORT_BANK_ACCOUNTS", no) *
                    ImportBankAccounts() *
                    Respond{ transform() } runOn Base(call, environment)
                }
                delete("delete") {
                    ReceiveContextual<DeleteBankAccount>() *
                    IsGranted("DELETE_BANK_ACCOUNTS", no) *
                    DeleteBankAccount() *
                    Respond{ transform() } runOn Base(call, environment)
                }
            }
        }
    }
}
