package org.solyton.solawi.bid.module.banking.service

import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.solyton.solawi.bid.module.banking.data.internal.Pain008GenerationRequest
import org.solyton.solawi.bid.module.banking.data.internal.Pain008Transaction
import org.solyton.solawi.bid.module.banking.exception.BankAccountsException
import org.solyton.solawi.bid.module.banking.exception.SepaException
import org.solyton.solawi.bid.module.banking.schema.*
import java.math.BigDecimal
import java.util.*

// ================================
// Constants and formatters
// ================================
private val dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd")
private val dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss")

// ================================
// Main generation function
// ================================

/**
 * Generiert eine pain.008.xml Datei für SEPA-Lastschriften
 */
fun generatePain008Xml(request: Pain008GenerationRequest): String {
    return transaction {
        val creditorIdentifier = CreditorIdentifier.findById(request.creditorId)
            ?: throw IllegalArgumentException("Creditor identifier not found")
        
        val creditorAccount = BankAccount.findById(request.creditorAccountId)
            ?: throw IllegalArgumentException("Creditor account not found")

        // Create SEPA Message Entity
        val sepaMessage = createSepaMessage(request, creditorIdentifier, creditorAccount)

        // Validate all Transactions
        validateTransactions(request.transactions)

        // Generate XML
        buildPain008Xml(sepaMessage, creditorIdentifier, creditorAccount, request.transactions)
    }
}

// ================================
// Amount formatting functions
// ================================

/**
 * Format amount as english Decimalformat (dot as delimiter)
 */
fun formatAmount(amount: BigDecimal): String {
    return amount.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()
}

// ================================
// SEPA message creation
// ================================

private fun createSepaMessage(
    request: Pain008GenerationRequest,
    creditorIdentifier: CreditorIdentifierEntity,
    creditorAccount: BankAccountEntity
): SepaMessageEntity {
    val messageId = generateMessageId()
    val totalAmount = request.transactions.sumOf { it.amount }

    return SepaMessageEntity.new {
        this.createdBy = request.createdBy
        this.creditorIdentifier = creditorIdentifier
        this.creditorAccount = creditorAccount
        this.messageId = messageId
        this.executionDate = request.executionDate.toDateTimeAtCurrentTime()
        this.status = SepaMessageStatus.CREATED
        this.numberOfPayments = request.transactions.size
        this.totalAmount = totalAmount.toDouble()
    }
}

// ================================
// Validation functions
// ================================

private fun validateTransactions(transactions: List<Pain008Transaction>) {
    transactions.forEach { transaction ->
        // Validate IBAN
        if (!isValidIban(transaction.debtorIban)) {
            throw BankAccountsException.InvalidIban(transaction.debtorIban)
        }

        // Validate Amount
        if (transaction.amount <= BigDecimal.ZERO) {
            throw SepaException.Transaction.InvalidAmount( transaction.amount.toString())
        }

        // Validate Mandate reference
        if (transaction.mandateReference.isBlank()) {
            throw SepaException.Transaction.MissingMandateReference
        }

        // Validate End-to-End ID
        if (transaction.endToEndId.isBlank()) {
            throw SepaException.Transaction.MissingEndToEndId
        }
    }
}
/*
fun isValidIban(iban: String): Boolean {
    val cleanIban = iban.replace(" ", "").uppercase()
    return cleanIban.length >= 15 && cleanIban.length <= 34 && 
           cleanIban.matches(Regex("[A-Z]{2}[0-9]{2}[A-Z0-9]+"))
}

 */

// ================================
// XML building functions
// ================================

private fun buildPain008Xml(
    sepaMessage: SepaMessageEntity,
    creditorIdentifier: CreditorIdentifierEntity,
    creditorAccount: BankAccountEntity,
    transactions: List<Pain008Transaction>
): String {
    val totalAmount = transactions.sumOf { it.amount }
    val numberOfTransactions = transactions.size
    val executionDate = sepaMessage.executionDate
    
    return buildString {
        appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
        appendLine("""<Document xmlns="urn:iso:std:iso:20022:tech:xsd:pain.008.001.02" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">""")
        appendLine("  <CstmrDrctDbtInitn>")
        
        // Group Header
        appendGroupHeader(this, sepaMessage, numberOfTransactions, totalAmount, creditorIdentifier)
        
        // Payment Information
        appendPaymentInformation(this, sepaMessage, numberOfTransactions, totalAmount, executionDate, creditorIdentifier, creditorAccount, transactions)
        
        appendLine("  </CstmrDrctDbtInitn>")
        appendLine("</Document>")
    }
}

private fun appendGroupHeader(
    builder: StringBuilder,
    sepaMessage: SepaMessageEntity,
    numberOfTransactions: Int,
    totalAmount: BigDecimal,
    creditorIdentifier: CreditorIdentifierEntity
) {
    with(builder) {
        appendLine("    <GrpHdr>")
        appendLine("      <MsgId>${sepaMessage.messageId}</MsgId>")
        appendLine("      <CreDtTm>${dateTimeFormatter.print(DateTime.now())}</CreDtTm>")
        appendLine("      <NbOfTxs>$numberOfTransactions</NbOfTxs>")
        appendLine("      <CtrlSum>${formatAmount(totalAmount)}</CtrlSum>")
        appendLine("      <InitgPty>")
        appendLine("        <Nm>${escapeXml(creditorIdentifier.legalEntity.name)}</Nm>")
        appendLine("        <Id>")
        appendLine("          <OrgId>")
        appendLine("            <Othr>")
        appendLine("              <Id>${creditorIdentifier.creditorId}</Id>")
        appendLine("              <SchmeNm>")
        appendLine("                <Prtry>SEPA</Prtry>")
        appendLine("              </SchmeNm>")
        appendLine("            </Othr>")
        appendLine("          </OrgId>")
        appendLine("        </Id>")
        appendLine("      </InitgPty>")
        appendLine("    </GrpHdr>")
    }
}

