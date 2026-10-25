package com.example.domain.ai

import com.example.data.local.ClientEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class GeneratedMessageResult(
    val subject: String,
    val body: String,
    val isAiGenerated: Boolean = true,
    val providerUsed: String = "Built-in"
)

data class ConnectionTestResult(
    val success: Boolean,
    val message: String,
    val latencyMs: Long = 0
)

object AiMessageGenerator {

    val PROVIDERS = listOf(
        "Google Gemini",
        "OpenAI",
        "Anthropic Claude",
        "Built-in Offline Engine"
    )

    val OUTREACH_TOPICS = listOf(
        "Website Development",
        "Digital Menus & QR Ordering",
        "Robotics & Tech Websites",
        "3D Design & Modeling Showcase",
        "Website Redesign & Modernization",
        "Landing Pages & Lead Gen",
        "E-Commerce & Digital Catalogs"
    )

    val SERVICES = listOf(
        "Modern Responsive Website",
        "Professional Business Website",
        "High-Converting Landing Page",
        "Interactive Digital Menu (QR)",
        "Website Redesign & Modernization",
        "Digital Solutions & Online Presence",
        "E-Commerce & Service Showcase"
    )

    val TONES = listOf("Professional", "Friendly", "Concise")
    val LENGTHS = listOf("Short", "Medium", "Detailed")
    val LANGUAGES = listOf("English", "Spanish", "French", "German", "Italian", "Portuguese")
    val CTAS = listOf(
        "Schedule a quick 10-minute preview",
        "Reply here to see a free design mockup",
        "Let me know if you would like me to send a sample link",
        "Open to a quick chat this week?",
        "Check our ROBOPIX portfolio and let's connect"
    )

    /**
     * Determines the most relevant ROBOPIX service based on client attributes
     */
    fun suggestServiceForClient(client: ClientEntity): String {
        val type = client.businessType.lowercase()
        val hasWebsite = client.website.isNotBlank()

        return when {
            type.contains("cafe") || type.contains("restaurant") || type.contains("bistro") ||
                    type.contains("dining") || type.contains("bakery") || type.contains("bar") ->
                "Interactive Digital Menu (QR)"
            hasWebsite ->
                "Website Redesign & Modernization"
            type.contains("consult") || type.contains("legal") || type.contains("agency") ||
                    type.contains("clinic") || type.contains("dental") || type.contains("health") ->
                "Professional Business Website"
            type.contains("fitness") || type.contains("yoga") || type.contains("event") ->
                "High-Converting Landing Page"
            type.contains("robot") || type.contains("tech") || type.contains("ai") || type.contains("software") ->
                "Robotics & Tech Websites"
            else ->
                "Modern Responsive Website"
        }
    }

    suspend fun testConnection(provider: String, apiKey: String): ConnectionTestResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()

        if (provider == "Built-in Offline Engine") {
            return@withContext ConnectionTestResult(
                success = true,
                message = "Built-in intelligent synthesizer is ready (runs on-device with zero API cost).",
                latencyMs = 2
            )
        }

        if (apiKey.isBlank()) {
            return@withContext ConnectionTestResult(
                success = false,
                message = "API key cannot be empty. Please enter your $provider API key."
            )
        }

