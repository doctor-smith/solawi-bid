package org.solyton.solawi.bid.module.banking.service

import org.solyton.solawi.bid.module.banking.exception.SepaException
import org.xml.sax.ErrorHandler
import org.xml.sax.SAXException
import org.xml.sax.SAXParseException
import java.io.ByteArrayInputStream
import javax.xml.XMLConstants
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.SchemaFactory
import javax.xml.validation.Validator

// ================================
// Constants
// ================================
private const val LOCAL_SCHEMA_PATH = "/schemas/iso20022/pain.008.001.02.xsd"

// ================================
// Data Types for Validation Results
// ================================

sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val message: String, val exception: Exception) : ValidationResult()
    data class Error(val message: String, val exception: Exception) : ValidationResult()
}

sealed class DetailedValidationResult {
    object Valid : DetailedValidationResult()
    data class Invalid(val errors: List<ValidationError>) : DetailedValidationResult()
    data class Error(val message: String, val exception: Exception) : DetailedValidationResult()
}

data class ValidationError(
    val line: Int?,
    val column: Int?,
    val message: String,
    val severity: ErrorSeverity
)

enum class ErrorSeverity {
    WARNING, ERROR, FATAL_ERROR
}

data class ValidationStatistics(
    val totalErrors: Int,
    val warnings: Int,
    val errors: Int,
    val fatalErrors: Int
) {
    val isValid: Boolean get() = errors == 0 && fatalErrors == 0
    val hasIssues: Boolean get() = totalErrors > 0
}

// ================================
// Main Validation Functions
// ================================

/**
 * Validates a pain.008 XML against the official schema.
 */
fun validatePain008Xml(xmlContent: String): ValidationResult {
    return try {
        val validator = createPain008Validator()
        val inputStream = ByteArrayInputStream(xmlContent.toByteArray(Charsets.UTF_8))
        validator.validate(StreamSource(inputStream))
        ValidationResult.Valid
    } catch (e: SAXException) {
        ValidationResult.Invalid(e.message ?: "Unknown validation error", e)
    } catch (e: Exception) {
        ValidationResult.Error("Validation failed: ${e.message}", e)
    }
}

/**
 * Validates a pain.008 XML with detailed error messages.
 */
fun validatePain008XmlWithDetails(xmlContent: String): DetailedValidationResult {
    return try {
        val validator = createPain008Validator()
        val errorHandler = ValidationErrorHandler()
        validator.errorHandler = errorHandler
        
        val inputStream = ByteArrayInputStream(xmlContent.toByteArray(Charsets.UTF_8))
        validator.validate(StreamSource(inputStream))
        
        val collectedErrors = errorHandler.getErrors()
        if (collectedErrors.isEmpty()) {
            DetailedValidationResult.Valid
        } else {
            DetailedValidationResult.Invalid(collectedErrors)
        }
    } catch (e: Exception) {
        DetailedValidationResult.Error("Validation failed: ${e.message}", e)
    }
}

/**
 * Validates only the XML structure (well-formedness) without schema
 */
fun validateXmlStructure(xmlContent: String): Boolean {
    return try {
        val inputStream = ByteArrayInputStream(xmlContent.toByteArray(Charsets.UTF_8))
        val factory = javax.xml.parsers.DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        builder.parse(inputStream)
        true
    } catch (e: Exception) {
        false
    }
}

/**
 * Checks whether the XML has a pain.008 namespace
 */
fun hasPain008Namespace(xmlContent: String): Boolean {
    return xmlContent.contains("urn:iso:std:iso:20022:tech:xsd:pain.008.001.02")
}

/**
 * Quick basic validation without full schema validation.
 */
