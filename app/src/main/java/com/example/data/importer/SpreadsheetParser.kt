package com.example.data.importer

import android.content.Context
import android.net.Uri
import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.zip.ZipInputStream

data class RawParsedSheet(
    val fileName: String,
    val fileSize: Long,
    val headers: List<String>,
    val rows: List<List<String>>,
    val detectedMappings: ColumnMapping
)

data class ColumnMapping(
    val companyNameCol: String? = null,
    val contactNameCol: String? = null,
    val emailCol: String? = null,
    val whatsappCol: String? = null,
    val countryCodeCol: String? = null,
    val websiteCol: String? = null,
    val businessTypeCol: String? = null,
    val countryCol: String? = null,
    val locationCol: String? = null
)

data class ValidationIssue(
    val rowIndex: Int,
    val field: String,
    val message: String,
    val isFatal: Boolean
)

data class ValidatedLeadRow(
    val rowIndex: Int,
    val rawValues: Map<String, String>,
    val companyName: String,
    val contactPerson: String,
    val email: String,
    val whatsapp: String,
    val countryCode: String = "",
    val website: String,
    val businessType: String,
    val country: String,
    val location: String,
    val issues: List<ValidationIssue>,
    val isDuplicate: Boolean = false
)

object SpreadsheetParser {

    fun parseSpreadsheet(
        context: Context,
        uri: Uri,
        fileName: String,
        fileSize: Long
    ): RawParsedSheet {
        val lowerName = fileName.lowercase()
        return if (lowerName.endsWith(".xlsx")) {
            val contentStream = context.contentResolver.openInputStream(uri)
                ?: throw IllegalArgumentException("Could not open file stream")
            parseXlsx(contentStream, fileName, fileSize)
        } else {
            val contentStream = context.contentResolver.openInputStream(uri)
                ?: throw IllegalArgumentException("Could not open file stream")
            parseCsv(contentStream, fileName, fileSize)
        }
    }

    fun parseCsv(
        inputStream: InputStream,
        fileName: String,
        fileSize: Long
    ): RawParsedSheet {
        val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
        val rows = mutableListOf<List<String>>()

        var line = reader.readLine()
        // Check delimiter in header line
        val delimiter = if (line != null && line.contains(";") && !line.contains(",")) ';' else ','

        while (line != null) {
            if (line.isNotBlank()) {
                val tokens = parseCsvLine(line, delimiter)
                rows.add(tokens)
            }
            line = reader.readLine()
        }
        reader.close()

        val headers = if (rows.isNotEmpty()) rows[0] else emptyList()
        val dataRows = if (rows.size > 1) rows.subList(1, rows.size) else emptyList()
        val mappings = autoDetectColumns(headers)

        return RawParsedSheet(
            fileName = fileName,
            fileSize = fileSize,
            headers = headers,
            rows = dataRows,
            detectedMappings = mappings
        )
    }

    private fun parseCsvLine(line: String, delimiter: Char): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false

