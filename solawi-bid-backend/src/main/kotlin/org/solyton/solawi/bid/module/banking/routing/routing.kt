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
import org.solyton.solawi.bid.module.banking.permissions.BankAccounts.Rights.CREATE_BANK_ACCOUNTS
import org.solyton.solawi.bid.module.banking.permissions.BankAccounts.Rights.DELETE_BANK_ACCOUNTS
import org.solyton.solawi.bid.module.banking.permissions.BankAccounts.Rights.IMPORT_BANK_ACCOUNTS
import org.solyton.solawi.bid.module.banking.permissions.BankAccounts.Rights.READ_BANK_ACCOUNTS
import org.solyton.solawi.bid.module.banking.permissions.BankAccounts.Rights.UPDATE_BANK_ACCOUNTS
import org.solyton.solawi.bid.module.banking.permissions.CreditorIdentifiers.Rights.READ_CREDITOR_IDENTIFIERS
import org.solyton.solawi.bid.module.banking.permissions.FiscalYears.Rights.CREATE_FISCAL_YEARS
import org.solyton.solawi.bid.module.banking.permissions.FiscalYears.Rights.READ_FISCAL_YEARS
import org.solyton.solawi.bid.module.banking.permissions.FiscalYears.Rights.UPDATE_FISCAL_YEARS
import org.solyton.solawi.bid.module.banking.permissions.LegalEntities.Rights.CREATE_LEGAL_ENTITIES
import org.solyton.solawi.bid.module.banking.permissions.LegalEntities.Rights.READ_LEGAL_ENTITIES
import org.solyton.solawi.bid.module.banking.permissions.Sepa.Rights.CREATE_SEPA_COLLECTIONS
import org.solyton.solawi.bid.module.banking.permissions.Sepa.Rights.CREATE_SEPA_MANDATES
import org.solyton.solawi.bid.module.banking.permissions.Sepa.Rights.CREATE_SEPA_MESSAGES
import org.solyton.solawi.bid.module.banking.permissions.Sepa.Rights.CREATE_SEPA_PAYMENTS
import org.solyton.solawi.bid.module.banking.permissions.Sepa.Rights.READ_SEPA_COLLECTIONS
import org.solyton.solawi.bid.module.banking.permissions.Sepa.Rights.READ_SEPA_MANDATES
import org.solyton.solawi.bid.module.banking.permissions.Sepa.Rights.UPDATE_SEPA_COLLECTIONS
import org.solyton.solawi.bid.module.banking.permissions.Sepa.Rights.UPDATE_SEPA_MANDATES
import org.solyton.solawi.bid.module.permission.action.db.IsGranted
import org.solyton.solawi.bid.module.permission.action.db.no

