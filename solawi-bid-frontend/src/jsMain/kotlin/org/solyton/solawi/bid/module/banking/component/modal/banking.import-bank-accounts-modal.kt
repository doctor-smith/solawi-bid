package org.solyton.solawi.bid.module.banking.component.modal

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.evoleq.compose.conditional.When
import org.evoleq.compose.dnd.Dropzone
import org.evoleq.compose.dnd.readFileContent
import org.evoleq.compose.download.downloadCsv
import org.evoleq.compose.modal.Modal
import org.evoleq.compose.modal.ModalData
import org.evoleq.compose.modal.ModalType
import org.evoleq.compose.modal.Modals
import org.evoleq.compose.style.data.device.DeviceType
import org.evoleq.csv.parseCsv
import org.evoleq.kotlinx.date.now
import org.evoleq.language.Lang
import org.evoleq.math.Source
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.nextId
import org.evoleq.optics.storage.put
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.dom.ElementScope
import org.jetbrains.letsPlot.commons.intern.filterNotNullValues
import org.solyton.solawi.bid.module.banking.data.BIC
import org.solyton.solawi.bid.module.banking.data.IBAN
import org.solyton.solawi.bid.module.banking.data.api.AccountType
import org.solyton.solawi.bid.module.banking.data.api.ImportBankAccount
import org.solyton.solawi.bid.module.banking.data.api.ImportBankAccounts
import org.solyton.solawi.bid.module.banking.data.bankaccount.BankAccount
import org.solyton.solawi.bid.module.control.button.FileExportButton
import org.solyton.solawi.bid.module.shares.data.subscriptions.ShareSubscription
import org.solyton.solawi.bid.module.shares.data.subscriptions.ShareSubscriptions
import org.solyton.solawi.bid.module.style.modal.commonModalStyles
import org.solyton.solawi.bid.module.style.wrap.Wrap
import org.solyton.solawi.bid.module.user.data.managed.ManagedUser
import org.solyton.solawi.bid.module.values.AccessorId
import org.solyton.solawi.bid.module.values.Username
import org.w3c.dom.HTMLElement


@Markup
@Suppress("FunctionName")
fun ImportBankAccountsModal(
    id: Int,
    texts: Lang.Block,
    modals: Storage<Modals<Int>>,
    device: Source<DeviceType>,
    accessorId: AccessorId,
    bankAccounts: List<BankAccount>,
    users: List<ManagedUser>,
    setImportBankAccounts: (ImportBankAccounts)->Unit,
    update: ()->Unit
): @Composable ElementScope<HTMLElement>.()->Unit = Modal(
    id = id,
    modals = modals,
    device = device,
    onOk = { update() },
    onCancel = {},
    texts = texts,
    styles = commonModalStyles(device),
) {

    /* keep - needed for overrides
    val userIdToUser = users.associateBy {
        user -> user.id
    }

    val usernameToUser = users.associateBy {
        user -> user.username
    }

    val usernameToBankAccount = bankAccounts.associateBy { bankAccount ->
        userIdToUser[bankAccount.userId.value]
    }.filterNotNullValues()

    */

    val usersWithoutAccounts = users.filter { user -> bankAccounts.none { it.userId.value == user.id } }

    Wrap {

        When(true) {
            FileExportButton(
                color = Color.black,
                bgColor = Color.white,
                { "Download template" },
                device,
            ) {
                val checked = listOf(
                    "username",
                    "bank_account_holder",
                    "iban",
                    "bic",
                    "description",
                    "is_active"
                )

                val headers = checked.joinToString(";")
                val numberOfCols = checked.size
                val semiColons = ";".repeat(numberOfCols - 1)
                val csvLines: String = usersWithoutAccounts.joinToString("\n") { (id, username) ->
                    username + semiColons
                }

                val csv = """
                    |$headers
                    |$csvLines
                """.trimMargin()

                downloadCsv(csv, "bank_accounts_${now()}.csv")
            }
            Dropzone() { files ->
                files.filter { it.name.endsWith("csv") }.map {
                    readFileContent(it) { content ->
                        val parsed = parseCsv(content)
                        val data = parsed.map { map ->
                            val username = Username(map["username"]!!)
                            val bankAccountHolder = map["bank_account_holder"]!!
                            val iban = IBAN(map["iban"]!!)
                            val bic = BIC(map["bic"]!!)
                            val description = map["description"]
                            val isActive = map["is_active"]?.toBooleanStrictOrNull() ?: true

                            ImportBankAccount(
                                username,
                                bankAccountHolder,
                                bic,
                                iban,
                                isActive,
                                AccountType.DEBTOR,
                                description
                            )
                        }
                        setImportBankAccounts(
                            ImportBankAccounts(
                                false,
                                accessorId,
                                data
                            )
                        )
                    }
                }
            }
        }
    }
}

@Markup
fun Storage<Modals<Int>>.showImportBankAccountsModal(
    texts: Lang.Block,
    device: Source<DeviceType>,
    accessorId: AccessorId,
    bankAccounts: List<BankAccount>,
    users: List<ManagedUser>,
    setImportBankAccounts: (ImportBankAccounts)->Unit,
    update: ()->Unit
) = with(nextId()) {
    put(this to ModalData(
        ModalType.Dialog,
        ImportBankAccountsModal(
            id = this,
            texts = texts,
            modals = this@showImportBankAccountsModal,
            device = device,
            accessorId = accessorId,
            bankAccounts = bankAccounts,
            users = users,
            setImportBankAccounts = setImportBankAccounts,
            update = update
        )
    ) )
}