        var i = 0
        while (i < line.length) {
            val c = line[i]
            if (c == '\"') {
                if (inQuotes && i + 1 < line.length && line[i + 1] == '\"') {
                    current.append('\"')
                    i++
                } else {
                    inQuotes = !inQuotes
                }
            } else if (c == delimiter && !inQuotes) {
                result.add(current.toString().trim())
                current.clear()
            } else {
                current.append(c)
            }
            i++
        }
        result.add(current.toString().trim())
        return result
    }

    fun parseXlsx(
        inputStream: InputStream,
        fileName: String,
        fileSize: Long
    ): RawParsedSheet {
        var sharedStrings = listOf<String>()
        val sheetBytesMap = mutableMapOf<String, ByteArray>()

        val zipIn = ZipInputStream(inputStream)
        var entry = zipIn.nextEntry
        while (entry != null) {
            val name = entry.name
            if (name == "xl/sharedStrings.xml") {
                sharedStrings = parseSharedStrings(zipIn)
            } else if (name == "xl/worksheets/sheet1.xml" || name.startsWith("xl/worksheets/sheet")) {
                sheetBytesMap[name] = zipIn.readBytes()
            }
            zipIn.closeEntry()
            entry = zipIn.nextEntry
        }
        zipIn.close()

        val sheetData = sheetBytesMap["xl/worksheets/sheet1.xml"]
            ?: sheetBytesMap.values.firstOrNull()
            ?: throw IllegalStateException("No worksheet data found in XLSX")

        val rows = parseSheetXml(sheetData.inputStream(), sharedStrings)
        val headers = if (rows.isNotEmpty()) rows[0] else emptyList()
        val dataRows = if (rows.size > 1) rows.subList(1, rows.size) else emptyList()
        val mappings = autoDetectColumns(headers)

        return RawParsedSheet(
            fileName = fileName,
            fileSize = fileSize,
            headers = headers,
            rows = dataRows,
            detectedMappings = mappings
        )
    }

    private fun parseSharedStrings(inputStream: InputStream): List<String> {
        val strings = mutableListOf<String>()
        val parser = Xml.newPullParser()
        parser.setInput(inputStream, "UTF-8")

        var eventType = parser.eventType
        var currentText = StringBuilder()
        var insideT = false

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (parser.name == "t") {
                        insideT = true
                    } else if (parser.name == "si") {
                        currentText = StringBuilder()
                    }
                }
                XmlPullParser.TEXT -> {
                    if (insideT) {
                        currentText.append(parser.text)
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name == "t") {
                        insideT = false
                    } else if (parser.name == "si") {
                        strings.add(currentText.toString().trim())
                    }
                }
            }
            eventType = parser.next()
        }
        return strings
    }

    private fun parseSheetXml(inputStream: InputStream, sharedStrings: List<String>): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        val parser = Xml.newPullParser()
        parser.setInput(inputStream, "UTF-8")

        var currentRow = mutableMapOf<Int, String>()
        var currentColIndex = 0
        var cellType = ""
        var cellValue = StringBuilder()
        var insideV = false

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "row" -> {
                            currentRow = mutableMapOf()
                        }
                        "c" -> {
                            cellType = parser.getAttributeValue(null, "t") ?: ""
                            val cellRef = parser.getAttributeValue(null, "r") ?: ""
                            currentColIndex = columnLetterToIndex(cellRef)
                            cellValue = StringBuilder()
                        }
                        "v" -> {
                            insideV = true
                        }
                    }
                }
                XmlPullParser.TEXT -> {
                    if (insideV) {
                        cellValue.append(parser.text)
                    }
                }
                XmlPullParser.END_TAG -> {
                    when (parser.name) {
                        "v" -> {
                            insideV = false
                        }
                        "c" -> {
                            val raw = cellValue.toString().trim()
                            val parsedValue = if (cellType == "s") {
                                val idx = raw.toIntOrNull()
                                if (idx != null && idx in sharedStrings.indices) {
                                    sharedStrings[idx]
                                } else raw
                            } else {
                                raw
                            }
                            currentRow[currentColIndex] = parsedValue
                        }
                        "row" -> {
                            if (currentRow.isNotEmpty()) {
                                val maxCol = (currentRow.keys.maxOrNull() ?: 0)
                                val rowList = (0..maxCol).map { col ->
                                    currentRow[col] ?: ""
                                }
                                rows.add(rowList)
                            }
                        }
                    }
                }
            }
            eventType = parser.next()
        }
        return rows
    }

    private fun columnLetterToIndex(cellRef: String): Int {
        var colStr = ""
        for (c in cellRef) {
            if (c.isLetter()) {
                colStr += c.uppercaseChar()
            } else {
                break
            }
        }
        if (colStr.isEmpty()) return 0
        var index = 0
        for (c in colStr) {
            index = index * 26 + (c - 'A' + 1)
        }
        return index - 1
    }

    fun autoDetectColumns(headers: List<String>): ColumnMapping {
        var company: String? = null
        var contact: String? = null
        var email: String? = null
        var whatsapp: String? = null
        var countryCode: String? = null
        var website: String? = null
        var businessType: String? = null
        var country: String? = null
        var location: String? = null

        val companyKeys = listOf("company", "business name", "organization", "company name", "client", "account name")
        val contactKeys = listOf("contact name", "contact person", "contact", "full name", "lead name", "owner", "first name", "name")
        val emailKeys = listOf("email", "e-mail", "email address", "mail", "contact email")
        val whatsappKeys = listOf("whatsapp", "phone", "mobile", "cell", "telephone", "phone number", "whatsapp number")
        val countryCodeKeys = listOf("country code", "dial code", "calling code", "phone code", "prefix")
        val websiteKeys = listOf("website", "url", "domain", "web", "site", "web address")
        val businessKeys = listOf("business type", "category", "industry", "niche", "type", "sector")
        val countryKeys = listOf("country", "nation", "region")
        val locationKeys = listOf("location", "city", "address", "state", "place")

        for (header in headers) {
            val norm = header.trim().lowercase()

            if (countryCode == null && countryCodeKeys.any { norm.contains(it) }) {
                countryCode = header
            } else if (company == null && companyKeys.any { norm.contains(it) } && !norm.contains("email") && !norm.contains("phone")) {
                company = header
            } else if (email == null && emailKeys.any { norm.contains(it) }) {
                email = header
            } else if (whatsapp == null && whatsappKeys.any { norm.contains(it) }) {
                whatsapp = header
            } else if (website == null && websiteKeys.any { norm.contains(it) }) {
                website = header
            } else if (contact == null && contactKeys.any { norm.contains(it) } && !norm.contains("company")) {
                contact = header
            } else if (businessType == null && businessKeys.any { norm.contains(it) }) {
                businessType = header
            } else if (country == null && countryKeys.any { norm.contains(it) }) {
                country = header
            } else if (location == null && locationKeys.any { norm.contains(it) }) {
                location = header
            }
        }

        // Secondary fallback if exact header "Name" wasn't mapped
        if (company == null && headers.any { it.equals("name", ignoreCase = true) }) {
            company = headers.first { it.equals("name", ignoreCase = true) }
        }

        return ColumnMapping(
            companyNameCol = company,
            contactNameCol = contact,
            emailCol = email,
            whatsappCol = whatsapp,
            countryCodeCol = countryCode,
            websiteCol = website,
            businessTypeCol = businessType,
            countryCol = country,
            locationCol = location
        )
    }

    fun validateRows(
        headers: List<String>,
        rows: List<List<String>>,
        mapping: ColumnMapping,
        existingPhones: Set<String>,
        existingEmails: Set<String>
    ): List<ValidatedLeadRow> {
        val colIndices = headers.mapIndexed { idx, name -> name to idx }.toMap()

        val results = mutableListOf<ValidatedLeadRow>()
        val seenPhonesInBatch = mutableSetOf<String>()
        val seenEmailsInBatch = mutableSetOf<String>()

        rows.forEachIndexed { rowIndex, row ->
            val rawMap = mutableMapOf<String, String>()
            headers.forEachIndexed { idx, h ->
                rawMap[h] = if (idx < row.size) row[idx] else ""
            }

            fun getValue(colName: String?): String {
                if (colName == null) return ""
                val idx = colIndices[colName] ?: return ""
                return if (idx < row.size) row[idx].trim() else ""
            }

            val company = getValue(mapping.companyNameCol)
            val contact = getValue(mapping.contactNameCol)
            val email = getValue(mapping.emailCol)
            val rawPhone = getValue(mapping.whatsappCol)
            val countryCode = getValue(mapping.countryCodeCol)
            val phone = if (countryCode.isNotBlank() && !rawPhone.startsWith("+") && !rawPhone.startsWith(countryCode.replace("+", ""))) {
                val cleanCode = if (countryCode.startsWith("+")) countryCode else "+$countryCode"
                "$cleanCode $rawPhone".trim()
            } else {
                rawPhone
            }
            val website = getValue(mapping.websiteCol)
            val businessType = getValue(mapping.businessTypeCol)
            val country = getValue(mapping.countryCol)
            val location = getValue(mapping.locationCol)

            val issues = mutableListOf<ValidationIssue>()

            // Flag missing company name
            if (company.isBlank()) {
                issues.add(ValidationIssue(rowIndex, "Company Name", "Missing company or business name", isFatal = true))
            }

            // Flag missing contact methods
            if (email.isBlank() && phone.isBlank()) {
                issues.add(ValidationIssue(rowIndex, "Contact", "No Email or WhatsApp number found for outreach", isFatal = false))
            }

            // Validate email format if provided
            if (email.isNotBlank()) {
                val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
                if (!emailRegex.matches(email)) {
                    issues.add(ValidationIssue(rowIndex, "Email", "Invalid email format: $email", isFatal = false))
                }
            }

            // Validate phone number if provided
            val cleanPhone = phone.replace(Regex("[^0-9+]"), "")
            if (phone.isNotBlank() && cleanPhone.length < 7) {
                issues.add(ValidationIssue(rowIndex, "Phone/WhatsApp", "Phone number may be invalid/too short ($phone)", isFatal = false))
            }

            // Duplicate detection
            val normEmail = email.trim().lowercase()
            val normPhone = cleanPhone.replace("+", "")
            var isDuplicate = false

            if (normEmail.isNotBlank()) {
                if (existingEmails.contains(normEmail) || seenEmailsInBatch.contains(normEmail)) {
                    isDuplicate = true
                    issues.add(ValidationIssue(rowIndex, "Duplicate", "Duplicate email ($normEmail) already in database or batch", isFatal = false))
                } else {
                    seenEmailsInBatch.add(normEmail)
                }
            }

            if (normPhone.isNotBlank()) {
                if (existingPhones.contains(normPhone) || seenPhonesInBatch.contains(normPhone)) {
                    isDuplicate = true
                    issues.add(ValidationIssue(rowIndex, "Duplicate", "Duplicate phone ($normPhone) already in database or batch", isFatal = false))
                } else {
                    seenPhonesInBatch.add(normPhone)
                }
            }

            results.add(
                ValidatedLeadRow(
                    rowIndex = rowIndex,
                    rawValues = rawMap,
                    companyName = company,
                    contactPerson = contact,
                    email = email,
                    whatsapp = phone,
                    countryCode = countryCode,
                    website = website,
                    businessType = businessType,
                    country = country,
                    location = location,
                    issues = issues,
                    isDuplicate = isDuplicate
                )
            )
        }

        return results
    }

    fun generateSampleCsvTemplate(): String {
        return buildString {
            appendLine("Company Name,Contact Person,Email,WhatsApp,Website,Business Type,Country,Location")
            appendLine("Artisan Coffee Lab,Marco Rossi,info@artisancoffeelab.com,+14155552671,,Cafe & Roastery,United States,San Francisco")
            appendLine("Apex Legal Consulting,Sarah Jenkins,sjenkins@apexlegal.org,+442079460192,http://old-apex-site.com,Legal Services,United Kingdom,London")
            appendLine("Bella Cucina Trattoria,Luigi Moretti,contact@bellacucina.it,+39025551234,,Italian Restaurant,Italy,Milan")
            appendLine("Nordic Design Studio,Freja Lind,freja@nordicdesign.co,+4681234567,https://nordicdesign.co,Architecture & Interiors,Sweden,Stockholm")
            appendLine("Aura Dental Clinic,Dr. David Vance,reception@auradental.com.au,+61298765432,,Dental & Health,Australia,Sydney")
        }
    }
}
