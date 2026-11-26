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
import org.evoleq.math.x
import org.evoleq.uuid.toUuid
import org.solyton.solawi.bid.module.application.repository.contextIdOf
import org.solyton.solawi.bid.module.banking.action.*
import org.solyton.solawi.bid.module.banking.application.BANKING_APPLICATION_NAME
import org.solyton.solawi.bid.module.banking.data.SepaMessageId
import org.solyton.solawi.bid.module.banking.data.api.*
import org.solyton.solawi.bid.module.banking.exception.SepaException
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
import org.solyton.solawi.bid.module.banking.repository.*
import org.solyton.solawi.bid.module.banking.service.validatedBankAccount
import org.solyton.solawi.bid.module.permission.action.db.*
import org.solyton.solawi.bid.module.values.LegalEntityId
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
                    ReceiveContextual<List<String>>{
                        parameters -> parameters.getAll("legal_entity")!!
                    } *
                    IsGrantedInAllDerivedContexts(READ_FISCAL_YEARS) { contextual ->
                        contextual.data.map { legalEntityId ->
                            val partyId = validatedLegalEntity(legalEntityId.toUuid()).partyId
                            contextIdOf(BANKING_APPLICATION_NAME) { partyId }
                        }.toSet()
                    } *
                    ReadFiscalYearsByLegalEntity() *
                    Respond{ transform() } runOn Base(call, environment)
                }
                post("create") {
                    ReceiveContextual<CreateFiscalYear>() *
                    IsGrantedInDerivedContext(
                        CREATE_FISCAL_YEARS, yes
                    ) { contextual ->
                        val partyId = validatedLegalEntity(contextual.data.legalEntityId.toUuid()).partyId
                        contextIdOf(BANKING_APPLICATION_NAME) { partyId }
                    } *
                    CreateFiscalYear() *
                    Respond{ transform() } runOn Base(call, environment)
                }
                patch("update") {
                    ReceiveContextual<UpdateFiscalYear>() *
                    IsGrantedInAllDerivedContexts(UPDATE_FISCAL_YEARS) { contextual ->
                        val data = contextual.data
                        val sourceLegalEntityId = validatedFiscalYear(data.id.toUuid()).legalEntityId
                        val sourcePartyId = validatedLegalEntity(sourceLegalEntityId).partyId
                        val targetPartyId = validatedLegalEntity(data.legalEntityId.toUuid()).partyId
                        setOf(sourcePartyId, targetPartyId).map { partyId ->
                            contextIdOf(BANKING_APPLICATION_NAME) { partyId }
                        }.toSet()
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
                delete("delete") {
                    ReceiveContextual<DeleteBankAccount>() *
                    IsGrantedInDerivedContext(DELETE_BANK_ACCOUNTS) { contextual ->
                        val ownerId = validatedBankAccount(contextual.data.id.value.toUuid()).userId
                        contextIdOf(BANKING_APPLICATION_NAME) { ownerId }
                    } *
                    DeleteBankAccount() *
                    Respond{ transform() } runOn Base(call, environment)
                }
                route("personal") {
                    get("/all") {
                        ReceiveContextual<String>{ "" } *
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
                        ReceiveContextual<Pair<String, String>>{
                            parameters ->
                            val legalEntityId = requireNotNull(parameters["legal_entity"]) {
                                "Parameter 'legal_entity' is empty"
                            }
                            val providerId = requireNotNull(parameters["provider"]) {
                                "Parameter 'provider' is empty"
                            }
                            legalEntityId x providerId
                        } *
                        IsGrantedInDerivedContext(
                            READ_CREDITOR_IDENTIFIERS,
                            yes
                        ) {
                            contextIdOf(BANKING_APPLICATION_NAME) {
                                it.data.second.toUuid()
                            }
                        }  *
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
                                it.data.providerId.value.toUuid()
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
                                it.data.providerId.value.toUuid()
                            }
                        } *
                        UpdateCreditorIdentifier() *
                        Respond{ transform() } runOn Base(call, environment)
                    }
                }
            }

            route("sepa") {
                route("mandates") {
                    post("create") {
                        ReceiveContextual<CreateSepaMandate>() *
                        IsGrantedOneOfInAllDerivedContexts(
                            rights(CREATE_SEPA_MANDATES, "MANAGE_BANK_TRANSACTIONS")
                        ) { contextual ->
                            val data = contextual.data
                            val collection = data.collectionId?.let { collectionId ->
                                validatedSepaCollection(collectionId.value.toUuid())
                            }
                            val creditorPartyId = collection?.creditorIdentifier?.legalEntity?.partyId
                                ?: validatedCreditorIdentifier(data.creditorId).legalEntity.partyId
                            val amendmentPartyId = data.amendmentOf?.let { mandateId ->
                                validatedSepaMandate(mandateId.value.toUuid()).creditorIdentifier.legalEntity.partyId
                            }
                            val collectionPartyId = collection?.creditorIdentifier?.legalEntity?.partyId
                            setOfNotNull(creditorPartyId, amendmentPartyId, collectionPartyId).map { partyId ->
                                contextIdOf(BANKING_APPLICATION_NAME) { partyId }
                            }.toSet()
                        } *
                        CreateSepaMandate() *
                        Respond{ transform() } runOn Base(call, environment)
                    }
                    get("by-creditors-legal-entity") {
                        ReceiveContextual<String>{
                            parameters -> requireNotNull(parameters["legal_entity"]) {
                                "Parameter 'legal_entity' is empty"
                            }
                        } *
                        IsGrantedInDerivedContext(READ_SEPA_MANDATES) { contextual ->
                            val partyId = validatedLegalEntity(contextual.data.toUuid()).partyId
                            contextIdOf(BANKING_APPLICATION_NAME) { partyId }
                        } *
                        ReadSepaMandatesByCreditorsLegalEntity() *
                        Respond { transform() } runOn Base(call, environment)
                    }
                    patch("update") {
                        ReceiveContextual<UpdateSepaMandate>() *
                        IsGrantedOneOfInAllDerivedContexts(
                            rights(UPDATE_SEPA_MANDATES, "MANAGE_BANK_TRANSACTIONS")
                        ) { contextual ->
                            val data = contextual.data
                            val sourcePartyId = validatedSepaMandate(data.sepaMandateId.value.toUuid())
                                .creditorIdentifier.legalEntity.partyId
                            val targetPartyId = validatedCreditorIdentifier(data.creditorId).legalEntity.partyId
                            setOf(sourcePartyId, targetPartyId).map { partyId ->
                                contextIdOf(BANKING_APPLICATION_NAME) { partyId }
                            }.toSet()
                        } *
                        UpdateSepaMandate() *
                        Respond{ transform() } runOn Base(call, environment)

                    }
                    patch("attach-reference-data") {
                        ReceiveContextual<AttachSepaMandateReferenceData>() *
                        IsGrantedOneOfInAllDerivedContexts(
                            rights(UPDATE_SEPA_MANDATES, "MANAGE_BANK_TRANSACTIONS")
                        ) { contextual ->
                            val data = contextual.data
                            val mandatePartyId = validatedSepaMandate(data.sepaMandateId.value.toUuid())
                                .creditorIdentifier.legalEntity.partyId
                            val collectionPartyId = data.collectionId?.let { collectionId ->
                                validatedSepaCollection(collectionId.value.toUuid()).creditorIdentifier.legalEntity.partyId
                            }
                            setOfNotNull(mandatePartyId, collectionPartyId).map { partyId ->
                                contextIdOf(BANKING_APPLICATION_NAME) { partyId }
                            }.toSet()
                        } *
                        AttachSepaMandateReferenceData() *
                        Respond<SepaMandate>{ transform() } runOn Base(call, environment)
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
                        ReceiveContextual<LegalEntityId> { parameters ->
                            LegalEntityId(requireNotNull(parameters["legal_entity"]) {
                                "Parameter 'legal_entity' is empty"
                            })
                        } *
                        IsGrantedInDerivedContext(READ_SEPA_COLLECTIONS) { contextual ->
                            val partyId = validatedLegalEntity(contextual.data.value.toUuid()).partyId
                            contextIdOf(BANKING_APPLICATION_NAME) { partyId }
                        } *
                        ReadSepaCollectionsByLegalEntity() *
                        Respond { transform() } runOn Base(call, environment)
                    }
                    post("create") {
                        ReceiveContextual<CreateSepaCollection>() *
                        IsGrantedOneOfInAllDerivedContexts(
                            rights(CREATE_SEPA_COLLECTIONS, "MANAGE_BANK_TRANSACTIONS")
                        ) { contextual ->
                            val data = contextual.data
                            val creditorPartyId = validatedCreditorIdentifier(
                                data.creditorIdentifierId.value.toUuid()
                            ).legalEntity.partyId
                            val accountOwnerId = validatedBankAccount(
                                data.creditorBankAccountId.value.toUuid()
                            ).userId
                            setOf(creditorPartyId, accountOwnerId).map { partyId ->
                                contextIdOf(BANKING_APPLICATION_NAME) { partyId }
                            }.toSet()
                        } *
                        CreateSepaCollection() *
                        Respond { transform() } runOn Base(call, environment)
                    }
                    post("create-payments"){
                        ReceiveContextual<CreateSepaPaymentsForCollection>() *
                        IsGrantedOneOfInAllDerivedContexts(
                            rights(CREATE_SEPA_PAYMENTS, "MANAGE_BANK_TRANSACTIONS")
                        ) { contextual ->
                            val collection = validatedSepaCollection(
                                contextual.data.sepaCollectionId.value.toUuid()
                            )
                            setOf(
                                collection.creditorIdentifier.legalEntity.partyId,
                                collection.creditorAccount.userId
                            ).map { partyId ->
                                contextIdOf(BANKING_APPLICATION_NAME) { partyId }
                            }.toSet()
                        } *
                        CreateSepaPaymentsForCollection() *
                        Respond { transform() } runOn Base(call, environment)
                    }
                    post("create-payment-successors") {
                        ReceiveContextual<CreateSepaPaymentSuccessors>() *
                        IsGrantedOneOfInAllDerivedContexts(
                            rights(CREATE_SEPA_PAYMENTS, "MANAGE_BANK_TRANSACTIONS")
                        ) { contextual ->
                            contextual.data.paymentIds.requireSepaBatch(SepaException.Payment.EmptyBatch)
                            contextual.data.paymentIds.flatMap { paymentId ->
                                val collection = validatedPayment(paymentId.value.toUuid()).collection
                                listOf(
                                    collection.creditorIdentifier.legalEntity.partyId,
                                    collection.creditorAccount.userId
                                )
                            }.map { partyId ->
                                contextIdOf(BANKING_APPLICATION_NAME) { partyId }
                            }.toSet()
                        } *
                        CreateSepaPaymentSuccessors() *
                        Respond { transform() } runOn Base(call, environment)
                    }
                    post("generate-sepa-message") {
                        ReceiveContextual<GenerateSepaMessageForCollection>() *
                        IsGrantedOneOfInAllDerivedContexts(
                            rights(CREATE_SEPA_MESSAGES, "MANAGE_BANK_TRANSACTIONS")
                        ) { contextual ->
                            contextual.data.sepaPaymentIds?.requireSepaBatch(SepaException.Payment.EmptyBatch)
                            val collection = validatedSepaCollection(
                                contextual.data.sepaCollectionId.value.toUuid()
                            )
                            setOf(
                                collection.creditorIdentifier.legalEntity.partyId,
                                collection.creditorAccount.userId
                            ).map { partyId ->
                                contextIdOf(BANKING_APPLICATION_NAME) { partyId }
                            }.toSet()
                        } *
                        GenerateSepaMessageForCollection() *
                        Respond { transform() } runOn Base(call, environment)
                    }
                    patch("update") {
                        ReceiveContextual<UpdateSepaCollection>() *
                        IsGrantedOneOfInAllDerivedContexts(
                            rights(UPDATE_SEPA_COLLECTIONS, "MANAGE_BANK_TRANSACTIONS")
                        ) { contextual ->
                            val data = contextual.data
                            val sourceCollection = validatedSepaCollection(data.sepaCollectionId.value.toUuid())
                            val targetCreditorPartyId = validatedCreditorIdentifier(
                                data.creditorIdentifierId.value.toUuid()
                            ).legalEntity.partyId
                            val targetAccountOwnerId = validatedBankAccount(
                                data.creditorAccountId.value.toUuid()
                            ).userId
                            setOf(
                                sourceCollection.creditorIdentifier.legalEntity.partyId,
                                sourceCollection.creditorAccount.userId,
                                targetCreditorPartyId,
                                targetAccountOwnerId
                            ).map { partyId ->
                                contextIdOf(BANKING_APPLICATION_NAME) { partyId }
                            }.toSet()
                        } *
                        UpdateSepaCollection() *
                        Respond { transform() } runOn Base(call, environment)
                    }
                    delete("delete") {
                        NotImplemented("Sepa collection deletion is not implemented yet")
                    }

                }
                route("payments"){
                    post("create") {
                        NotImplemented("Sepa collection creation is not implemented yet")
                    }
                    post("create-ad-hoc") {
                        ReceiveContextual<CreateAdHocSepaPayment>() *
                        IsGrantedOneOfInAllDerivedContexts(
                            rights(UPDATE_SEPA_PAYMENTS, "MANAGE_BANK_TRANSACTIONS")
                        ) { contextual ->
                            val data = contextual.data
                            val collection = validatedSepaCollection(data.sepaCollectionId.value.toUuid())
                            val mandate = validatedSepaMandate(data.sepaMandateId.value.toUuid())
                            val predecessorCollection = data.predecessorId?.let { predecessorId ->
                                validatedPayment(predecessorId.value.toUuid()).collection
                            }
                            listOfNotNull(
                                collection.creditorIdentifier.legalEntity.partyId,
                                collection.creditorAccount.userId,
                                mandate.creditorIdentifier.legalEntity.partyId,
                                predecessorCollection?.creditorIdentifier?.legalEntity?.partyId,
                                predecessorCollection?.creditorAccount?.userId
                            ).map { partyId ->
                                contextIdOf(BANKING_APPLICATION_NAME) { partyId }
                            }.toSet()
                        } *
                        CreateAdHocSepaPayment() *
                        Respond { transform() } runOn Base(call, environment)
                    }
                    patch("update-execution-statuses") {
                        ReceiveContextual<UpdateSepaPaymentExecutionStatuses>() *
                        IsGrantedOneOfInAllDerivedContexts(
                            rights(UPDATE_SEPA_PAYMENTS, "MANAGE_BANK_TRANSACTIONS")
                        ) { contextual ->
                            contextual.data.paymentIds.requireSepaBatch(SepaException.Payment.EmptyBatch)
                            contextual.data.paymentIds.flatMap { paymentId ->
                                val collection = validatedPayment(paymentId.value.toUuid()).collection
                                listOf(
                                    collection.creditorIdentifier.legalEntity.partyId,
                                    collection.creditorAccount.userId
                                )
                            }.map { partyId ->
                                contextIdOf(BANKING_APPLICATION_NAME) { partyId }
                            }.toSet()
                        } *
                        UpdateSepaPaymentExecutionStatuses() *
                        Respond { transform() } runOn Base(call, environment)
                    }
                    patch("update") {
                        ReceiveContextual<UpdateSepaPayment>() *
                        IsGrantedOneOfInAllDerivedContexts(
                            rights(UPDATE_SEPA_PAYMENTS, "MANAGE_BANK_TRANSACTIONS")
                        ) { contextual ->
                            val data = contextual.data
                            val collection = validatedPayment(data.sepaPaymentId.value.toUuid()).collection
                            val message = data.sepaMessageId?.let { messageId ->
                                validateSepaMessage(messageId.value.toUuid())
                            }
                            listOfNotNull(
                                collection.creditorIdentifier.legalEntity.partyId,
                                collection.creditorAccount.userId,
                                message?.creditorIdentifier?.legalEntity?.partyId,
                                message?.creditorAccount?.userId
                            ).map { partyId ->
                                contextIdOf(BANKING_APPLICATION_NAME) { partyId }
                            }.toSet()
                        } *
                        UpdateSepaPayment() *
                        Respond { transform() } runOn Base(call, environment)
                    }
                    patch("update-many") {
                        ReceiveContextual<UpdateSepaPayments>() *
                        IsGrantedOneOfInAllDerivedContexts(
                            rights(UPDATE_SEPA_PAYMENTS, "MANAGE_BANK_TRANSACTIONS")
                        ) { contextual ->
                            contextual.data.all.requireSepaBatch(SepaException.Payment.EmptyBatch)
                            contextual.data.all.flatMap { paymentUpdate ->
                                val collection = validatedPayment(
                                    paymentUpdate.sepaPaymentId.value.toUuid()
                                ).collection
                                val message = paymentUpdate.sepaMessageId?.let { messageId ->
                                    validateSepaMessage(messageId.value.toUuid())
                                }
                                listOfNotNull(
                                    collection.creditorIdentifier.legalEntity.partyId,
                                    collection.creditorAccount.userId,
                                    message?.creditorIdentifier?.legalEntity?.partyId,
                                    message?.creditorAccount?.userId
                                )
                            }.map { partyId ->
                                contextIdOf(BANKING_APPLICATION_NAME) { partyId }
                            }.toSet()
                        } *
                        UpdateSepaPayments() *
                        Respond { transform() } runOn Base(call, environment)
                    }
                    delete("delete") {
                        ReceiveContextual<DeleteSepaPayment>() *
                        IsGrantedOneOfInAllDerivedContexts(
                            rights(DELETE_SEPA_PAYMENTS, "MANAGE_BANK_TRANSACTIONS")
                        ) { contextual ->
                            val collection = validatedPayment(contextual.data.id.value.toUuid()).collection
                            setOf(
                                collection.creditorIdentifier.legalEntity.partyId,
                                collection.creditorAccount.userId
                            ).map { partyId ->
                                contextIdOf(BANKING_APPLICATION_NAME) { partyId }
                            }.toSet()
                        } *
                        DeleteSepaPayment() *
                        Respond { transform() } runOn Base(call, environment)
                    }
                    delete("delete-many") {
                        ReceiveContextual<DeleteSepaPayments>() *
                        IsGrantedOneOfInAllDerivedContexts(
                            rights(DELETE_SEPA_PAYMENTS, "MANAGE_BANK_TRANSACTIONS")
                        ) { contextual ->
                            contextual.data.paymentIds.requireSepaBatch(SepaException.Payment.EmptyBatch)
                            contextual.data.paymentIds.flatMap { paymentId ->
                                val collection = validatedPayment(paymentId.value.toUuid()).collection
                                listOf(
                                    collection.creditorIdentifier.legalEntity.partyId,
                                    collection.creditorAccount.userId
                                )
                            }.map { partyId ->
                                contextIdOf(BANKING_APPLICATION_NAME) { partyId }
                            }.toSet()
                        } *
                        DeleteSepaPayments() *
                        Respond { transform() } runOn Base(call, environment)
                    }
                }
                route("payment-links") {
                    get("by-legal-entity") {
                        ReceiveContextual<LegalEntityId> { parameters ->
                            LegalEntityId(requireNotNull(parameters["legal_entity"]) {
                                "Parameter 'legal_entity' is empty"
                            })
                        } *
                        IsGrantedOneOfInAllDerivedContexts(
                            rights(READ_SEPA_PAYMENTS, "MANAGE_BANK_TRANSACTIONS")
                        ) { contextual ->
                            val partyId = validatedLegalEntity(contextual.data.value.toUuid()).partyId
                            setOf(contextIdOf(BANKING_APPLICATION_NAME) { partyId })
                        } *
                        ReadSepaPaymentLinksByLegalEntity() *
                        Respond { transform() } runOn Base(call, environment)
                    }
                    get("personal") {
                        ReceiveContextual<Unit>{} *
                        ReadPersonalSepaPaymentLinks() *
                        Respond { transform() } runOn Base(call, environment)
                    }
                }
                route("messages") {
                    get("download") {
                        ReceiveContextual<SepaMessageId> { parameters ->
                            SepaMessageId(requireNotNull(parameters["message_id"]) {
                                "Parameter 'message_id' is empty"
                            })
                        } *
                        IsGrantedOneOfInAllDerivedContexts(
                            rights(READ_SEPA_MESSAGES, "MANAGE_BANK_TRANSACTIONS")
                        ) { contextual ->
                            val message = validateSepaMessage(contextual.data.value.toUuid())
                            setOf(
                                message.creditorIdentifier.legalEntity.partyId,
                                message.creditorAccount.userId
                            ).map { partyId ->
                                contextIdOf(BANKING_APPLICATION_NAME) { partyId }
                            }.toSet()
                        } *
                        DownloadSepaMessage() *
                        Respond { transform() } runOn Base(call, environment)
                    }
                    get("by-legal-entity") {
                        ReceiveContextual<LegalEntityId> { parameters ->
                            LegalEntityId(requireNotNull(parameters["legal_entity"]) {
                                "Parameter 'legal_entity' is empty"
                            })
                        } *
                        IsGrantedOneOfInAllDerivedContexts(
                            rights(READ_SEPA_MESSAGES, "MANAGE_BANK_TRANSACTIONS")
                        ) { contextual ->
                            val partyId = validatedLegalEntity(contextual.data.value.toUuid()).partyId
                            setOf(contextIdOf(BANKING_APPLICATION_NAME) { partyId })
                        } *
                        ReadSepaMessagesByLegalEntityId() *
                        Respond { transform() } runOn Base(call, environment)
                    }
                    patch("update-status") {
                        ReceiveContextual<UpdateSepaMessageStatus>() *
                        IsGrantedOneOfInAllDerivedContexts(
                            rights(UPDATE_SEPA_MESSAGES, "MANAGE_BANK_TRANSACTIONS")
                        ) { contextual ->
                            val message = validateSepaMessage(
                                contextual.data.sepaMessageId.value.toUuid()
                            )
                            setOf(
                                message.creditorIdentifier.legalEntity.partyId,
                                message.creditorAccount.userId
                            ).map { partyId ->
                                contextIdOf(BANKING_APPLICATION_NAME) { partyId }
                            }.toSet()
                        } *
                        UpdateSepaMessageStatus() *
                        Respond { transform() } runOn Base(call, environment)
                    }
                    post("merge") {
                        ReceiveContextual<MergeSepaMessages>() *
                        IsGrantedOneOfInAllDerivedContexts(
                            rights(UPDATE_SEPA_MESSAGES, "MANAGE_BANK_TRANSACTIONS")
                        ) { contextual ->
                            contextual.data.sepaMessageIds.requireSepaBatch(SepaException.Message.EmptyBatch)
                            contextual.data.sepaMessageIds.flatMap { messageId ->
                                val message = validateSepaMessage(messageId.value.toUuid())
                                listOf(
                                    message.creditorIdentifier.legalEntity.partyId,
                                    message.creditorAccount.userId
                                )
                            }.map { partyId ->
                                contextIdOf(BANKING_APPLICATION_NAME) { partyId }
                            }.toSet()
                        } *
                        MergeSepaMessages() *
                        Respond { transform() } runOn Base(call, environment)
                    }
                }
            }
        }
    }
}

private fun List<*>.requireSepaBatch(emptyBatch: SepaException) {
    if (isEmpty()) throw emptyBatch
}
