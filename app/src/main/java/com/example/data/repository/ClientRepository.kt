package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.local.AppDao
import com.example.data.local.ClientEntity
import com.example.data.local.MessageEntity
import kotlinx.coroutines.flow.Flow

data class RobopixProfile(
    val companyName: String = "ROBOPIX",
    val senderName: String = "Alex from ROBOPIX",
    val senderEmail: String = "outreach@robopix.dev",
    val senderWhatsApp: String = "+14155550199",
    val portfolioUrl: String = "https://robopix.dev",
    val aiProvider: String = "Google Gemini", // Google Gemini, OpenAI, Anthropic Claude, Built-in Offline Engine
    val geminiApiKey: String = "",
    val openAiApiKey: String = "",
    val anthropicApiKey: String = "",
    val defaultOutreachTopic: String = "Website Development",
    val defaultService: String = "Modern Responsive Website",
    val defaultTone: String = "Professional",
    val defaultCta: String = "Schedule a quick 10-minute preview",
    val isTestMode: Boolean = false
)

class ClientRepository(
    private val appDao: AppDao,
    context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("robopix_prefs", Context.MODE_PRIVATE)

    // Flow streams
    val clients: Flow<List<ClientEntity>> = appDao.getAllClients()
    val messages: Flow<List<MessageEntity>> = appDao.getAllMessages()
    val pendingReviewMessages: Flow<List<MessageEntity>> = appDao.getPendingReviewMessages()
    val approvedMessages: Flow<List<MessageEntity>> = appDao.getApprovedMessages()

    suspend fun getClient(id: Long): ClientEntity? = appDao.getClientById(id)

    suspend fun insertClient(client: ClientEntity): Long = appDao.insertClient(client)

    suspend fun insertClients(clients: List<ClientEntity>): List<Long> = appDao.insertClients(clients)

    suspend fun updateClient(client: ClientEntity) = appDao.updateClient(client)

    suspend fun deleteClient(id: Long) = appDao.deleteClientById(id)

    suspend fun deleteAllClients() = appDao.deleteAllClients()

    suspend fun setClientOptOut(id: Long, optedOut: Boolean) = appDao.setClientOptOut(id, optedOut)

    suspend fun updateClientStatus(id: Long, status: String, timestamp: Long? = null) =
        appDao.updateClientStatus(id, status, timestamp)

    // Messages
    suspend fun getMessage(id: Long): MessageEntity? = appDao.getMessageById(id)

    suspend fun getMessagesForClient(clientId: Long): Flow<List<MessageEntity>> =
        appDao.getMessagesForClient(clientId)

    suspend fun insertMessage(message: MessageEntity): Long = appDao.insertMessage(message)

    suspend fun updateMessage(message: MessageEntity) = appDao.updateMessage(message)

    suspend fun updateMessageStatus(id: Long, status: String) =
        appDao.updateMessageStatus(id, status)

    suspend fun markMessageSent(id: Long) = appDao.markMessageSent(id)

    suspend fun editMessageContent(id: Long, body: String, subject: String, status: String) =
        appDao.editMessageContent(id, body, subject, status)

    suspend fun deleteMessage(id: Long) = appDao.deleteMessageById(id)

    suspend fun deleteAllMessages() = appDao.deleteAllMessages()

    // Settings Profile
    fun getProfile(): RobopixProfile {
        return RobopixProfile(
            companyName = prefs.getString("company_name", "ROBOPIX") ?: "ROBOPIX",
            senderName = prefs.getString("sender_name", "Alex from ROBOPIX") ?: "Alex from ROBOPIX",
            senderEmail = prefs.getString("sender_email", "outreach@robopix.dev") ?: "outreach@robopix.dev",
            senderWhatsApp = prefs.getString("sender_whatsapp", "+14155550199") ?: "+14155550199",
            portfolioUrl = prefs.getString("portfolio_url", "https://robopix.dev") ?: "https://robopix.dev",
            aiProvider = prefs.getString("ai_provider", "Google Gemini") ?: "Google Gemini",
            geminiApiKey = prefs.getString("gemini_api_key", "") ?: "",
            openAiApiKey = prefs.getString("openai_api_key", "") ?: "",
            anthropicApiKey = prefs.getString("anthropic_api_key", "") ?: "",
            defaultOutreachTopic = prefs.getString("default_outreach_topic", "Website Development") ?: "Website Development",
            defaultService = prefs.getString("default_service", "Modern Responsive Website") ?: "Modern Responsive Website",
            defaultTone = prefs.getString("default_tone", "Professional") ?: "Professional",
            defaultCta = prefs.getString("default_cta", "Schedule a quick 10-minute preview") ?: "Schedule a quick 10-minute preview",
            isTestMode = prefs.getBoolean("is_test_mode", false)
        )
    }

    fun saveProfile(profile: RobopixProfile) {
        prefs.edit().apply {
            putString("company_name", profile.companyName)
            putString("sender_name", profile.senderName)
            putString("sender_email", profile.senderEmail)
            putString("sender_whatsapp", profile.senderWhatsApp)
            putString("portfolio_url", profile.portfolioUrl)
            putString("ai_provider", profile.aiProvider)
            putString("gemini_api_key", profile.geminiApiKey)
            putString("openai_api_key", profile.openAiApiKey)
            putString("anthropic_api_key", profile.anthropicApiKey)
            putString("default_outreach_topic", profile.defaultOutreachTopic)
            putString("default_service", profile.defaultService)
            putString("default_tone", profile.defaultTone)
            putString("default_cta", profile.defaultCta)
            putBoolean("is_test_mode", profile.isTestMode)
            apply()
        }
    }
}
