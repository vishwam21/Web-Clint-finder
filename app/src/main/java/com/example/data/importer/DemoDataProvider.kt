package com.example.data.importer

import com.example.data.local.ClientEntity

object DemoDataProvider {

    fun getDemoClients(): List<ClientEntity> {
        val now = System.currentTimeMillis()
        return listOf(
            ClientEntity(
                companyName = "Velvet Roast Cafe",
                contactPerson = "Elena Gomez",
                email = "contact@velvetroast.com",
                whatsapp = "+14159823411",
                countryCode = "+1",
                website = "",
                businessType = "Cafe & Bakery",
                country = "United States",
                location = "Austin, TX",
                isOptedOut = false,
                hasConsent = true,
                outreachTopic = "Digital Menus for Cafes",
                status = "NOT_GENERATED",
                createdAt = now - 3600000 * 24 * 3
            ),
            ClientEntity(
                companyName = "Vanguard Architecture Group",
                contactPerson = "Marcus Sterling",
                email = "marcus@vanguardarch.co.uk",
                whatsapp = "+442079460833",
                countryCode = "+44",
                website = "http://vanguardarch-old.co.uk",
                businessType = "Architecture & Design",
                country = "United Kingdom",
                location = "London",
                isOptedOut = false,
                hasConsent = true,
                outreachTopic = "Website Development",
                status = "PENDING_REVIEW",
                createdAt = now - 3600000 * 24 * 2
            ),
            ClientEntity(
                companyName = "Trattoria Da Vinci",
                contactPerson = "Giuseppe Rossi",
                email = "prenotazioni@davincitrattoria.it",
                whatsapp = "+39025550198",
                countryCode = "+39",
                website = "",
                businessType = "Fine Dining Restaurant",
                country = "Italy",
                location = "Florence",
                isOptedOut = false,
                hasConsent = true,
                outreachTopic = "Digital Menus for Cafes & Restaurants",
                status = "APPROVED",
                createdAt = now - 3600000 * 24 * 2
            ),
            ClientEntity(
                companyName = "Apex Robotics Hub",
                contactPerson = "David Becker",
                email = "d.becker@apexlogistics.de",
                whatsapp = "+493012345678",
                countryCode = "+49",
                website = "https://apexlogistics.de",
                businessType = "Robotics & Automation",
                country = "Germany",
                location = "Hamburg",
                isOptedOut = false,
                hasConsent = true,
                outreachTopic = "Robotics Websites",
                status = "MARKED_SENT",
                lastContactDate = now - 3600000 * 18,
                createdAt = now - 3600000 * 24 * 4
            ),
            ClientEntity(
                companyName = "Horizon Dental Studio",
                contactPerson = "Dr. Claire Chen",
                email = "chen@horizondental.com.au",
                whatsapp = "+61298765410",
                countryCode = "+61",
                website = "",
                businessType = "Healthcare & Dental",
                country = "Australia",
                location = "Melbourne",
                isOptedOut = false,
                hasConsent = true,
                outreachTopic = "Website Redesign",
                status = "WHATSAPP_OPENED",
                lastContactDate = now - 3600000 * 2,
                createdAt = now - 3600000 * 24 * 1
            ),
            ClientEntity(
                companyName = "Solstice 3D Studios",
                contactPerson = "Aria Patel",
                email = "hello@solsticeyoga.ca",
                whatsapp = "+14165550921",
                countryCode = "+1",
                website = "http://solsticewellness-wix.site",
                businessType = "3D & Motion Design",
                country = "Canada",
                location = "Toronto",
                isOptedOut = false,
                hasConsent = true,
                outreachTopic = "3D Design Services",
                status = "DRAFT",
                createdAt = now - 3600000 * 12
            ),
            ClientEntity(
                companyName = "Komorebi Japanese Dining",
                contactPerson = "Kenji Sato",
                email = "info@komorebi-dining.jp",
                whatsapp = "+81355550144",
                countryCode = "+81",
                website = "",
                businessType = "Restaurant & Bar",
                country = "Japan",
                location = "Tokyo",
                isOptedOut = true, // Opted-out contact to test privacy rule
                hasConsent = false,
                outreachTopic = "Digital Menus for Cafes",
                status = "NOT_GENERATED",
                createdAt = now - 3600000 * 24 * 5
            )
        )
    }
}