fun <BankingEnv> Routing.banking (
    environment: BankingEnv,
    authenticate: Routing.(Route.() -> Route)-> Route
) where BankingEnv : KTorEnv, BankingEnv : DbEnv {
    val transform = environment.transformException
    authenticate {
        route("banking") {
            route("legal-entities") {
                route("personal") {
                    @Suppress("UnsafeCallOnNullableType")
                    get("") {
                        ReceiveContextual<String> { parameters ->
                            parameters["party"]!!
                        } *
                        IsGranted(READ_LEGAL_ENTITIES, no) *
                        ReadLegalEntity() *
                        Respond { transform() } runOn Base(call, environment)
                    }
                    post("create") {
                        ReceiveContextual<CreateLegalEntity>() *
                        IsGranted(CREATE_LEGAL_ENTITIES, no) *
                        CreateLegalEntity() *
                        Respond { transform() } runOn Base(call, environment)
                    }
                    patch("update") {
                        ReceiveContextual<UpdateLegalEntity>() *
                        IsGranted(CREATE_LEGAL_ENTITIES, no) *
                        UpdateLegalEntity() *
                        Respond { transform() } runOn Base(call, environment)
                    }
                    delete("delete") {
                        NotImplemented("") * Respond {transform()} runOn Base(call, environment)
                    }
                }
            }
            route("fiscal-years") {
                get("all") {
                    @Suppress("UnsafeCallOnNullableType")
                    ReceiveContextual<String>{
                        parameters -> parameters["legal_entity"]!!
                    } *
                    IsGranted(READ_FISCAL_YEARS, no) *
                    ReadFiscalYearsByLegalEntity() *
                    Respond{ transform() } runOn Base(call, environment)
                }
                post("create") {
                    ReceiveContextual<CreateFiscalYear>() *
                    IsGranted(CREATE_FISCAL_YEARS, no) *
                    CreateFiscalYear() *
                    Respond{ transform() } runOn Base(call, environment)
                }
                patch("update") {
                    ReceiveContextual<UpdateFiscalYear>() *
                    IsGranted(UPDATE_FISCAL_YEARS, no) *
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
                    IsGranted(READ_BANK_ACCOUNTS, no) *
                    ReadBankAccountsByLegalEntity() *
                    Respond{ transform() } runOn Base(call, environment)
                }
                post("create") {
                    ReceiveContextual<CreateBankAccount>() *
                    IsGranted(CREATE_BANK_ACCOUNTS, no) *
                    CreateBankAccount() *
                    Respond{ transform() } runOn Base(call, environment)
                }
                patch("update") {
                    ReceiveContextual<UpdateBankAccount>() *
                    IsGranted(UPDATE_BANK_ACCOUNTS, no) *
                    UpdateBankAccount() *
                    Respond{ transform() } runOn Base(call, environment)
                }
                post("import") {
                    ReceiveContextual<ImportBankAccounts>() *
                    IsGranted(IMPORT_BANK_ACCOUNTS, no) *
                    ImportBankAccounts() *
                    Respond{ transform() } runOn Base(call, environment)
                }
                delete("delete") {
                    ReceiveContextual<DeleteBankAccount>() *
                    IsGranted(DELETE_BANK_ACCOUNTS, no) *
                    DeleteBankAccount() *
                    Respond{ transform() } runOn Base(call, environment)
                }
                route("personal") {
                    get("/") {
                        NotImplemented("") * Respond { transform() } runOn Base(call, environment)
                    }
                    post("create") {
                        NotImplemented("") * Respond { transform() } runOn Base(call, environment)
                    }
                    patch("update") {
                        NotImplemented("") * Respond { transform() } runOn Base(call, environment)
                    }
                    delete("delete") {
                        NotImplemented("") * Respond { transform() } runOn Base(call, environment)
                    }
                }
            }
            route("creditors") {
                route("identifiers") {
                    get("by-legal-entity"){
                        ReceiveContextual<String>{
                            parameters -> requireNotNull(parameters["legal_entity"]) {
                                "Parameter 'legal_entity' is empty"
                            }
                        } *
                        IsGranted(READ_CREDITOR_IDENTIFIERS, no) *
                        ReadCreditorIdentifierByLegalEntity() *
                        Respond{ transform() } runOn Base(call, environment)
                    }
                }
            }

            route("sepa") {
                route("mandates") {
                    post("create") {
                        ReceiveContextual<CreateSepaMandate>() *
                        IsGranted(CREATE_SEPA_MANDATES, no) *
                        CreateSepaMandate() *
                        Respond{ transform() } runOn Base(call, environment)
                    }
                    get("by-creditors-legal-entity") {
                        ReceiveContextual<String>{
                            parameters -> requireNotNull(parameters["legal_entity"]) {
                                "Parameter 'legal_entity' is empty"
                            }
                        } *
                        IsGranted(READ_SEPA_MANDATES, no) *
                        ReadSepaMandatesByCreditorsLegalEntity() *
                        Respond { transform() } runOn Base(call, environment)
                    }
                    patch("update") {
                        ReceiveContextual<UpdateSepaMandate>() *
                        IsGranted(UPDATE_SEPA_MANDATES, no) *
                        UpdateSepaMandate() *
                        Respond{ transform() } runOn Base(call, environment)

                    }
                }
                route("collections"){
                    get("by-legal-entity") {
                        ReceiveContextual<String>{
                            parameters -> requireNotNull(parameters["legal_entity"]) {
                                "Parameter 'legal_entity' is empty"
                            }
                        } *
                        IsGranted(READ_SEPA_COLLECTIONS, no) *
                        ReadSepaCollectionsByLegalEntity() *
                        Respond { transform() } runOn Base(call, environment)
                    }
                    post("create") {
                        ReceiveContextual<CreateSepaCollection>() *
                        IsGranted(CREATE_SEPA_COLLECTIONS, no) *
                        CreateSepaCollection() *
                        Respond { transform() } runOn Base(call, environment)
                    }
                    post("create-payments"){
                        ReceiveContextual<CreateSepaPaymentsForCollection>() *
                        IsGranted(CREATE_SEPA_PAYMENTS, no) *
                        CreateSepaPaymentsForCollection() *
                        Respond { transform() } runOn Base(call, environment)
                    }
                    post("generate-sepa-message") {
                        ReceiveContextual<GenerateSepaMessageForCollection>() *
                        IsGranted(CREATE_SEPA_MESSAGES, no) *
                        GenerateSepaMessageForCollection() *
                        Respond { transform() } runOn Base(call, environment)
                    }
                    patch("update") {
                        ReceiveContextual<UpdateSepaCollection>() *
                        IsGranted(UPDATE_SEPA_COLLECTIONS, no) *
                        UpdateSepaCollection() *
                        Respond { transform() } runOn Base(call, environment)
                    }
                    delete("delete") {
                        NotImplemented("Sepa collection deletion is not implemented yet")
                    }

                }
                route("payments"){
                    post("create") {

                    }
                }
            }
        }
    }
}
