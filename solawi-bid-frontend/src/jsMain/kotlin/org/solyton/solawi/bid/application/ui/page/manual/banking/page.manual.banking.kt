package org.solyton.solawi.bid.application.ui.page.manual.banking

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.evoleq.compose.layout.Horizontal
import org.evoleq.compose.routing.navigate
import org.evoleq.device.data.mediaType
import org.evoleq.optics.storage.Read
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Li
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.Ul
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.deviceData
import org.solyton.solawi.bid.module.control.button.ArrowUpButton
import org.solyton.solawi.bid.module.page.component.Page
import org.solyton.solawi.bid.module.style.page.Headline
import org.solyton.solawi.bid.module.style.page.PageTitle
import org.solyton.solawi.bid.module.style.page.Paragraph
import org.solyton.solawi.bid.module.style.page.verticalPageStyle
import org.solyton.solawi.bid.module.style.wrap.Wrap

@Markup
@Composable
@Suppress("MaxLineLength")
fun BankingManualPage(storage: Storage<Application>) {

    val deviceType = Read(storage * deviceData * mediaType)

    Page(verticalPageStyle) {
        Wrap {
            Horizontal({
                justifyContent(JustifyContent.SpaceBetween)
                alignItems(AlignItems.Center)
            }) {
                PageTitle("Banking Application")
                Horizontal{
                    ArrowUpButton(
                        Color.black,
                        Color.white,
                        texts = {"Back to Manual Overview"},
                        deviceType = deviceType
                    ) {
                        navigate("/manual")
                    }
                }
            }
        }
        P { Text("The banking application is used to manage your and your customers / members bank accounts. Moreover, it allows you to manage the fiscal years of your organization and provides full support for SEPA payment processing.") }
        Wrap {
            Headline("Bank Accounts")
            P { Text("A bank account is a financial account maintained by a bank or other financial institution for a customer. Bank accounts allow customers to deposit money, withdraw funds, and perform various financial transactions. In this application, you can manage both your organization's bank accounts and your customers' or members' bank accounts.") }
            Paragraph("IBAN and BIC")
            P { Text("IBAN (International Bank Account Number) is a standardized international numbering system for identifying bank accounts across national borders. It consists of up to 34 alphanumeric characters and includes a country code, check digits, bank identifier, and account number. BIC (Bank Identifier Code), also known as SWIFT code, is an 8 or 11-character code that uniquely identifies a financial institution. When processing payments, especially international transfers, both IBAN and BIC are essential for ensuring that funds are routed correctly to the intended recipient's account.") }
        }
        Wrap {
            Headline("Fiscal Years")
            P { Text("A fiscal year (also known as a financial year or budget year) is a period used for calculating annual financial statements in businesses and other organizations. Unlike the calendar year which always runs from January 1 to December 31, a fiscal year can start on any date and ends exactly 12 months later.") }
            P { Text("Organizations choose their fiscal year based on various factors, including business cycles, tax regulations, or seasonal patterns. For example, a farming cooperative might align its fiscal year with the harvest season, while many businesses align with their country's tax year for simplified reporting.") }
            P { Text("In this application, fiscal years are used to organize and track financial transactions, bank accounts, and payment processing within specific time periods. Each fiscal year maintains its own set of financial records, making it easier to generate annual reports, compare year-over-year performance, and manage budgets. When processing SEPA payments or managing bank accounts, transactions are associated with the appropriate fiscal year to ensure accurate financial tracking and reporting.") }
        }
        Wrap {
            Headline("SEPA")
            P { Text("Managing SEPA payments within your organization is quite involved and requires a lot of knowledge and care. It is therefore recommended to use the SEPA payment processing functionality only if you are familiar with the SEPA payment processing process and have the necessary knowledge and experience.") }

            Paragraph("How SEPA Works")
            P { Text("SEPA (Single Euro Payments Area) is a payment integration initiative that simplifies bank transfers denominated in euros across European countries. SEPA allows individuals and businesses to make cashless euro payments to any account located anywhere in the SEPA zone using a single bank account and a single set of payment instruments.") }
            P { Text("The SEPA payment scheme operates through standardized XML messages that are exchanged between banks and payment service providers. These messages follow ISO 20022 standards and ensure that payment instructions are processed uniformly across all participating countries.") }

            Paragraph("SEPA Messages and Pain Formats")
            P { Text("SEPA messages are XML-based files that contain payment instructions. The most common message types are based on the 'pain' (Payment Initiation) format. The term 'pain' is an abbreviation derived from the ISO 20022 message standard naming convention.") }
            P { Text("Key pain message formats include: pain.001 (Customer Credit Transfer Initiation) for regular transfers, pain.008 (Customer Direct Debit Initiation) for collecting payments from customers, and pain.002 (Customer Payment Status Report) for payment status updates. Each message type follows a specific XML schema that defines the structure and data elements required for processing.") }

            Paragraph("Basic SEPA Terms")
            P { Text("Creditor: The party receiving the payment (your organization when collecting payments). Debtor: The party making the payment (your customers/members). Mandate: A written authorization from the debtor allowing the creditor to collect payments from their account. IBAN (International Bank Account Number): The standardized account number used across SEPA. BIC (Bank Identifier Code): The unique identifier for financial institutions. Creditor Identifier: A unique ID assigned to organizations for SEPA direct debit collections.") }
            
            Paragraph("SEPA Messages and Payment Collections")
            P {
                Text("The Banking application uses a hierarchical structure to organize SEPA payment processing:")
            }
            P {
                Text("SepaCollections serve as containers that group related SepaPayments together based on a common collection purpose or time period (such as monthly membership fees). Each collection is equipped with a unique collection_key that identifies it throughout the system.")
            }
            P {
                Text("Each SepaCollection contains multiple SepaPayments, where each SepaPayment represents an individual transaction from a specific debtor to your organization.")
            }
            P {
                Text("When you're ready to process payments, the application generates SepaMessages from these collections. A SepaMessage is the actual XML file (in pain format) that gets sent to the bank, containing all the payment instructions from the associated SepaCollection.")
            }
            P {
                Text("This hierarchical structure allows you to organize payments logically, review them before submission, and maintain clear records of which payments were included in which bank submission.")
            }


            Paragraph("Sell Products in a Recurring Way")
            P { Text("To sell products in your organization, you need to follow these steps:") }
            P({style { marginLeft(30.px) }}) {
                Ul {
                    Li { Text("Purchase an appropriate solyton application, e.g. the share management application") }
                    Li { Text("Create a product (share type) and an offer (share offer) in the application, specifying the (maybe flexible) amount of the payment.") }
                    Li { Text("For each share you wish to abrechnen by SEPA, give it a sepa mandate and connect it with the sepa collection") }

                }
            }
            P { Text("Now the collection will appear in the SEPA module of the banking application page for your organization.") }
            P { Text("You can now process the sepa message.") }

            Paragraph("Processing a sepa message")
            P { Text("In order to process a SEPA message, you need to follow these steps:") }
            P({style { marginLeft(30.px) }}) {
                Ul {
                    Li { Text("Chose a collection") }
                    Li { Text("Move to the Payments tab") }
                    Li { Text("Create payments with specific execution dates (usually two or more days in the future), If you use a mandate for the first time, you can select payments to create under Mandates without any Payments") }
                    Li { Text("Wrap the payment in a message, download the SEPA file and provide it to the bank") }
                    Li { Text("Move to the Messages tab") }
                    Li { Text("Set the status of the message to pending, if the bank accepted the message, otherwise set it to failed (The payments will appear in the Open Payments section again)") }
                    Li { Text("When the bank has confirmed the execution of the message set the message state to confirmed") }
                    Li { Text("The bank credits the amount to your account, but only releases it after a few days. Usually, this takes around 5 business days. Once the amount is available, set the message status to `settled`.") }
                    Li { Text("When the status of the message is settled, you can move back to the Payments tab / settled payments and handle the payment execution states of the payments individually. If a payment failed, you move it to the failed payments, move it to the confirmed state otherwise.") }
                    Li { Text("Both confirmed and failed payments will appear under Create New Payments / Candidates for Recurring Payments. In addition, the failed payments show up under Create New Payments Payments / Candidates for Retry Payments") }
                }
            }

            Paragraph("Payment Histories")
            P{Text("The Tab Payment Histories provides a detailed overview of all payments that have been processed in the past. It shows the payment history for each payment sequence of a debtor, including the payment date, the amount, the payment status, and the payment method. The payment history is useful for tracking the progress of payments and for identifying any issues that may have occurred during the payment process.")}
            P{Text("For example, if a payment fails, the payment history will show the reason for the failure, which can help you troubleshoot the issue and take corrective action.")}
            P{Text("Regular payment sequences of a debtor are listed horizontally, retries are listed vertically")}
        }
    }
}