        try {
            when (provider) {
                "Google Gemini" -> testGemini(apiKey, startTime)
                "OpenAI" -> testOpenAi(apiKey, startTime)
                "Anthropic Claude" -> testAnthropic(apiKey, startTime)
                else -> ConnectionTestResult(success = true, message = "Provider ready", latencyMs = 5)
            }
        } catch (e: Exception) {
            ConnectionTestResult(
                success = false,
                message = "Connection failed: ${e.localizedMessage ?: "Unknown network error"}"
            )
        }
    }

    private fun testGemini(apiKey: String, startTime: Long): ConnectionTestResult {
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=$apiKey"
        val requestJson = JSONObject().apply {
            put("contents", JSONArray().put(JSONObject().apply {
                put("parts", JSONArray().put(JSONObject().apply {
                    put("text", "Ping test. Respond with OK.")
                }))
            }))
        }

        val url = URL(endpoint)
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 10000
            readTimeout = 10000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
        }

        conn.outputStream.use { os ->
            os.write(requestJson.toString().toByteArray(Charsets.UTF_8))
        }

        val code = conn.responseCode
        val latency = System.currentTimeMillis() - startTime
        return if (code in 200..299) {
            ConnectionTestResult(true, "Successfully connected to Google Gemini! (Latency: ${latency}ms)", latency)
        } else {
            val err = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: "HTTP $code"
            ConnectionTestResult(false, "Gemini returned error ($code): ${parseErrorMessage(err)}")
        }
    }

    private fun testOpenAi(apiKey: String, startTime: Long): ConnectionTestResult {
        val endpoint = "https://api.openai.com/v1/models"
        val url = URL(endpoint)
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10000
            readTimeout = 10000
            setRequestProperty("Authorization", "Bearer $apiKey")
        }

        val code = conn.responseCode
        val latency = System.currentTimeMillis() - startTime
        return if (code in 200..299) {
            ConnectionTestResult(true, "Successfully connected to OpenAI API! (Latency: ${latency}ms)", latency)
        } else {
            val err = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: "HTTP $code"
            ConnectionTestResult(false, "OpenAI returned error ($code): ${parseErrorMessage(err)}")
        }
    }

    private fun testAnthropic(apiKey: String, startTime: Long): ConnectionTestResult {
        val endpoint = "https://api.anthropic.com/v1/messages"
        val requestJson = JSONObject().apply {
            put("model", "claude-3-5-haiku-20241022")
            put("max_tokens", 10)
            put("messages", JSONArray().put(JSONObject().apply {
                put("role", "user")
                put("content", "ping")
            }))
        }

        val url = URL(endpoint)
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 10000
            readTimeout = 10000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("x-api-key", apiKey)
            setRequestProperty("anthropic-version", "2023-06-01")
        }

        conn.outputStream.use { os ->
            os.write(requestJson.toString().toByteArray(Charsets.UTF_8))
        }

        val code = conn.responseCode
        val latency = System.currentTimeMillis() - startTime
        return if (code in 200..299) {
            ConnectionTestResult(true, "Successfully connected to Anthropic Claude! (Latency: ${latency}ms)", latency)
        } else {
            val err = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: "HTTP $code"
            ConnectionTestResult(false, "Anthropic returned error ($code): ${parseErrorMessage(err)}")
        }
    }

    private fun parseErrorMessage(raw: String): String {
        return try {
            val json = JSONObject(raw)
            if (json.has("error")) {
                val errObj = json.get("error")
                if (errObj is JSONObject) errObj.optString("message", raw) else errObj.toString()
            } else {
                raw.take(120)
            }
        } catch (e: Exception) {
            raw.take(120)
        }
    }

    suspend fun generateMessage(
        client: ClientEntity,
        channel: String, // "WHATSAPP" or "EMAIL"
        tone: String = "Professional",
        length: String = "Medium",
        outreachTopic: String = "Website Development",
        service: String = "Modern Responsive Website",
        language: String = "English",
        callToAction: String = "Schedule a quick 10-minute preview",
        provider: String = "Google Gemini",
        apiKey: String = "",
        isTestMode: Boolean = false
    ): GeneratedMessageResult = withContext(Dispatchers.IO) {
        val topic = if (outreachTopic.isNotBlank()) outreachTopic else service

        if (apiKey.isNotBlank() && provider != "Built-in Offline Engine") {
            try {
                val remoteResult = when (provider) {
                    "Google Gemini" -> callGeminiApi(apiKey, client, channel, tone, length, topic, service, language, callToAction)
                    "OpenAI" -> callOpenAiApi(apiKey, client, channel, tone, length, topic, service, language, callToAction)
                    "Anthropic Claude" -> callAnthropicApi(apiKey, client, channel, tone, length, topic, service, language, callToAction)
                    else -> null
                }
                if (remoteResult != null) {
                    val finalBody = if (isTestMode) "[TEST MODE / SANDBOX]\n" + remoteResult.body else remoteResult.body
                    return@withContext remoteResult.copy(body = finalBody)
                }
            } catch (e: Exception) {
                // Graceful fallback to deterministic synthesizer
            }
        }

        val localResult = synthesizePersonalizedMessage(
            client = client,
            channel = channel,
            tone = tone,
            length = length,
            outreachTopic = topic,
            service = service,
            language = language,
            callToAction = callToAction
        )

        val finalBody = if (isTestMode) "[TEST MODE / SANDBOX]\n" + localResult.body else localResult.body
        return@withContext localResult.copy(body = finalBody)
    }

    private fun buildSystemPrompt(
        client: ClientEntity,
        channel: String,
        tone: String,
        length: String,
        outreachTopic: String,
        service: String,
        language: String,
        callToAction: String
    ): String = buildString {
        appendLine("You are an expert business outreach copywriter for ROBOPIX, specializing in modern web development, robotics sites, digital menus, and digital solutions.")
        appendLine("Write a personalized $channel outreach message for:")
        appendLine("Business Name: ${client.companyName}")
        if (client.contactPerson.isNotBlank()) appendLine("Contact Person: ${client.contactPerson}")
        if (client.businessType.isNotBlank()) appendLine("Business Category: ${client.businessType}")
        if (client.location.isNotBlank()) appendLine("Location: ${client.location}")
        if (client.website.isNotBlank()) appendLine("Current Website: ${client.website}")
        appendLine("Outreach Topic: $outreachTopic")
        appendLine("Service Offered: $service")
        appendLine("Tone: $tone")
        appendLine("Length: $length")
        appendLine("Language: $language")
        appendLine("Call to Action: $callToAction")
        appendLine("STRICT RULES:")
        appendLine("- Do NOT invent fake customer results, fake testimonials, or fake personal relationships.")
        appendLine("- Write naturally and concisely, optimized for mobile reading.")
        if (channel == "EMAIL") {
            appendLine("Return a clean JSON object with keys: 'subject' (email subject line) and 'body' (the email text).")
        } else {
            appendLine("Return a clean JSON object with keys: 'subject' (empty string) and 'body' (the WhatsApp message text).")
        }
    }

    private fun callGeminiApi(
        apiKey: String,
        client: ClientEntity,
        channel: String,
        tone: String,
        length: String,
        outreachTopic: String,
        service: String,
        language: String,
        callToAction: String
    ): GeneratedMessageResult? {
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=$apiKey"
        val prompt = buildSystemPrompt(client, channel, tone, length, outreachTopic, service, language, callToAction)

        val requestJson = JSONObject().apply {
            put("contents", JSONArray().put(JSONObject().apply {
                put("parts", JSONArray().put(JSONObject().apply {
                    put("text", prompt)
                }))
            }))
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
            })
        }

        val url = URL(endpoint)
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15000
            readTimeout = 20000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
        }

        conn.outputStream.use { os ->
            os.write(requestJson.toString().toByteArray(Charsets.UTF_8))
        }

        if (conn.responseCode in 200..299) {
            val responseText = conn.inputStream.bufferedReader().use { it.readText() }
            val root = JSONObject(responseText)
            val candidates = root.optJSONArray("candidates")
            val candidate = candidates?.optJSONObject(0)
            val content = candidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text")

            if (!text.isNullOrBlank()) {
                val parsed = JSONObject(text)
                return GeneratedMessageResult(
                    subject = parsed.optString("subject", "Modern Web Solution for ${client.companyName} | ROBOPIX"),
                    body = parsed.optString("body", ""),
                    isAiGenerated = true,
                    providerUsed = "Google Gemini"
                )
            }
        }
        return null
    }

    private fun callOpenAiApi(
        apiKey: String,
        client: ClientEntity,
        channel: String,
        tone: String,
        length: String,
        outreachTopic: String,
        service: String,
        language: String,
        callToAction: String
    ): GeneratedMessageResult? {
        val endpoint = "https://api.openai.com/v1/chat/completions"
        val prompt = buildSystemPrompt(client, channel, tone, length, outreachTopic, service, language, callToAction)

        val requestJson = JSONObject().apply {
            put("model", "gpt-4o-mini")
            put("response_format", JSONObject().apply { put("type", "json_object") })
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", "You are an outreach copywriter for ROBOPIX. Respond ONLY with valid JSON having 'subject' and 'body'.")
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                })
            })
        }

        val url = URL(endpoint)
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15000
            readTimeout = 20000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Authorization", "Bearer $apiKey")
        }

        conn.outputStream.use { os ->
            os.write(requestJson.toString().toByteArray(Charsets.UTF_8))
        }

        if (conn.responseCode in 200..299) {
            val responseText = conn.inputStream.bufferedReader().use { it.readText() }
            val root = JSONObject(responseText)
            val choices = root.optJSONArray("choices")
            val choice = choices?.optJSONObject(0)
            val msg = choice?.optJSONObject("message")
            val content = msg?.optString("content")

            if (!content.isNullOrBlank()) {
                val parsed = JSONObject(content)
                return GeneratedMessageResult(
                    subject = parsed.optString("subject", "Web Outreach for ${client.companyName}"),
                    body = parsed.optString("body", ""),
                    isAiGenerated = true,
                    providerUsed = "OpenAI"
                )
            }
        }
        return null
    }

    private fun callAnthropicApi(
        apiKey: String,
        client: ClientEntity,
        channel: String,
        tone: String,
        length: String,
        outreachTopic: String,
        service: String,
        language: String,
        callToAction: String
    ): GeneratedMessageResult? {
        val endpoint = "https://api.anthropic.com/v1/messages"
        val prompt = buildSystemPrompt(client, channel, tone, length, outreachTopic, service, language, callToAction)

        val requestJson = JSONObject().apply {
            put("model", "claude-3-5-haiku-20241022")
            put("max_tokens", 800)
            put("messages", JSONArray().put(JSONObject().apply {
                put("role", "user")
                put("content", prompt + "\nProvide your answer ONLY in JSON format: {\"subject\": \"...\", \"body\": \"...\"}")
            }))
        }

        val url = URL(endpoint)
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15000
            readTimeout = 20000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("x-api-key", apiKey)
            setRequestProperty("anthropic-version", "2023-06-01")
        }

        conn.outputStream.use { os ->
            os.write(requestJson.toString().toByteArray(Charsets.UTF_8))
        }

        if (conn.responseCode in 200..299) {
            val responseText = conn.inputStream.bufferedReader().use { it.readText() }
            val root = JSONObject(responseText)
            val contents = root.optJSONArray("content")
            val text = contents?.optJSONObject(0)?.optString("text")

            if (!text.isNullOrBlank()) {
                val cleanJson = if (text.contains("{")) {
                    text.substring(text.indexOf("{"), text.lastIndexOf("}") + 1)
                } else text
                val parsed = JSONObject(cleanJson)
                return GeneratedMessageResult(
                    subject = parsed.optString("subject", "Outreach for ${client.companyName}"),
                    body = parsed.optString("body", ""),
                    isAiGenerated = true,
                    providerUsed = "Anthropic Claude"
                )
            }
        }
        return null
    }

    private fun synthesizePersonalizedMessage(
        client: ClientEntity,
        channel: String,
        tone: String,
        length: String,
        outreachTopic: String,
        service: String,
        language: String,
        callToAction: String
    ): GeneratedMessageResult {
        val greetingName = if (client.contactPerson.isNotBlank()) {
            client.contactPerson.split(" ").firstOrNull() ?: client.contactPerson
        } else {
            client.companyName
        }

        val hasExistingWeb = client.website.isNotBlank()
        val locSuffix = if (client.location.isNotBlank()) " in ${client.location}" else ""
        val categoryLabel = if (client.businessType.isNotBlank()) client.businessType.lowercase() else "business"
        val topicLower = outreachTopic.lowercase()

        val isEmail = channel.equals("EMAIL", ignoreCase = true)

        val subject = if (isEmail) {
            when (tone) {
                "Friendly" -> "Quick idea for ${client.companyName}'s $outreachTopic"
                "Concise" -> "ROBOPIX: $outreachTopic for ${client.companyName}"
                else -> "$outreachTopic & digital presence for ${client.companyName} | ROBOPIX"
            }
        } else ""

        val body = buildString {
            // Greeting
            when (language) {
                "Spanish" -> appendLine(if (isEmail) "Hola $greetingName," else "Hola $greetingName 👋")
                "French" -> appendLine(if (isEmail) "Bonjour $greetingName," else "Bonjour $greetingName 👋")
                "German" -> appendLine(if (isEmail) "Hallo $greetingName," else "Hallo $greetingName 👋")
                "Italian" -> appendLine(if (isEmail) "Ciao $greetingName," else "Ciao $greetingName 👋")
                "Portuguese" -> appendLine(if (isEmail) "Olá $greetingName," else "Olá $greetingName 👋")
                else -> {
                    when (tone) {
                        "Friendly" -> appendLine(if (isEmail) "Hi $greetingName," else "Hi $greetingName 👋")
                        "Concise" -> appendLine(if (isEmail) "Hello $greetingName," else "Hello $greetingName,")
                        else -> appendLine(if (isEmail) "Dear $greetingName," else "Hello $greetingName,")
                    }
                }
            }
            appendLine()

            // Outreach Topic Hook
            when {
                topicLower.contains("menu") -> {
                    appendLine("I came across ${client.companyName}$locSuffix and noticed your popular $categoryLabel offerings.")
                    appendLine("At ROBOPIX, we build clean, interactive digital QR menus and fast mobile ordering experiences specifically designed for cafes, bars, and dining venues.")
                }
                topicLower.contains("robot") || topicLower.contains("tech") || topicLower.contains("ai") -> {
                    appendLine("I was reviewing innovative ${categoryLabel} companies and came across ${client.companyName}$locSuffix.")
                    appendLine("At ROBOPIX, we specialize in high-tech, robotics, and engineering showcase websites built for technical credibility and clear client inquiries.")
                }
                topicLower.contains("3d") || topicLower.contains("design") -> {
                    appendLine("I came across ${client.companyName}$locSuffix while researching creative and design ventures.")
                    appendLine("At ROBOPIX, we engineer interactive 3D web experiences, portfolio showcases, and high-performance visual landing pages.")
                }
                topicLower.contains("redesign") || hasExistingWeb -> {
                    appendLine("I came across ${client.companyName}$locSuffix while researching leading ${categoryLabel}s.")
                    appendLine("At ROBOPIX, we specialize in website redesigns and performance upgrades to help established businesses convert more visitors into active clients.")
                }
                topicLower.contains("landing") -> {
                    appendLine("I was looking into ${client.companyName}$locSuffix and wanted to reach out directly.")
                    appendLine("At ROBOPIX, we craft dedicated, high-converting landing pages built for mobile speed and clear client inquiries.")
                }
                else -> {
                    appendLine("I came across ${client.companyName}$locSuffix and wanted to connect with your team regarding your $outreachTopic.")
                    appendLine("At ROBOPIX, we build custom responsive business websites that look sharp across all devices and make it effortless for customers to reach you.")
                }
            }
            appendLine()

            // Value & Points
            if (length != "Short") {
                appendLine("A few key things we ensure with every build:")
                appendLine("• 100% mobile-first design and lightning-fast loading speeds")
                appendLine("• Direct WhatsApp, calling, and inquiry integrations")
                if (topicLower.contains("menu")) {
                    appendLine("• Instant menu updates without needing to reprint anything")
                } else if (topicLower.contains("3d") || topicLower.contains("robot")) {
                    appendLine("• Smooth interactive animations with clean responsive architecture")
                } else {
                    appendLine("• Clean, modern aesthetics aligned with your brand identity")
                }
                if (length == "Detailed") {
                    appendLine("• On-page search optimization and modern responsive architecture")
                    appendLine("• Zero maintenance headache with dedicated technical support")
                }
                appendLine()
            }

            // Call to Action
            appendLine(callToAction + ".")
            appendLine()

            // Sign-off
            if (isEmail) {
                appendLine("Best regards,")
                appendLine("The ROBOPIX Team")
                appendLine("Web Development & Digital Solutions")
            } else {
                appendLine("— ROBOPIX Web Development")
            }
        }.trim()

        return GeneratedMessageResult(
            subject = subject,
            body = body,
            isAiGenerated = true,
            providerUsed = "Built-in Synthesizer"
        )
    }
}
