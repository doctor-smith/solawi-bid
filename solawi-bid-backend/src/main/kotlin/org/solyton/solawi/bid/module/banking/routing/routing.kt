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
import org.evoleq.uuid.toUuid
import org.solyton.solawi.bid.module.application.repository.contextIdOf
import org.solyton.solawi.bid.module.banking.action.*
import org.solyton.solawi.bid.module.banking.application.BANKING_APPLICATION_NAME
import org.solyton.solawi.bid.module.banking.data.api.*
import org.solyton.solawi.bid.module.banking.permissions.BankAccounts.Rights.CREATE_BANK_ACCOUNTS
import org.solyton.solawi.bid.module.banking.permissions.BankAccounts.Rights.DELETE_BANK_ACCOUNTS
import org.solyton.solawi.bid.module.banking.permissions.BankAccounts.Rights.IMPORT_BANK_ACCOUNTS
import org.solyton.solawi.bid.module.banking.permissions.BankAccounts.Rights.READ_BANK_ACCOUNTS
import org.solyton.solawi.bid.module.banking.permissions.BankAccounts.Rights.UPDATE_BANK_ACCOUNTS
import org.solyton.solawi.bid.module.banking.permissions.CreditorIdentifiers.Rights.CREATE_CREDITOR_IDENTIFIERS
import org.solyton.solawi.bid.module.banking.permissions.CreditorIdentifiers.Rights.READ_CREDITOR_IDENTIFIERS
import org.solyton.solawi.bid.module.banking.permissions.CreditorIdentifiers.Rights.UPDATE_CREDITOR_IDENTIFIERS
import org.solyton.solawi.bid.module.banking.permissions.FiscalYears.Rights.CREATE_FISCAL_YEARS
import org.solyton.solawi.bid.module.banking.permissions.FiscalYears.Rights.READ_FISCAL_YEARS
import org.solyton.solawi.bid.module.banking.permissions.FiscalYears.Rights.UPDATE_FISCAL_YEARS
import org.solyton.solawi.bid.module.banking.permissions.LegalEntities.Rights.CREATE_LEGAL_ENTITIES
import org.solyton.solawi.bid.module.banking.permissions.LegalEntities.Rights.READ_LEGAL_ENTITIES
import org.solyton.solawi.bid.module.banking.permissions.LegalEntities.Rights.UPDATE_LEGAL_ENTITIES
import org.solyton.solawi.bid.module.banking.permissions.Sepa.Rights.CREATE_SEPA_COLLECTIONS
import org.solyton.solawi.bid.module.banking.permissions.Sepa.Rights.CREATE_SEPA_MANDATES
import org.solyton.solawi.bid.module.banking.permissions.Sepa.Rights.CREATE_SEPA_MESSAGES
import org.solyton.solawi.bid.module.banking.permissions.Sepa.Rights.CREATE_SEPA_PAYMENTS
import org.solyton.solawi.bid.module.banking.permissions.Sepa.Rights.DELETE_SEPA_PAYMENTS
import org.solyton.solawi.bid.module.banking.permissions.Sepa.Rights.READ_SEPA_COLLECTIONS
import org.solyton.solawi.bid.module.banking.permissions.Sepa.Rights.READ_SEPA_MANDATES
import org.solyton.solawi.bid.module.banking.permissions.Sepa.Rights.READ_SEPA_MESSAGES
import org.solyton.solawi.bid.module.banking.permissions.Sepa.Rights.READ_SEPA_PAYMENTS
import org.solyton.solawi.bid.module.banking.permissions.Sepa.Rights.UPDATE_SEPA_COLLECTIONS
import org.solyton.solawi.bid.module.banking.permissions.Sepa.Rights.UPDATE_SEPA_MANDATES
import org.solyton.solawi.bid.module.banking.permissions.Sepa.Rights.UPDATE_SEPA_MESSAGES
import org.solyton.solawi.bid.module.banking.permissions.Sepa.Rights.UPDATE_SEPA_PAYMENTS
import org.solyton.solawi.bid.module.permission.action.db.*
import org.solyton.solawil.bid.module.bid.data.api.toUUID

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
                        IsGrantedInDerivedContext(
                            READ_LEGAL_ENTITIES,
                            yes
                        ){
                            contextIdOf(
                                BANKING_APPLICATION_NAME
                            ) {
                                it.data.toUuid()
                            }
                        } *
                        ReadLegalEntity() *
                        Respond { transform() } runOn Base(call, environment)
                    }
                    post("create") {
                        ReceiveContextual<CreateLegalEntity>() *
                        IsGrantedInDerivedContext(
                            CREATE_LEGAL_ENTITIES,
                            yes
                        ){
                            contextIdOf(
                                BANKING_APPLICATION_NAME
                            ) {
                                it.data.partyId.value.toUuid()
                            }
                        } *
                        CreateLegalEntity() *
                        Respond { transform() } runOn Base(call, environment)
                    }
                    patch("update") {
                        ReceiveContextual<UpdateLegalEntity>() *
                        IsGrantedInDerivedContext(
                            UPDATE_LEGAL_ENTITIES,
                            yes
                        ){
                            contextIdOf(
                                BANKING_APPLICATION_NAME
                            ) {
                                it.data.partyId.value.toUuid()
                            }
                        } *
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
                    IsGrantedInDerivedContext(
                        READ_FISCAL_YEARS, yes
                    ) {
                        contextIdOf(BANKING_APPLICATION_NAME) {
                            it.data.toUuid()
                        }
                    } *
                    ReadFiscalYearsByLegalEntity() *
                    Respond{ transform() } runOn Base(call, environment)
                }
                post("create") {
                    ReceiveContextual<CreateFiscalYear>() *
                    IsGrantedInDerivedContext(
                        CREATE_FISCAL_YEARS, yes
                    ) {
                        contextIdOf(
                            BANKING_APPLICATION_NAME
                        ) {
                            it.data.legalEntityId.toUuid()
                        }
                    } *
                    CreateFiscalYear() *
                    Respond{ transform() } runOn Base(call, environment)
                }
                patch("update") {
                    ReceiveContextual<UpdateFiscalYear>() *
                    IsGrantedInDerivedContext(
                        UPDATE_FISCAL_YEARS, yes
                    ) {
                        contextIdOf(BANKING_APPLICATION_NAME) {
                            it.data.legalEntityId.toUuid()
                        }
                    } *
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
                    IsGrantedInDerivedContext(
                        READ_BANK_ACCOUNTS,
                        yes
                    ) {
                        contextIdOf(BANKING_APPLICATION_NAME) {
                            it.data.toUuid()
                        }
                    } *
                    ReadBankAccountsByLegalEntity() *
                    Respond{ transform() } runOn Base(call, environment)
                }
                post("create") {
                    ReceiveContextual<CreateBankAccount>() *
                    IsGrantedInSpecialContext(CREATE_BANK_ACCOUNTS) {
                        contextual -> contextual.userId != contextual.data.userId.toUUID()
                    } *
                    CreateBankAccount() *
                    Respond{ transform() } runOn Base(call, environment)
                }
                patch("update") {
                    ReceiveContextual<UpdateBankAccount>() *
                    IsGrantedInSpecialContext(UPDATE_BANK_ACCOUNTS) {
                        contextual -> contextual.userId != contextual.data.userId.toUUID()
                    } *
                    UpdateBankAccount() *
                    Respond{ transform() } runOn Base(call, environment)
                }
                post("import") {
                    ReceiveContextual<ImportBankAccounts>() *
                    IsGrantedInDerivedContext(
                        IMPORT_BANK_ACCOUNTS,
                        yes
                    ) {
                        contextIdOf(BANKING_APPLICATION_NAME){
                            it.data.accessorId.value.toUuid()
                        }
                    } *
                    ImportBankAccounts() *
                    Respond{ transform() } runOn Base(call, environment)
                }
                // TODO(correct context needs to be provided)
                delete("delete") {
                    ReceiveContextual<DeleteBankAccount>() *
                    IsGranted(DELETE_BANK_ACCOUNTS, no) *
                    DeleteBankAccount() *
                    Respond{ transform() } runOn Base(call, environment)
                }
                route("personal") {
                    // TODO(correct context needs to be provided)
                    get("/all") {
                        ReceiveContextual<String>{
                            _ -> ""
                        } *
                        IsGranted(READ_BANK_ACCOUNTS, no) *
                        ReadPersonalBankAccounts() *
                        Respond { transform() } runOn Base(call, environment)
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
                        IsGrantedInDerivedContext(
                            READ_CREDITOR_IDENTIFIERS,
                            yes
                        ) {
                            contextIdOf(BANKING_APPLICATION_NAME) {
                                it.data.toUuid()
                            }
                        } *
                        ReadCreditorIdentifierByLegalEntity() *
                        Respond{ transform() } runOn Base(call, environment)
                    }
                    post("create") {
                        ReceiveContextual<CreateCreditorIdentifier>() *
                        IsGrantedInDerivedContext(
                            CREATE_CREDITOR_IDENTIFIERS,
                            yes
                        ) {
                            contextIdOf(BANKING_APPLICATION_NAME) {
                                it.data.legalEntityId.value.toUuid()
                            }
                        } *
                        CreateCreditorIdentifier() *
                        Respond{ transform() } runOn Base(call, environment)
                    }
                    patch("update") {
                        ReceiveContextual<UpdateCreditorIdentifier>() *
                        IsGrantedInDerivedContext(
                            UPDATE_CREDITOR_IDENTIFIERS,
                            yes
                        ) {
                            contextIdOf(BANKING_APPLICATION_NAME) {
                                it.data.legalEntityId.value.toUuid()
                            }
                        } *
                        UpdateCreditorIdentifier() *
                        Respond{ transform() } runOn Base(call, environment)
                    }
                }
            }

            route("sepa") {
                route("mandates") {
                    // TODO(correct context needs to be provided)
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
                    // TODO(correct context needs to be provided)
                    patch("update") {
                        ReceiveContextual<UpdateSepaMandate>() *
                        IsGranted(UPDATE_SEPA_MANDATES, no) *
                        UpdateSepaMandate() *
                        Respond{ transform() } runOn Base(call, environment)

                    }
                    route("personal") {
                        // TODO(correct context needs to be provided)
                        get("all") {
                            ReceiveContextual<String>{
                                _-> ""
                            } *
                            ReadPersonalSepaMandates() *
                            Respond { transform() } runOn Base(call, environment)
                        }
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
                    // TODO(correct context needs to be provided)
                    post("create") {
                        ReceiveContextual<CreateSepaCollection>() *
                        IsGranted(CREATE_SEPA_COLLECTIONS, no) *
                        CreateSepaCollection() *
                        Respond { transform() } runOn Base(call, environment)
                    }
                    // TODO(correct context needs to be provided)
                    post("create-payments"){
                        ReceiveContextual<CreateSepaPaymentsForCollection>() *
                        IsGranted(CREATE_SEPA_PAYMENTS, no) *
                        CreateSepaPaymentsForCollection() *
                        Respond { transform() } runOn Base(call, environment)
                    }
                    // TODO(correct context needs to be provided)
                    post("create-payment-successors") {
                        ReceiveContextual<CreateSepaPaymentSuccessors>() *
                        IsGranted(CREATE_SEPA_PAYMENTS, no) *
                        CreateSepaPaymentSuccessors() *
                        Respond { transform() } runOn Base(call, environment)
                    }
                    // TODO(correct context needs to be provided)
                    post("generate-sepa-message") {
                        ReceiveContextual<GenerateSepaMessageForCollection>() *
                        IsGranted(CREATE_SEPA_MESSAGES, no) *
                        GenerateSepaMessageForCollection() *
                        Respond { transform() } runOn Base(call, environment)
                    }
                    // TODO(correct context needs to be provided)
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
                // TODO(correct context needs to be provided)
                route("payments"){
                    post("create") {
                        NotImplemented("Sepa collection creation is not implemented yet")
                    }
                    post("create-ad-hoc") {
                        ReceiveContextual<CreateAdHocSepaPayment>() *
                        IsGranted(UPDATE_SEPA_PAYMENTS, no) *
                        CreateAdHocSepaPayment() *
                        Respond { transform() } runOn Base(call, environment)
                    }
                    patch("update-execution-statuses") {
                        ReceiveContextual<UpdateSepaPaymentExecutionStatuses>() *
                        IsGranted(UPDATE_SEPA_PAYMENTS, no) *
                        UpdateSepaPaymentExecutionStatuses() *
                        Respond { transform() } runOn Base(call, environment)
                    }
                    patch("update") {
                        ReceiveContextual<UpdateSepaPayment>() *
                        IsGranted(UPDATE_SEPA_PAYMENTS, no) *
                        UpdateSepaPayment() *
                        Respond { transform() } runOn Base(call, environment)
                    }
                    patch("update-many") {
                        ReceiveContextual<UpdateSepaPayments>() *
                        IsGranted(UPDATE_SEPA_PAYMENTS, no) *
                        UpdateSepaPayments() *
                        Respond { transform() } runOn Base(call, environment)
                    }
                    delete("delete") {
                        ReceiveContextual<DeleteSepaPayment>() *
                        IsGranted(DELETE_SEPA_PAYMENTS, no) *
                        DeleteSepaPayment() *
                        Respond { transform() } runOn Base(call, environment)
                    }
                    delete("delete-many") {
                        ReceiveContextual<DeleteSepaPayments>() *
                        IsGranted(DELETE_SEPA_PAYMENTS, no) *
                        DeleteSepaPayments() *
                        Respond { transform() } runOn Base(call, environment)
                    }
                }
                // TODO(correct context needs to be provided)
                route("payment-links") {
                    get("by-legal-entity") {
                        ReceiveContextual<String>{
                            parameters -> requireNotNull(parameters["legal_entity"]) {
                                "Parameter 'legal_entity' is empty"
                            }
                        } *
                        IsGranted(READ_SEPA_PAYMENTS, no) *
                        ReadSepaPaymentLinksByLegalEntity() *
                        Respond { transform() } runOn Base(call, environment)
                    }
                    get("personal") {
                        ReceiveContextual<Unit>{} *
                        IsGranted(READ_SEPA_PAYMENTS, no) *
                        ReadPersonalSepaPaymentLinks() *
                        Respond { transform() } runOn Base(call, environment)
                    }
                }
                // TODO(correct context needs to be provided)
                route("messages") {
                    get("download") {
                        ReceiveContextual<String>{
                                parameters -> requireNotNull(parameters["message_id"]) {
                            "Parameter 'message_id' is empty"
                        }
                        } *
                        IsGranted(READ_SEPA_MESSAGES, no) *
                        DownloadSepaMessage() *
                        Respond { transform() } runOn Base(call, environment)
                    }
                    get("by-legal-entity") {
                        ReceiveContextual<String>{
                            parameters -> requireNotNull(parameters["legal_entity"]) {
                                "Parameter 'legal_entity' is empty"
                            }
                        } *
                        IsGranted(READ_SEPA_MESSAGES, no) *
                        ReadSepaMessagesByLegalEntityId() *
                        Respond { transform() } runOn Base(call, environment)
                    }
                    patch("update-status") {
                        ReceiveContextual<UpdateSepaMessageStatus>() *
                        IsGranted(UPDATE_SEPA_MESSAGES, no) *
                        UpdateSepaMessageStatus() *
                        Respond { transform() } runOn Base(call, environment)
                    }
                    post("merge") {
                        ReceiveContextual<MergeSepaMessages>() *
                        IsGranted(UPDATE_SEPA_MESSAGES, no) *
                        MergeSepaMessages() *
                        Respond { transform() } runOn Base(call, environment)
                    }
                }
            }
        }
    }
}