private fun appendPaymentInformation(
    builder: StringBuilder,
    sepaMessage: SepaMessageEntity,
    numberOfTransactions: Int,
    totalAmount: BigDecimal,
    executionDate: DateTime,
    creditorIdentifier: CreditorIdentifierEntity,
    creditorAccount: BankAccountEntity,
    transactions: List<Pain008Transaction>
) {
    with(builder) {
        appendLine("    <PmtInf>")
        appendLine("      <PmtInfId>${sepaMessage.messageId}-1</PmtInfId>")
        appendLine("      <PmtMtd>DD</PmtMtd>")
        appendLine("      <NbOfTxs>$numberOfTransactions</NbOfTxs>")
        appendLine("      <CtrlSum>${formatAmount(totalAmount)}</CtrlSum>")
        appendLine("      <PmtTpInf>")
        appendLine("        <SvcLvl>")
        appendLine("          <Cd>SEPA</Cd>")
        appendLine("        </SvcLvl>")
        appendLine("        <LclInstrm>")
        appendLine("          <Cd>CORE</Cd>")
        appendLine("        </LclInstrm>")
        appendLine("        <SeqTp>OOFF</SeqTp>")
        appendLine("      </PmtTpInf>")
        appendLine("      <ReqdColltnDt>${dateFormatter.print(executionDate)}</ReqdColltnDt>")
        
        // Creditor Information
        appendCreditorInformation(this, creditorIdentifier, creditorAccount)
        
        // Transaction Information
        transactions.forEach { transaction ->
            appendTransactionInformation(this, transaction)
        }
        
        appendLine("    </PmtInf>")
    }
}

private fun appendCreditorInformation(
    builder: StringBuilder,
    creditorIdentifier: CreditorIdentifierEntity,
    creditorAccount: BankAccountEntity
) {
    with(builder) {
        appendLine("      <Cdtr>")
        appendLine("        <Nm>${escapeXml(creditorIdentifier.legalEntity.name)}</Nm>")
        appendLine("      </Cdtr>")
        appendLine("      <CdtrAcct>")
        appendLine("        <Id>")
        appendLine("          <IBAN>${creditorAccount.iban}</IBAN>")
        appendLine("        </Id>")
        appendLine("      </CdtrAcct>")
        appendLine("      <CdtrAgt>")
        appendLine("        <FinInstnId>")
        appendLine("          <BIC>${creditorAccount.bic}</BIC>")
        appendLine("        </FinInstnId>")
        appendLine("      </CdtrAgt>")
        appendLine("      <CdtrSchmeId>")
        appendLine("        <Id>")
        appendLine("          <PrvtId>")
        appendLine("            <Othr>")
        appendLine("              <Id>${creditorIdentifier.creditorId}</Id>")
        appendLine("              <SchmeNm>")
        appendLine("                <Prtry>SEPA</Prtry>")
        appendLine("              </SchmeNm>")
        appendLine("            </Othr>")
        appendLine("          </PrvtId>")
        appendLine("        </Id>")
        appendLine("      </CdtrSchmeId>")
    }
}

private fun appendTransactionInformation(
    builder: StringBuilder,
    transaction: Pain008Transaction
) {
    with(builder) {
        appendLine("      <DrctDbtTxInf>")
        appendLine("        <PmtId>")
        appendLine("          <EndToEndId>${escapeXml(transaction.endToEndId)}</EndToEndId>")
        appendLine("        </PmtId>")
        appendLine("        <InstdAmt Ccy=\"EUR\">${formatAmount(transaction.amount)}</InstdAmt>")
        appendLine("        <DrctDbtTx>")
        appendLine("          <MndtRltdInf>")
        appendLine("            <MndtId>${escapeXml(transaction.mandateReference)}</MndtId>")
        appendLine("            <DtOfSgntr>${dateFormatter.print(transaction.mandateSignDate)}</DtOfSgntr>")
        appendLine("          </MndtRltdInf>")
        appendLine("        </DrctDbtTx>")
        appendLine("        <DbtrAgt>")
        appendLine("          <FinInstnId>")
        if (transaction.debtorBic.isNotBlank()) {
            appendLine("            <BIC>${transaction.debtorBic}</BIC>")
        }
        appendLine("          </FinInstnId>")
        appendLine("        </DbtrAgt>")
        appendLine("        <Dbtr>")
        appendLine("          <Nm>${escapeXml(transaction.debtorName)}</Nm>")
        appendLine("        </Dbtr>")
        appendLine("        <DbtrAcct>")
        appendLine("          <Id>")
        appendLine("            <IBAN>${transaction.debtorIban}</IBAN>")
        appendLine("          </Id>")
        appendLine("        </DbtrAcct>")
        if (transaction.remittanceInfo.isNotBlank()) {
            appendLine("        <RmtInf>")
            appendLine("          <Ustrd>${escapeXml(transaction.remittanceInfo)}</Ustrd>")
            appendLine("        </RmtInf>")
        }
        appendLine("      </DrctDbtTxInf>")
    }
}

// ================================
// Utility functions
// ================================

fun generateMessageId(): String {
    val timestamp = DateTime.now().toString("yyyyMMddHHmmss")
    val random = (1_000..9_999).random()
    return "MSG-$timestamp-$random"
}

fun generateE2ETransactionId(): String {
    val timestamp = DateTime.now().toString("yyyyMMddHHmmss")
    val random = (1_000_000..9_999_999).random()
    return "TSX-$timestamp-$random"
}

fun escapeXml(text: String): String {
    return text
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")
}