@Suppress("ReturnCount")
fun quickValidatePain008(xmlContent: String): ValidationResult {
    return try {
        // 1. XML Well-formedness
        if (!validateXmlStructure(xmlContent)) {
            return ValidationResult.Invalid("XML ist nicht wohlgeformt", 
                Exception("Malformed XML"))
        }
        
        // 2. Namespace-Check
        if (!hasPain008Namespace(xmlContent)) {
            return ValidationResult.Invalid("Fehlendes pain.008 Namespace", 
                Exception("Invalid namespace"))
        }
        
        // 3. Basic Structure-Elements
        val requiredElements = listOf(
            "Document", "CstmrDrctDbtInitn", "GrpHdr", "PmtInf"
        )
        
        for (element in requiredElements) {
            if (!xmlContent.contains("<$element")) {
                return ValidationResult.Invalid("Fehlendes Element: $element", 
                    Exception("Missing required element"))
            }
        }
        
        ValidationResult.Valid
    } catch (e: Exception) {
        ValidationResult.Error("Schnellvalidierung fehlgeschlagen", e)
    }
}

// ================================
// Helper Functions
// ================================

// ================================
// Validator Creation Functions
// ================================

/**
 * Creates a validator for the pain.008 schema.
 */
fun createPain008Validator(): Validator {
    val factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
    
    // Lade Schema aus Resources
    val schemaStream = object {}.javaClass.getResourceAsStream(LOCAL_SCHEMA_PATH)
        ?: throw SepaException.MissingXmlSchema("pain.008.001.02.xsd")
        /*
        IllegalStateException(
            "Schema nicht gefunden: $LOCAL_SCHEMA_PATH. " +
            "Bitte stellen Sie sicher, dass pain.008.001.02.xsd in src/main/resources/schemas/ liegt."
        )

         */

    val schema = factory.newSchema(StreamSource(schemaStream))
    return schema.newValidator()
}

/**
 * Checks whether the pain.008 schema is available.
 */
fun isPain008SchemaAvailable(): Boolean {
    return try {
        createPain008Validator()
        true
    } catch (e: Exception) {
        false
    }
}

/**
 * Formats validation errors for output.
 */
fun formatValidationErrors(errors: List<ValidationError>): String {
    return errors.joinToString("\n") { error ->
        val location = if (error.line != null && error.column != null) {
            "Zeile ${error.line}, Spalte ${error.column}: "
        } else ""

        "${error.severity.name} - $location${error.message}"
    }
}

/**
 * Returns validation statistics.
 */
fun getValidationStatistics(errors: List<ValidationError>): ValidationStatistics {
    return ValidationStatistics(
        totalErrors = errors.size,
        warnings = errors.count { it.severity == ErrorSeverity.WARNING },
        errors = errors.count { it.severity == ErrorSeverity.ERROR },
        fatalErrors = errors.count { it.severity == ErrorSeverity.FATAL_ERROR }
    )
}

/**
 * Batch validation of multiple XML files.
 */
fun validateMultiplePain008Xmls(xmlContents: List<String>): List<Pair<Int, ValidationResult>> {
    return xmlContents.mapIndexed { index, xml ->
        index to validatePain008Xml(xml)
    }
}

// ================================
// Error Handling
// ================================

class ValidationErrorHandler : ErrorHandler {
    private val errors = mutableListOf<ValidationError>()

    override fun warning(exception: SAXParseException) {
        errors.add(ValidationError(
            line = exception.lineNumber,
            column = exception.columnNumber,
            message = exception.message ?: "Unknown warning",
            severity = ErrorSeverity.WARNING
        ))
    }

    override fun error(exception: SAXParseException) {
        errors.add(ValidationError(
            line = exception.lineNumber,
            column = exception.columnNumber,
            message = exception.message ?: "Unknown error",
            severity = ErrorSeverity.ERROR
        ))
    }

    override fun fatalError(exception: SAXParseException) {
        errors.add(ValidationError(
            line = exception.lineNumber,
            column = exception.columnNumber,
            message = exception.message ?: "Unknown fatal error",
            severity = ErrorSeverity.FATAL_ERROR
        ))
    }

    fun getErrors(): List<ValidationError> = errors.toList()
    
    // ✅ Fehlende Methoden hinzufügen:
    fun hasErrors(): Boolean = errors.any { it.severity == ErrorSeverity.ERROR || it.severity == ErrorSeverity.FATAL_ERROR }
    
    fun hasWarnings(): Boolean = errors.any { it.severity == ErrorSeverity.WARNING }
    
    fun clear() = errors.clear()
}
