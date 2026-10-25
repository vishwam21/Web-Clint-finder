package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.ai.AiMessageGenerator
import com.example.ui.components.StatusBadge
import com.example.ui.theme.EmailBlue
import com.example.ui.theme.RobopixBackground
import com.example.ui.theme.RobopixBorder
import com.example.ui.theme.RobopixBorderLight
import com.example.ui.theme.RobopixSurface
import com.example.ui.theme.RobopixSurfaceVariant
import com.example.ui.theme.RobopixViolet
import com.example.ui.theme.RobopixVioletBright
import com.example.ui.theme.RobopixVioletContainer
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WhatsAppContainer
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.viewmodel.MessengerViewModel

@Composable
fun AiGeneratorScreen(
    viewModel: MessengerViewModel,
    onNavigateToApprovals: () -> Unit
) {
    val context = LocalContext.current
    val clients by viewModel.allClients.collectAsStateWithLifecycle()
    val selectedClient by viewModel.genSelectedClient.collectAsStateWithLifecycle()

    val channel by viewModel.genChannel.collectAsStateWithLifecycle()
    val tone by viewModel.genTone.collectAsStateWithLifecycle()
    val length by viewModel.genLength.collectAsStateWithLifecycle()
    val outreachTopic by viewModel.genOutreachTopic.collectAsStateWithLifecycle()
    val service by viewModel.genService.collectAsStateWithLifecycle()
    val language by viewModel.genLanguage.collectAsStateWithLifecycle()
    val cta by viewModel.genCallToAction.collectAsStateWithLifecycle()

    val subject by viewModel.genSubject.collectAsStateWithLifecycle()
    val body by viewModel.genBody.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isAiGenerating.collectAsStateWithLifecycle()
    val statusMessage by viewModel.genStatusMessage.collectAsStateWithLifecycle()

    var showClientDropdown by remember { mutableStateOf(false) }
    var showServiceDropdown by remember { mutableStateOf(false) }
    var showCtaDropdown by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(RobopixBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "AI MESSAGE GENERATOR",
                color = RobopixVioletBright,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "Personalized Outreach Studio",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Craft tailored proposals for ROBOPIX website development, digital menus, landing pages, and redesigns using verified company information.",
                color = TextSecondary,
                fontSize = 13.sp
            )
        }

        // 1. Client Selector Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, RobopixBorderLight, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = RobopixSurface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. TARGET BUSINESS",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(RobopixBackground)
                                .border(1.dp, RobopixBorder, RoundedCornerShape(10.dp))
                                .clickable { showClientDropdown = true }
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = selectedClient?.companyName ?: "— Select a Business from Directory —",
                                    color = if (selectedClient != null) TextPrimary else TextSecondary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                if (selectedClient != null) {
                                    Text(
                                        text = "${selectedClient!!.businessType} • ${selectedClient!!.location}",
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = RobopixVioletBright)
                        }

                        DropdownMenu(
                            expanded = showClientDropdown,
                            onDismissRequest = { showClientDropdown = false },
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .background(RobopixSurface)
                        ) {
                            if (clients.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No clients imported yet", color = TextSecondary) },
                                    onClick = { showClientDropdown = false }
                                )
                            }
                            clients.forEach { client ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                client.companyName + if (client.isOptedOut) " (Opted Out)" else "",
                                                color = if (client.isOptedOut) Color(0xFFF87171) else TextPrimary,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                "${client.businessType} • ${client.location}",
                                                color = TextSecondary,
                                                fontSize = 11.sp
                                            )
                                        }
                                    },
                                    onClick = {
                                        viewModel.selectClientForAi(client)
                                        showClientDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    if (selectedClient != null && selectedClient!!.isOptedOut) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Warning: This client is marked as Opted Out. Outreach messages cannot be sent to this contact.",
                            color = Color(0xFFF87171),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // 2. Generation Settings Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, RobopixBorder, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = RobopixSurface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "2. OUTREACH STRATEGY & PARAMETERS",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Channel Selector
                    Text("Outreach Channel", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutreachChannelOption(
                            title = "WhatsApp",
                            subtitle = if (selectedClient?.whatsapp?.isNotBlank() == true) selectedClient!!.whatsapp else "wa.me link",
                            icon = Icons.Default.Phone,
                            selected = channel == "WHATSAPP",
                            accentColor = WhatsAppGreen,
                            onClick = { viewModel.genChannel.value = "WHATSAPP" },
                            modifier = Modifier.weight(1f)
                        )
                        OutreachChannelOption(
                            title = "Email",
                            subtitle = if (selectedClient?.email?.isNotBlank() == true) selectedClient!!.email else "mailto draft",
                            icon = Icons.Default.Email,
                            selected = channel == "EMAIL",
                            accentColor = EmailBlue,
                            onClick = { viewModel.genChannel.value = "EMAIL" },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Outreach Topic (User requested)
                    Text("Outreach Topic", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = outreachTopic,
                        onValueChange = { viewModel.genOutreachTopic.value = it },
                        placeholder = { Text("e.g. Website development, digital menus, robotics websites, 3D design", color = TextSecondary, fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = RobopixBackground,
                            unfocusedContainerColor = RobopixBackground,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_outreach_topic")
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "Website Development",
                            "Digital Menus",
                            "Robotics Websites",
                            "3D Design Services",
                            "Website Redesign"
                        ).forEach { top ->
                            FilterChip(
                                selected = outreachTopic == top,
                                onClick = { viewModel.genOutreachTopic.value = top },
                                label = { Text(top, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RobopixVioletContainer,
                                    selectedLabelColor = RobopixVioletBright
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // ROBOPIX Service Offering Dropdown
                    Text("ROBOPIX Service Offering", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(RobopixBackground)
                                .border(1.dp, RobopixBorder, RoundedCornerShape(8.dp))
                                .clickable { showServiceDropdown = true }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(service, color = RobopixVioletBright, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextSecondary)
                        }

                        DropdownMenu(
                            expanded = showServiceDropdown,
                            onDismissRequest = { showServiceDropdown = false },
                            modifier = Modifier.background(RobopixSurface)
                        ) {
                            AiMessageGenerator.SERVICES.forEach { srv ->
                                DropdownMenuItem(
                                    text = { Text(srv, color = TextPrimary, fontSize = 12.sp) },
                                    onClick = {
                                        viewModel.genService.value = srv
                                        showServiceDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Tone Pills
                    Text("Tone", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AiMessageGenerator.TONES.forEach { t ->
                            FilterChip(
                                selected = tone == t,
                                onClick = { viewModel.genTone.value = t },
                                label = { Text(t, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RobopixVioletContainer,
                                    selectedLabelColor = RobopixVioletBright
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Length Pills
                    Text("Message Length", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AiMessageGenerator.LENGTHS.forEach { l ->
                            FilterChip(
                                selected = length == l,
                                onClick = { viewModel.genLength.value = l },
                                label = { Text(l, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RobopixVioletContainer,
                                    selectedLabelColor = RobopixVioletBright
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Language Pills
                    Text("Language", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AiMessageGenerator.LANGUAGES.forEach { lang ->
                            FilterChip(
                                selected = language == lang,
                                onClick = { viewModel.genLanguage.value = lang },
                                label = { Text(lang, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RobopixVioletContainer,
                                    selectedLabelColor = RobopixVioletBright
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Call to Action
                    Text("Call to Action (CTA)", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(RobopixBackground)
                                .border(1.dp, RobopixBorder, RoundedCornerShape(8.dp))
                                .clickable { showCtaDropdown = true }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(cta, color = TextPrimary, fontSize = 12.sp)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextSecondary)
                        }

                        DropdownMenu(
                            expanded = showCtaDropdown,
                            onDismissRequest = { showCtaDropdown = false },
                            modifier = Modifier.background(RobopixSurface)
                        ) {
                            AiMessageGenerator.CTAS.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text(c, color = TextPrimary, fontSize = 12.sp) },
                                    onClick = {
                                        viewModel.genCallToAction.value = c
                                        showCtaDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Generate Button
                    Button(
                        onClick = { viewModel.generateOutreachMessage() },
                        enabled = selectedClient != null && !isGenerating && !(selectedClient?.isOptedOut ?: false),
                        colors = ButtonDefaults.buttonColors(containerColor = RobopixViolet),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_generate_ai_message")
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generating Personalized Pitch...")
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Generate AI Outreach Message", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Status or Info Banner
        if (statusMessage != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, RobopixBorderLight, RoundedCornerShape(10.dp)),
                    colors = CardDefaults.cardColors(containerColor = RobopixSurfaceVariant),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = RobopixVioletBright, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(statusMessage ?: "", color = TextPrimary, fontSize = 12.sp)
                    }
                }
            }
        }

        // 3. Message Editor & Preview (WhatsApp or Email)
        if (body.isNotBlank() || isGenerating) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, RobopixViolet.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = RobopixSurface),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (channel == "EMAIL") "EMAIL EDITOR" else "WHATSAPP MESSAGE EDITOR",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${body.length} chars",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = {
                                        val fullText = if (channel == "EMAIL") "Subject: $subject\n\n$body" else body
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("ROBOPIX Message", fullText))
                                        Toast.makeText(context, "Message copied to clipboard", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = RobopixVioletBright, modifier = Modifier.size(18.dp))
                                }
                            }
                        }

                        if (channel == "EMAIL") {
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = subject,
                                onValueChange = { viewModel.genSubject.value = it },
                                label = { Text("Subject Line") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = RobopixBackground,
                                    unfocusedContainerColor = RobopixBackground,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = body,
                            onValueChange = { viewModel.genBody.value = it },
                            label = { Text(if (channel == "EMAIL") "Email Body" else "WhatsApp Message Body") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = RobopixBackground,
                                unfocusedContainerColor = RobopixBackground,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            minLines = 8,
                            maxLines = 14,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("textarea_message_body")
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Workflow Action Buttons: Regenerate, Edit, Approve, Skip
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.generateOutreachMessage() },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(RobopixBorder)),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_regenerate_message")
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Regenerate", fontSize = 11.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    val client = selectedClient
                                    if (client != null) {
                                        viewModel.skipClient(client.id)
                                        viewModel.genBody.value = ""
                                        viewModel.genSubject.value = ""
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(RobopixBorder)),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_skip_client")
                            ) {
                                Text("Skip", fontSize = 11.sp)
                            }

                            OutlinedButton(
                                onClick = { viewModel.saveGeneratedMessage(sendToReview = false) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(RobopixBorder)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Save Draft", fontSize = 11.sp)
                            }

                            Button(
                                onClick = {
                                    viewModel.saveGeneratedMessage(sendToReview = true)
                                    onNavigateToApprovals()
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = RobopixViolet),
                                modifier = Modifier
                                    .weight(1.2f)
                                    .testTag("btn_approve_and_queue")
                            ) {
                                Text("Approve & Queue", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun OutreachChannelOption(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) accentColor.copy(alpha = 0.12f) else RobopixSurfaceVariant)
            .border(
                1.dp,
                if (selected) accentColor else RobopixBorder,
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) accentColor else TextSecondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    color = if (selected) TextPrimary else TextSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = subtitle,
                    color = TextSecondary,
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }
        }
    }
}
