package com.example

import com.example.data.importer.SpreadsheetParser
import com.example.data.local.ClientEntity
import com.example.domain.ai.AiMessageGenerator
import com.example.domain.util.ContactNormalizer
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testWaMeNormalization() {
        val raw = "+1 (415) 555-2671"
        val normalized = ContactNormalizer.normalizeForWaMe(raw)
        // Must contain digits only without plus sign
        assertEquals("14155552671", normalized)
        assertFalse(normalized.contains("+"))
    }

    @Test
    fun testWaMeUrlConstruction() {
        val phone = "+44 20 7946 0833"
        val text = "Hi Alex, check our ROBOPIX website portfolio"
        val url = ContactNormalizer.buildWaMeUrl(phone, text)

        assertTrue(url.startsWith("https://wa.me/442079460833?text="))
        assertFalse(url.contains("+44"))
        assertTrue(url.contains("ROBOPIX"))
    }

    @Test
    fun testPhoneValidation() {
        assertTrue(ContactNormalizer.isValidInternationalPhone("+14155552671"))
        assertTrue(ContactNormalizer.isValidInternationalPhone("442079460833"))
        assertFalse(ContactNormalizer.isValidInternationalPhone("123"))
        assertFalse(ContactNormalizer.isValidInternationalPhone(""))
    }

    @Test
    fun testAutoDetectHeaders() {
        val headers = listOf("Business Name", "Contact Person", "Email Address", "Mobile Phone", "Category", "City")
        val mapping = SpreadsheetParser.autoDetectColumns(headers)

        assertEquals("Business Name", mapping.companyNameCol)
        assertEquals("Contact Person", mapping.contactNameCol)
        assertEquals("Email Address", mapping.emailCol)
        assertEquals("Mobile Phone", mapping.whatsappCol)
        assertEquals("Category", mapping.businessTypeCol)
        assertEquals("City", mapping.locationCol)
    }

    @Test
    fun testAiServiceSuggestion() {
        val cafe = ClientEntity(companyName = "Bella Cafe", businessType = "Cafe & Bakery")
        val cafeService = AiMessageGenerator.suggestServiceForClient(cafe)
        assertEquals("Interactive Digital Menu (QR)", cafeService)

        val dental = ClientEntity(companyName = "City Dental", businessType = "Dental Clinic")
        val dentalService = AiMessageGenerator.suggestServiceForClient(dental)
        assertEquals("Professional Business Website", dentalService)

        val existingWeb = ClientEntity(companyName = "Old Co", businessType = "Retail", website = "http://old.com")
        val redesignService = AiMessageGenerator.suggestServiceForClient(existingWeb)
        assertEquals("Website Redesign & Modernization", redesignService)
    }

    @Test
    fun testAiMessageGeneration() = runBlocking {
        val client = ClientEntity(
            companyName = "Artisan Bakery",
            contactPerson = "Sophie Martin",
            email = "sophie@artisanbakery.com",
            whatsapp = "+33123456789",
            businessType = "Bakery & Cafe",
            location = "Paris"
        )

        val result = AiMessageGenerator.generateMessage(
            client = client,
            channel = "WHATSAPP",
            tone = "Friendly",
            length = "Medium",
            outreachTopic = "Digital Menus for Cafes",
            service = "Interactive Digital Menu (QR)",
            language = "English",
            callToAction = "Schedule a quick 10-minute preview"
        )

        assertTrue(result.body.contains("Sophie"))
        assertTrue(result.body.contains("ROBOPIX"))
        assertTrue(result.body.contains("Artisan Bakery"))
        assertTrue(result.body.contains("menu", ignoreCase = true))
    }

    @Test
    fun testRoboticsOutreachTopic() = runBlocking {
        val client = ClientEntity(
            companyName = "Apex Robotics",
            contactPerson = "David Becker",
            email = "david@apexrobotics.io",
            whatsapp = "+493012345678",
            businessType = "Robotics & Automation",
            location = "Berlin"
        )

        val result = AiMessageGenerator.generateMessage(
            client = client,
            channel = "WHATSAPP",
            outreachTopic = "Robotics Websites",
            service = "Robotics & Tech Websites"
        )

        assertTrue(result.body.contains("Apex Robotics"))
        assertTrue(result.body.contains("ROBOPIX"))
        assertTrue(result.body.contains("robotics", ignoreCase = true) || result.body.contains("technical", ignoreCase = true))
    }

    @Test
    fun testCountryCodeHeaderDetection() {
        val headers = listOf("Company", "Contact", "Dial Code", "Mobile", "Email", "Sector")
        val mapping = SpreadsheetParser.autoDetectColumns(headers)

        assertEquals("Company", mapping.companyNameCol)
        assertEquals("Dial Code", mapping.countryCodeCol)
        assertEquals("Mobile", mapping.whatsappCol)
    }

    @Test
    fun testTestModeSandboxBanner() = runBlocking {
        val client = ClientEntity(
            companyName = "Quantum Lab",
            contactPerson = "Elena",
            whatsapp = "+14155550199"
        )

        val result = AiMessageGenerator.generateMessage(
            client = client,
            channel = "WHATSAPP",
            isTestMode = true
        )

        assertTrue(result.body.startsWith("[TEST MODE / SANDBOX]"))
    }

    @Test
    fun testBuiltInOfflineProviderConnection() = runBlocking {
        val res = AiMessageGenerator.testConnection("Built-in Offline Engine", "")
        assertTrue(res.success)
        assertTrue(res.latencyMs in 0..100)
    }
}
