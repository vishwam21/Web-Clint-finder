package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clients")
data class ClientEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val companyName: String,
    val contactPerson: String = "",
    val email: String = "",
    val whatsapp: String = "",
    val countryCode: String = "",
    val website: String = "",
    val businessType: String = "",
    val country: String = "",
    val location: String = "",
    val hasConsent: Boolean = true,
    val isOptedOut: Boolean = false,
    val status: String = "NOT_GENERATED", // NOT_GENERATED, DRAFT, PENDING_REVIEW, APPROVED, WHATSAPP_OPENED, MARKED_SENT, SKIPPED, OPTED_OUT
    val outreachTopic: String = "",
    val lastContactDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val notes: String = ""
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val clientId: Long,
    val clientCompanyName: String,
    val channel: String = "WHATSAPP", // WHATSAPP or EMAIL
    val recipient: String,
    val subject: String = "", // For email
    val body: String,
    val status: String = "DRAFT", // DRAFT, PENDING_REVIEW, APPROVED, WHATSAPP_OPENED, MARKED_SENT, SKIPPED, REJECTED, FAILED
    val tone: String = "Professional",
    val serviceOffered: String = "Modern Responsive Website",
    val outreachTopic: String = "Website Development",
    val language: String = "English",
    val callToAction: String = "Schedule a 10-minute preview",
    val isTestMode: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val sentAt: Long? = null,
    val errorMessage: String? = null
)
