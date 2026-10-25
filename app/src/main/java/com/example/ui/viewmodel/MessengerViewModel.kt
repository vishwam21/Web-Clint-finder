package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.importer.ColumnMapping
import com.example.data.importer.DemoDataProvider
import com.example.data.importer.RawParsedSheet
import com.example.data.importer.SpreadsheetParser
import com.example.data.importer.ValidatedLeadRow
import com.example.data.local.AppDatabase
import com.example.data.local.ClientEntity
import com.example.data.local.MessageEntity
import com.example.data.repository.ClientRepository
import com.example.data.repository.RobopixProfile
import com.example.domain.ai.AiMessageGenerator
import com.example.domain.ai.ConnectionTestResult
import com.example.domain.util.ContactNormalizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardMetrics(
    val totalClients: Int = 0,
    val pendingReview: Int = 0,
    val approved: Int = 0,
    val sent: Int = 0,
    val failed: Int = 0,
    val optedOut: Int = 0,
    val skipped: Int = 0
)

data class ClientFilterState(
    val query: String = "",
    val status: String = "ALL",
    val category: String = "ALL",
    val country: String = "ALL",
    val missingFilter: String = "ALL"
)

class MessengerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ClientRepository = ClientRepository(
        AppDatabase.getInstance(application).appDao(),
        application
    )

    // Data streams
    val allClients: StateFlow<List<ClientEntity>> = repository.clients
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMessages: StateFlow<List<MessageEntity>> = repository.messages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingReviewMessages: StateFlow<List<MessageEntity>> = repository.pendingReviewMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val approvedMessages: StateFlow<List<MessageEntity>> = repository.approvedMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Settings Profile
    private val _profile = MutableStateFlow(repository.getProfile())
    val profile: StateFlow<RobopixProfile> = _profile.asStateFlow()

    // Dashboard metrics derived from clients and messages
    val dashboardMetrics: StateFlow<DashboardMetrics> = combine(allClients, allMessages) { clients, messages ->
        DashboardMetrics(
            totalClients = clients.size,
            pendingReview = messages.count { it.status == "PENDING_REVIEW" },
            approved = messages.count { it.status == "APPROVED" },
            sent = messages.count { it.status == "MARKED_SENT" || it.status == "SENT" },
            failed = messages.count { it.status == "FAILED" },
            optedOut = clients.count { it.isOptedOut },
            skipped = messages.count { it.status == "SKIPPED" }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardMetrics())

    // Filter states for Client Management
    val clientSearchQuery = MutableStateFlow("")
    val clientStatusFilter = MutableStateFlow("ALL")
    val clientCategoryFilter = MutableStateFlow("ALL")
    val clientCountryFilter = MutableStateFlow("ALL")
    val clientMissingInfoFilter = MutableStateFlow("ALL") // ALL, MISSING_PHONE, MISSING_EMAIL, HAS_BOTH

    // Combine all filters into a single StateFlow
    val filterState: StateFlow<ClientFilterState> = combine(
        clientSearchQuery,
        clientStatusFilter,
        clientCategoryFilter,
        clientCountryFilter
    ) { query, status, category, country ->
        ClientFilterState(query, status, category, country, clientMissingInfoFilter.value)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ClientFilterState())

    // Filtered clients list
    val filteredClients: StateFlow<List<ClientEntity>> = combine(
        allClients,
        filterState,
        clientMissingInfoFilter
    ) { clients, filter, missingFilter ->
        val query = filter.query.trim()
        val status = filter.status
        val category = filter.category
        val country = filter.country

        clients.filter { client ->
            val matchesQuery = query.isBlank() ||
                    client.companyName.contains(query, ignoreCase = true) ||
                    client.contactPerson.contains(query, ignoreCase = true) ||
                    client.email.contains(query, ignoreCase = true) ||
                    client.whatsapp.contains(query, ignoreCase = true) ||
                    client.location.contains(query, ignoreCase = true)

            val matchesStatus = when (status) {
                "ALL" -> true
                "OPTED_OUT" -> client.isOptedOut
                "NOT_GENERATED", "NO_MESSAGE" -> client.status == "NOT_GENERATED" || client.status == "NO_MESSAGE"
                else -> client.status == status
            }

            val matchesCategory = category == "ALL" || client.businessType.equals(category, ignoreCase = true)
            val matchesCountry = country == "ALL" || client.country.equals(country, ignoreCase = true)

            val matchesMissing = when (missingFilter) {
                "MISSING_PHONE" -> client.whatsapp.isBlank()
                "MISSING_EMAIL" -> client.email.isBlank()
                "HAS_BOTH" -> client.whatsapp.isNotBlank() && client.email.isNotBlank()
                else -> true
            }

            matchesQuery && matchesStatus && matchesCategory && matchesCountry && matchesMissing
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Import State ---
    private val _rawParsedSheet = MutableStateFlow<RawParsedSheet?>(null)
    val rawParsedSheet: StateFlow<RawParsedSheet?> = _rawParsedSheet.asStateFlow()

    private val _currentColumnMapping = MutableStateFlow(ColumnMapping())
    val currentColumnMapping: StateFlow<ColumnMapping> = _currentColumnMapping.asStateFlow()

    private val _validatedRows = MutableStateFlow<List<ValidatedLeadRow>>(emptyList())
    val validatedRows: StateFlow<List<ValidatedLeadRow>> = _validatedRows.asStateFlow()

    private val _isImporting = MutableStateFlow(false)
    val isImporting: StateFlow<Boolean> = _isImporting.asStateFlow()

    private val _importSummary = MutableStateFlow<String?>(null)
    val importSummary: StateFlow<String?> = _importSummary.asStateFlow()

    // --- AI Generator State ---
    val genSelectedClient = MutableStateFlow<ClientEntity?>(null)
    val genChannel = MutableStateFlow("WHATSAPP") // WHATSAPP or EMAIL
    val genTone = MutableStateFlow("Professional")
    val genLength = MutableStateFlow("Medium")
    val genOutreachTopic = MutableStateFlow("Website Development")
    val genService = MutableStateFlow("Modern Responsive Website")
    val genLanguage = MutableStateFlow("English")
    val genCallToAction = MutableStateFlow("Schedule a quick 10-minute preview")
    val genSubject = MutableStateFlow("")
    val genBody = MutableStateFlow("")
    val isAiGenerating = MutableStateFlow(false)
    val genStatusMessage = MutableStateFlow<String?>(null)

    // --- AI Provider Connection Test State ---
    val isTestingConnection = MutableStateFlow(false)
    val connectionTestResult = MutableStateFlow<ConnectionTestResult?>(null)

    // --- Confirmation & Send State ---
    val pendingSendConfirmationMessage = MutableStateFlow<MessageEntity?>(null)

    // --- Import Actions ---
    fun parseSelectedFile(uri: Uri, fileName: String, fileSize: Long) {
        viewModelScope.launch {
            _isImporting.value = true
            _importSummary.value = null
            try {
                val parsed = SpreadsheetParser.parseSpreadsheet(
                    context = getApplication(),
                    uri = uri,
                    fileName = fileName,
                    fileSize = fileSize
                )
                _rawParsedSheet.value = parsed
                _currentColumnMapping.value = parsed.detectedMappings

                recomputeValidatedRows(parsed, parsed.detectedMappings)
            } catch (e: Exception) {
                _importSummary.value = "Error parsing file: ${e.localizedMessage}"
            } finally {
                _isImporting.value = false
            }
        }
    }

    fun updateColumnMapping(mapping: ColumnMapping) {
        _currentColumnMapping.value = mapping
        _rawParsedSheet.value?.let { parsed ->
            recomputeValidatedRows(parsed, mapping)
        }
    }

    private fun recomputeValidatedRows(sheet: RawParsedSheet, mapping: ColumnMapping) {
        val existingPhones = allClients.value.mapNotNull {
            if (it.whatsapp.isNotBlank()) ContactNormalizer.normalizeForDuplicateCheck(it.whatsapp) else null
        }.toSet()

        val existingEmails = allClients.value.mapNotNull {
            if (it.email.isNotBlank()) it.email.trim().lowercase() else null
        }.toSet()

        val validated = SpreadsheetParser.validateRows(
            headers = sheet.headers,
            rows = sheet.rows,
            mapping = mapping,
            existingPhones = existingPhones,
            existingEmails = existingEmails
        )
        _validatedRows.value = validated
    }

    fun commitImport() {
        val rows = _validatedRows.value
        if (rows.isEmpty()) return

        viewModelScope.launch {
            _isImporting.value = true
            var insertedCount = 0
            var skippedFatalCount = 0

            val toInsert = mutableListOf<ClientEntity>()
            rows.forEach { row ->
                if (row.companyName.isBlank()) {
                    skippedFatalCount++
                } else {
                    toInsert.add(
                        ClientEntity(
                            companyName = row.companyName,
                            contactPerson = row.contactPerson,
                            email = row.email,
                            whatsapp = row.whatsapp,
                            countryCode = row.countryCode,
                            website = row.website,
                            businessType = row.businessType,
                            country = row.country,
                            location = row.location,
                            status = "NOT_GENERATED",
                            isOptedOut = false,
                            hasConsent = true
                        )
                    )
                    insertedCount++
                }
            }

            if (toInsert.isNotEmpty()) {
                repository.insertClients(toInsert)
            }

            _importSummary.value = "Successfully imported $insertedCount clients. " +
                    if (skippedFatalCount > 0) "($skippedFatalCount skipped due to missing company name)" else ""

            _rawParsedSheet.value = null
            _validatedRows.value = emptyList()
            _isImporting.value = false
        }
    }

    fun clearImportPreview() {
        _rawParsedSheet.value = null
        _validatedRows.value = emptyList()
        _importSummary.value = null
    }

    fun loadDemoLeads() {
        viewModelScope.launch {
            val demoClients = DemoDataProvider.getDemoClients()
            repository.insertClients(demoClients)
            Toast.makeText(getApplication(), "Loaded ${demoClients.size} realistic ROBOPIX demo leads!", Toast.LENGTH_SHORT).show()
        }
    }

    // --- Client Operations ---
    fun addOrUpdateClient(client: ClientEntity) {
        viewModelScope.launch {
            if (client.id == 0L) {
                repository.insertClient(client)
            } else {
                repository.updateClient(client)
            }
        }
    }

    fun deleteClient(id: Long) {
        viewModelScope.launch {
            repository.deleteClient(id)
        }
    }

    fun toggleOptOut(id: Long, currentOptedOut: Boolean) {
        viewModelScope.launch {
            repository.setClientOptOut(id, !currentOptedOut)
        }
    }

    fun toggleConsent(id: Long, currentConsent: Boolean) {
        viewModelScope.launch {
            val client = repository.getClient(id)
            if (client != null) {
                repository.updateClient(client.copy(hasConsent = !currentConsent))
            }
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.deleteAllClients()
            repository.deleteAllMessages()
            Toast.makeText(getApplication(), "All clients and message history cleared", Toast.LENGTH_SHORT).show()
        }
    }

    // --- AI Message Generation ---
    fun selectClientForAi(client: ClientEntity) {
        genSelectedClient.value = client
        val suggestedService = AiMessageGenerator.suggestServiceForClient(client)
        genService.value = suggestedService
        if (client.outreachTopic.isNotBlank()) {
            genOutreachTopic.value = client.outreachTopic
        }

        if (client.whatsapp.isBlank() && client.email.isNotBlank()) {
            genChannel.value = "EMAIL"
        } else if (client.whatsapp.isNotBlank()) {
            genChannel.value = "WHATSAPP"
        }

        genSubject.value = ""
        genBody.value = ""
        genStatusMessage.value = null
    }

    fun generateOutreachMessage() {
        val client = genSelectedClient.value ?: return
        if (client.isOptedOut) {
            genStatusMessage.value = "Cannot generate outreach: Contact has opted out of communication."
            return
        }

        viewModelScope.launch {
            isAiGenerating.value = true
            val provider = _profile.value.aiProvider
            val apiKey = when (provider) {
                "OpenAI" -> _profile.value.openAiApiKey
                "Anthropic Claude" -> _profile.value.anthropicApiKey
                else -> _profile.value.geminiApiKey
            }

            genStatusMessage.value = "Generating personalized outreach using $provider..."
            try {
                val result = AiMessageGenerator.generateMessage(
                    client = client,
                    channel = genChannel.value,
                    tone = genTone.value,
                    length = genLength.value,
                    outreachTopic = genOutreachTopic.value,
                    service = genService.value,
                    language = genLanguage.value,
                    callToAction = genCallToAction.value,
                    provider = provider,
                    apiKey = apiKey,
                    isTestMode = _profile.value.isTestMode
                )

                genSubject.value = result.subject
                genBody.value = result.body
                genStatusMessage.value = "Generated pitch successfully via ${result.providerUsed}. Review, edit, approve, or skip below."
            } catch (e: Exception) {
                genStatusMessage.value = "Generation error: ${e.localizedMessage}"
            } finally {
                isAiGenerating.value = false
            }
        }
    }

    fun saveGeneratedMessage(sendToReview: Boolean) {
        val client = genSelectedClient.value ?: return
        val bodyText = genBody.value.trim()
        if (bodyText.isBlank()) return

        viewModelScope.launch {
            val recipient = if (genChannel.value == "WHATSAPP") client.whatsapp else client.email
            val newStatus = if (sendToReview) "PENDING_REVIEW" else "DRAFT"

            val message = MessageEntity(
                clientId = client.id,
                clientCompanyName = client.companyName,
                channel = genChannel.value,
                recipient = recipient,
                subject = genSubject.value,
                body = bodyText,
                status = newStatus,
                tone = genTone.value,
                serviceOffered = genService.value,
                outreachTopic = genOutreachTopic.value,
                language = genLanguage.value,
                callToAction = genCallToAction.value,
                isTestMode = _profile.value.isTestMode
            )

            repository.insertMessage(message)
            repository.updateClientStatus(client.id, newStatus)

            Toast.makeText(
                getApplication(),
                if (sendToReview) "Saved to Approval Queue (Pending Review)" else "Saved as Draft",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // --- Approval Workflow Actions ---
    fun approveMessage(messageId: Long) {
        viewModelScope.launch {
            repository.updateMessageStatus(messageId, "APPROVED")
            val msg = repository.getMessage(messageId)
            if (msg != null) {
                repository.updateClientStatus(msg.clientId, "APPROVED")
            }
            Toast.makeText(getApplication(), "Message approved for outreach", Toast.LENGTH_SHORT).show()
        }
    }

    fun rejectMessage(messageId: Long) {
        viewModelScope.launch {
            repository.updateMessageStatus(messageId, "REJECTED")
            Toast.makeText(getApplication(), "Message rejected", Toast.LENGTH_SHORT).show()
        }
    }

    fun skipMessage(messageId: Long) {
        viewModelScope.launch {
            repository.updateMessageStatus(messageId, "SKIPPED")
            val msg = repository.getMessage(messageId)
            if (msg != null) {
                repository.updateClientStatus(msg.clientId, "SKIPPED")
            }
            Toast.makeText(getApplication(), "Message skipped", Toast.LENGTH_SHORT).show()
        }
    }

    fun skipClient(clientId: Long) {
        viewModelScope.launch {
            repository.updateClientStatus(clientId, "SKIPPED")
            Toast.makeText(getApplication(), "Client outreach skipped", Toast.LENGTH_SHORT).show()
        }
    }

    fun updateMessageDraft(messageId: Long, subject: String, body: String) {
        viewModelScope.launch {
            repository.editMessageContent(messageId, body, subject, "PENDING_REVIEW")
            Toast.makeText(getApplication(), "Updated and resubmitted for review", Toast.LENGTH_SHORT).show()
        }
    }

    // --- Sending Integrations ---
    fun initiateSend(context: Context, message: MessageEntity) {
        if (message.status != "APPROVED") {
            Toast.makeText(context, "Mandatory approval required before sending!", Toast.LENGTH_LONG).show()
            return
        }

        val client = allClients.value.firstOrNull { it.id == message.clientId }
        if (client != null && client.isOptedOut) {
            Toast.makeText(context, "Cannot send: Client is marked as Opted-Out", Toast.LENGTH_LONG).show()
            return
        }

        if (message.channel == "WHATSAPP") {
            sendViaWhatsApp(context, message)
        } else {
            sendViaEmail(context, message)
        }
    }

    private fun sendViaWhatsApp(context: Context, message: MessageEntity) {
        val phone = message.recipient
        if (!ContactNormalizer.isValidInternationalPhone(phone)) {
            Toast.makeText(
                context,
                "Invalid WhatsApp phone number: '$phone'. Must include country code and at least 7 digits.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        try {
            val waUrl = ContactNormalizer.buildWaMeUrl(phone, message.body)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(waUrl)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)

            viewModelScope.launch {
                repository.updateMessageStatus(message.id, "WHATSAPP_OPENED")
                repository.updateClientStatus(message.clientId, "WHATSAPP_OPENED", System.currentTimeMillis())
            }

            pendingSendConfirmationMessage.value = message
        } catch (e: Exception) {
            Toast.makeText(context, "Error opening WhatsApp: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    private fun sendViaEmail(context: Context, message: MessageEntity) {
        val email = message.recipient
        if (!ContactNormalizer.isValidEmail(email)) {
            Toast.makeText(context, "Invalid recipient email address: '$email'", Toast.LENGTH_LONG).show()
            return
        }

        try {
            val mailtoUri = ContactNormalizer.buildMailtoUri(email, message.subject, message.body)
            val intent = Intent(Intent.ACTION_SENDTO, mailtoUri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)

            viewModelScope.launch {
                repository.updateMessageStatus(message.id, "WHATSAPP_OPENED")
                repository.updateClientStatus(message.clientId, "WHATSAPP_OPENED", System.currentTimeMillis())
            }

            pendingSendConfirmationMessage.value = message
        } catch (e: Exception) {
            Toast.makeText(context, "Error opening email client: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    fun confirmSentResult(messageId: Long, actuallySent: Boolean, isFailed: Boolean = false) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val msg = repository.getMessage(messageId)
            if (actuallySent) {
                repository.updateMessageStatus(messageId, "MARKED_SENT")
                if (msg != null) {
                    repository.updateClientStatus(msg.clientId, "MARKED_SENT", now)
                }
                Toast.makeText(getApplication(), "Outreach status: Marked Sent (manual confirmation)", Toast.LENGTH_SHORT).show()
            } else if (isFailed) {
                repository.updateMessageStatus(messageId, "FAILED")
                if (msg != null) {
                    repository.updateClientStatus(msg.clientId, "FAILED", now)
                }
                Toast.makeText(getApplication(), "Marked as FAILED", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(getApplication(), "Status kept as WhatsApp Opened", Toast.LENGTH_SHORT).show()
            }
            pendingSendConfirmationMessage.value = null
        }
    }

    // --- Provider Connection Test ---
    fun testProviderConnection(provider: String, apiKey: String) {
        viewModelScope.launch {
            isTestingConnection.value = true
            connectionTestResult.value = null
            val result = AiMessageGenerator.testConnection(provider, apiKey)
            connectionTestResult.value = result
            isTestingConnection.value = false
        }
    }

    // --- Settings & Profile ---
    fun updateProfile(newProfile: RobopixProfile) {
        _profile.value = newProfile
        repository.saveProfile(newProfile)
        Toast.makeText(getApplication(), "Settings saved", Toast.LENGTH_SHORT).show()
    }
}
