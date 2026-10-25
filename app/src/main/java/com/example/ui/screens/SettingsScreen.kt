package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.ai.AiMessageGenerator
import com.example.ui.theme.EmailBlue
import com.example.ui.theme.RobopixBackground
import com.example.ui.theme.RobopixBorder
import com.example.ui.theme.RobopixBorderLight
import com.example.ui.theme.RobopixSurface
import com.example.ui.theme.RobopixSurfaceVariant
import com.example.ui.theme.RobopixViolet
import com.example.ui.theme.RobopixVioletBright
import com.example.ui.theme.RobopixVioletContainer
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.viewmodel.MessengerViewModel

@Composable
fun SettingsScreen(
    viewModel: MessengerViewModel
) {
    val context = LocalContext.current
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val allClients by viewModel.allClients.collectAsStateWithLifecycle()
    val isTestingConnection by viewModel.isTestingConnection.collectAsStateWithLifecycle()
    val connectionResult by viewModel.connectionTestResult.collectAsStateWithLifecycle()

    var companyName by remember(profile) { mutableStateOf(profile.companyName) }
    var senderName by remember(profile) { mutableStateOf(profile.senderName) }
    var senderEmail by remember(profile) { mutableStateOf(profile.senderEmail) }
    var senderWhatsApp by remember(profile) { mutableStateOf(profile.senderWhatsApp) }
    var portfolioUrl by remember(profile) { mutableStateOf(profile.portfolioUrl) }

    var aiProvider by remember(profile) { mutableStateOf(profile.aiProvider) }
    var geminiApiKey by remember(profile) { mutableStateOf(profile.geminiApiKey) }
    var openAiApiKey by remember(profile) { mutableStateOf(profile.openAiApiKey) }
    var anthropicApiKey by remember(profile) { mutableStateOf(profile.anthropicApiKey) }
    var defaultOutreachTopic by remember(profile) { mutableStateOf(profile.defaultOutreachTopic) }
    var defaultService by remember(profile) { mutableStateOf(profile.defaultService) }
    var defaultCta by remember(profile) { mutableStateOf(profile.defaultCta) }
    var isTestMode by remember(profile) { mutableStateOf(profile.isTestMode) }

    var showProviderDropdown by remember { mutableStateOf(false) }
    var showApiKey by remember { mutableStateOf(false) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(RobopixBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "CONFIGURATION & PREFERENCES",
                color = RobopixVioletBright,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "Settings & AI Connection",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Manage your ROBOPIX brand identity, AI provider API keys, test connection status, outreach topics, and on-device data.",
                color = TextSecondary,
                fontSize = 13.sp
            )
        }

        // 1. AI Provider Connection Card (User Requested Core Feature)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, RobopixViolet.copy(alpha = 0.6f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = RobopixSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(RobopixVioletContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Key, contentDescription = null, tint = RobopixVioletBright, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("AI PROVIDER & API CONNECTION", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Select Provider & Secure Key", color = TextSecondary, fontSize = 11.sp)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(RobopixVioletContainer)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(aiProvider, color = RobopixVioletBright, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Provider Selector Dropdown
                    Text("Selected AI Provider", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(RobopixBackground)
                                .border(1.dp, RobopixBorder, RoundedCornerShape(8.dp))
                                .clickable { showProviderDropdown = true }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(aiProvider, color = RobopixVioletBright, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextSecondary)
                        }

                        DropdownMenu(
                            expanded = showProviderDropdown,
                            onDismissRequest = { showProviderDropdown = false },
                            modifier = Modifier.background(RobopixSurface)
                        ) {
                            AiMessageGenerator.PROVIDERS.forEach { prov ->
                                DropdownMenuItem(
                                    text = { Text(prov, color = TextPrimary, fontSize = 12.sp) },
                                    onClick = {
                                        aiProvider = prov
                                        showProviderDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // API Key Field based on selected provider
                    if (aiProvider != "Built-in Offline Engine") {
                        val currentKey = when (aiProvider) {
                            "OpenAI" -> openAiApiKey
                            "Anthropic Claude" -> anthropicApiKey
                            else -> geminiApiKey
                        }

                        Text("$aiProvider API Key", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = currentKey,
                            onValueChange = { newVal ->
                                when (aiProvider) {
                                    "OpenAI" -> openAiApiKey = newVal
                                    "Anthropic Claude" -> anthropicApiKey = newVal
                                    else -> geminiApiKey = newVal
                                }
                            },
                            label = { Text("Enter $aiProvider API Key") },
                            placeholder = { Text("e.g. AIzaSy... or sk-...", color = TextSecondary, fontSize = 12.sp) },
                            visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showApiKey = !showApiKey }) {
                                    Icon(
                                        imageVector = if (showApiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle key visibility",
                                        tint = TextSecondary
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = RobopixBackground,
                                unfocusedContainerColor = RobopixBackground,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_provider_api_key")
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Security guarantee: Your API key is stored securely on-device using local private SharedPreferences and is never logged or exposed in public repositories.",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Test Connection Button & Result
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    viewModel.testProviderConnection(aiProvider, currentKey)
                                },
                                enabled = !isTestingConnection,
                                colors = ButtonDefaults.buttonColors(containerColor = RobopixVioletContainer),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("btn_test_connection")
                            ) {
                                if (isTestingConnection) {
                                    CircularProgressIndicator(color = RobopixVioletBright, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Testing...", color = RobopixVioletBright)
                                } else {
                                    Icon(Icons.Default.Refresh, contentDescription = null, tint = RobopixVioletBright, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Test Connection", color = RobopixVioletBright, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }

                        // Connection Result Banner
                        AnimatedVisibility(visible = connectionResult != null) {
                            val res = connectionResult
                            if (res != null) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (res.success) StatusSuccess.copy(alpha = 0.15f) else StatusDanger.copy(alpha = 0.15f))
                                        .border(1.dp, if (res.success) StatusSuccess else StatusDanger, RoundedCornerShape(8.dp))
                                        .padding(10.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (res.success) Icons.Default.CheckCircle else Icons.Default.Close,
                                            contentDescription = null,
                                            tint = if (res.success) StatusSuccess else StatusDanger,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = res.message,
                                            color = TextPrimary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(RobopixBackground)
                                .border(1.dp, RobopixBorder, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "Using Built-in Intelligent Synthesizer: Runs 100% on-device with zero external API dependencies, zero latency, and zero cost. Generates authentic, non-hallucinatory outreach tailored for each business category.",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // 2. Safe Outreach & Sandbox Mode Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, RobopixBorder, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = RobopixSurface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = RobopixVioletBright)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Safe Outreach & Sandbox Mode", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Anti-spam & verification controls", color = TextSecondary, fontSize = 11.sp)
                            }
                        }
                        Switch(
                            checked = isTestMode,
                            onCheckedChange = { isTestMode = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = RobopixVioletBright,
                                checkedTrackColor = RobopixVioletContainer
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (isTestMode)
                            "Sandbox Mode is ON: Generated messages include a [TEST MODE / SANDBOX] tag so you can review copy safely without risk of accidental live outreach."
                        else
                            "Live Mode: Messages will be formatted for real business communication. Automatic bulk sending is disabled to prevent WhatsApp restrictions.",
                        color = if (isTestMode) RobopixVioletBright else TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // 3. Profile & Default Brand Identity
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, RobopixBorder, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = RobopixSurface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = RobopixVioletBright)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ROBOPIX Profile", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = companyName,
                        onValueChange = { companyName = it },
                        label = { Text("Brand / Agency Name") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = RobopixBackground,
                            unfocusedContainerColor = RobopixBackground,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = senderName,
                        onValueChange = { senderName = it },
                        label = { Text("Sender Name") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = RobopixBackground,
                            unfocusedContainerColor = RobopixBackground,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = senderEmail,
                            onValueChange = { senderEmail = it },
                            label = { Text("Sender Email") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = RobopixBackground,
                                unfocusedContainerColor = RobopixBackground,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = senderWhatsApp,
                            onValueChange = { senderWhatsApp = it },
                            label = { Text("Sender WhatsApp") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = RobopixBackground,
                                unfocusedContainerColor = RobopixBackground,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = portfolioUrl,
                        onValueChange = { portfolioUrl = it },
                        label = { Text("Portfolio Website URL") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = RobopixBackground,
                            unfocusedContainerColor = RobopixBackground,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // 4. Default Outreach Topic & Offers
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, RobopixBorder, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = RobopixSurface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("OUTREACH DEFAULTS", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = defaultOutreachTopic,
                        onValueChange = { defaultOutreachTopic = it },
                        label = { Text("Default Outreach Topic") },
                        placeholder = { Text("e.g. Website development, digital menus, robotics websites", color = TextSecondary, fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = RobopixBackground,
                            unfocusedContainerColor = RobopixBackground,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = defaultService,
                        onValueChange = { defaultService = it },
                        label = { Text("Default Service Offer") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = RobopixBackground,
                            unfocusedContainerColor = RobopixBackground,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = defaultCta,
                        onValueChange = { defaultCta = it },
                        label = { Text("Default Call To Action") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = RobopixBackground,
                            unfocusedContainerColor = RobopixBackground,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Save Settings Button
        item {
            Button(
                onClick = {
                    viewModel.updateProfile(
                        profile.copy(
                            companyName = companyName.trim(),
                            senderName = senderName.trim(),
                            senderEmail = senderEmail.trim(),
                            senderWhatsApp = senderWhatsApp.trim(),
                            portfolioUrl = portfolioUrl.trim(),
                            aiProvider = aiProvider,
                            geminiApiKey = geminiApiKey.trim(),
                            openAiApiKey = openAiApiKey.trim(),
                            anthropicApiKey = anthropicApiKey.trim(),
                            defaultOutreachTopic = defaultOutreachTopic.trim(),
                            defaultService = defaultService.trim(),
                            defaultCta = defaultCta.trim(),
                            isTestMode = isTestMode
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = RobopixViolet),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_save_settings")
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save Profile & AI Settings", fontWeight = FontWeight.Bold)
            }
        }

        // 5. Data Management & Export
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, RobopixBorder, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = RobopixSurface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Storage, contentDescription = null, tint = RobopixVioletBright)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Data Management & CSV Export", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "All lead records and message histories are persisted locally in private Room SQLite storage.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val csv = buildString {
                                    appendLine("Company Name,Contact Person,Email,WhatsApp,CountryCode,Website,Business Type,Country,Location,Status,OptedOut")
                                    allClients.forEach { c ->
                                        appendLine("\"${c.companyName}\",\"${c.contactPerson}\",\"${c.email}\",\"${c.whatsapp}\",\"${c.countryCode}\",\"${c.website}\",\"${c.businessType}\",\"${c.country}\",\"${c.location}\",\"${c.status}\",${c.isOptedOut}")
                                    }
                                }
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Exported Clients", csv))
                                Toast.makeText(context, "Exported ${allClients.size} clients to CSV clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export CSV", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { showClearConfirmDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusDanger),
                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(StatusDanger)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Clear All Data", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            containerColor = RobopixSurface,
            title = { Text("Delete All Client Data?", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("This will permanently clear all imported leads and message audit history from the local database.", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllData()
                        showClearConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusDanger),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Delete Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}
