package com.example.domain.util

import android.net.Uri
import java.net.URLEncoder

object ContactNormalizer {

    /**
     * Normalizes a phone number to international format digits-only without '+' or non-digit chars.
     * E.g. "+1 (415) 555-2671" -> "14155552671"
     */
    fun normalizeForWaMe(phone: String): String {
        // Strip out non-digit characters
        val digitsOnly = phone.replace(Regex("[^0-9]"), "")
        return digitsOnly
    }

    /**
     * Normalizes a phone number for duplicate checking (digits only).
     */
    fun normalizeForDuplicateCheck(phone: String): String {
        return phone.replace(Regex("[^0-9]"), "")
    }

    /**
     * Validates if the phone number has enough digits for international routing (typically >= 7-8 digits).
     */
    fun isValidInternationalPhone(phone: String): Boolean {
        val digits = normalizeForWaMe(phone)
        return digits.length in 7..15
    }

    /**
     * Constructs the official wa.me link:
     * https://wa.me/INTERNATIONAL_PHONE_NUMBER?text=URL_ENCODED_MESSAGE
     * Strict requirement: No plus sign in the phone number path!
     */
    fun buildWaMeUrl(phone: String, messageText: String): String {
        val cleanPhone = normalizeForWaMe(phone)
        val encodedText = URLEncoder.encode(messageText, "UTF-8")
            .replace("+", "%20") // WhatsApp prefers %20 for spaces
        return "https://wa.me/$cleanPhone?text=$encodedText"
    }

    /**
     * Constructs a mailto URI with recipient, subject, and body.
     */
    fun buildMailtoUri(recipient: String, subject: String, body: String): Uri {
        val encodedSubject = Uri.encode(subject)
        val encodedBody = Uri.encode(body)
        val uriString = "mailto:$recipient?subject=$encodedSubject&body=$encodedBody"
        return Uri.parse(uriString)
    }

    /**
     * Validates an email address.
     */
    fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return emailRegex.matches(email.trim())
    }
}
