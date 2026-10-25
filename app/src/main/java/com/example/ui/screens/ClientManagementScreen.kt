package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import com.example.domain.util.ContactNormalizer
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.ClientEntity
import com.example.ui.components.StatusBadge
import com.example.ui.theme.RobopixBackground
import com.example.ui.theme.RobopixBorder
import com.example.ui.theme.RobopixBorderLight
import com.example.ui.theme.RobopixSurface
import com.example.ui.theme.RobopixSurfaceVariant
import com.example.ui.theme.RobopixViolet
import com.example.ui.theme.RobopixVioletBright
import com.example.ui.theme.RobopixVioletContainer
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.viewmodel.MessengerViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ClientManagementScreen(
    viewModel: MessengerViewModel,
    onNavigateToAiGenForClient: (ClientEntity) -> Unit
) {
    val context = LocalContext.current
    val clients by viewModel.filteredClients.collectAsStateWithLifecycle()
    val allClients by viewModel.allClients.collectAsStateWithLifecycle()

    val searchQuery by viewModel.clientSearchQuery.collectAsStateWithLifecycle()
    val statusFilter by viewModel.clientStatusFilter.collectAsStateWithLifecycle()
    val missingFilter by viewModel.clientMissingInfoFilter.collectAsStateWithLifecycle()

    var showAddEditDialog by remember { mutableStateOf<ClientEntity?>(null) }
    var clientToDelete by remember { mutableStateOf<ClientEntity?>(null) }
    var showFiltersPanel by remember { mutableStateOf(false) }

    // Collect unique categories and countries for filter pills
    val categories = remember(allClients) {
        listOf("ALL") + allClients.map { it.businessType }.filter { it.isNotBlank() }.distinct()
    }
    val currentCat by viewModel.clientCategoryFilter.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(RobopixBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CLIENT DIRECTORY",
                        color = RobopixVioletBright,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Leads & Contact Management",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Button(
                    onClick = {
                        showAddEditDialog = ClientEntity(companyName = "")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RobopixViolet),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("btn_add_client")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Client", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Search Bar & Filter Toggle
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.clientSearchQuery.value = it },
                    placeholder = { Text("Search by company, person, email, phone...", color = TextSecondary, fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.clientSearchQuery.value = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = null, tint = TextSecondary)
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = RobopixSurface,
                        unfocusedContainerColor = RobopixSurface,
                        focusedBorderColor = RobopixViolet,
                        unfocusedBorderColor = RobopixBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_client_search")
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (showFiltersPanel) RobopixVioletContainer else RobopixSurface)
                        .border(1.dp, if (showFiltersPanel) RobopixViolet else RobopixBorder, RoundedCornerShape(10.dp))
                        .clickable { showFiltersPanel = !showFiltersPanel }
                        .padding(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filters",
                        tint = if (showFiltersPanel) RobopixVioletBright else TextSecondary
                    )
                }
            }
        }

        // Expandable Filters Panel
        item {
            AnimatedVisibility(visible = showFiltersPanel) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, RobopixBorder, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = RobopixSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("STATUS FILTER", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("ALL", "NOT_GENERATED", "DRAFT", "PENDING_REVIEW", "APPROVED", "WHATSAPP_OPENED", "MARKED_SENT", "SKIPPED", "OPTED_OUT").forEach { st ->
                                FilterChip(
                                    selected = statusFilter == st,
                                    onClick = { viewModel.clientStatusFilter.value = st },
                                    label = { Text(st.replace("_", " "), fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = RobopixVioletContainer,
                                        selectedLabelColor = RobopixVioletBright
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text("CONTACT INFO FILTER", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("ALL", "MISSING_PHONE", "MISSING_EMAIL", "HAS_BOTH").forEach { mf ->
                                FilterChip(
                                    selected = missingFilter == mf,
                                    onClick = { viewModel.clientMissingInfoFilter.value = mf },
                                    label = { Text(mf.replace("_", " "), fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = RobopixVioletContainer,
                                        selectedLabelColor = RobopixVioletBright
                                    )
                                )
                            }
                        }

                        if (categories.size > 2) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("CATEGORY FILTER", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                categories.take(8).forEach { cat ->
                                    FilterChip(
                                        selected = currentCat == cat,
                                        onClick = { viewModel.clientCategoryFilter.value = cat },
                                        label = { Text(cat, fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = RobopixVioletContainer,
                                            selectedLabelColor = RobopixVioletBright
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // List Header Count
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SHOWING ${clients.size} OF ${allClients.size} CLIENTS",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        if (clients.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, RobopixBorder, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = RobopixSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (allClients.isEmpty()) "No Clients in Database" else "No Matching Clients Found",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (allClients.isEmpty()) "Import an Excel file or click Load Demo Leads to populate your client directory."
                            else "Try adjusting your search query or filter settings.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }

        items(clients, key = { it.id }) { client ->
            ClientCard(
                client = client,
                onEdit = { showAddEditDialog = client },
                onDelete = { clientToDelete = client },
                onToggleOptOut = { viewModel.toggleOptOut(client.id, client.isOptedOut) },
                onToggleConsent = { viewModel.toggleConsent(client.id, client.hasConsent) },
                onGenerateAi = { onNavigateToAiGenForClient(client) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Add / Edit Dialog
    if (showAddEditDialog != null) {
        AddEditClientDialog(
            initialClient = showAddEditDialog!!,
            onSave = { updated ->
                viewModel.addOrUpdateClient(updated)
                showAddEditDialog = null
            },
            onDismiss = { showAddEditDialog = null }
        )
    }

    // Delete Confirmation Dialog
    if (clientToDelete != null) {
        val target = clientToDelete!!
        AlertDialog(
            onDismissRequest = { clientToDelete = null },
            containerColor = RobopixSurface,
            title = { Text("Delete Client", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete '${target.companyName}' and its associated outreach drafts?", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteClient(target.id)
                        clientToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusDanger),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { clientToDelete = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun ClientCard(
    client: ClientEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleOptOut: () -> Unit,
    onToggleConsent: () -> Unit,
    onGenerateAi: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (client.isOptedOut) Color(0xFF4A2020) else RobopixBorder,
                RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = RobopixSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Company name & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = client.companyName,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        if (client.isOptedOut) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF331515))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("OPTED OUT", color = Color(0xFFF87171), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (client.contactPerson.isNotBlank()) {
                        Text(
                            text = "Contact: " + client.contactPerson,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                StatusBadge(status = if (client.isOptedOut) "OPTED_OUT" else client.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Details Chips Row (Email, WhatsApp, Website, Location)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (client.whatsapp.isNotBlank()) {
                    val isValid = ContactNormalizer.isValidInternationalPhone(client.whatsapp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = WhatsAppGreen, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(client.whatsapp, color = TextPrimary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isValid) Color(0xFF132A1C) else Color(0xFF332005))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isValid) "✓ Int'l Valid" else "⚠ Format Issue",
                                color = if (isValid) WhatsAppGreen else Color(0xFFFBBF24),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("No WhatsApp/Phone provided", color = TextSecondary, fontSize = 11.sp)
                    }
                }

                if (client.email.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Email, contentDescription = null, tint = RobopixVioletBright, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(client.email, color = TextPrimary, fontSize = 12.sp)
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Email, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("No Email provided", color = TextSecondary, fontSize = 11.sp)
                    }
                }

                if (client.website.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            val url = if (client.website.startsWith("http")) client.website else "https://${client.website}"
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        }
                    ) {
                        Icon(Icons.Default.Language, contentDescription = null, tint = RobopixVioletBright, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(client.website, color = RobopixVioletBright, fontSize = 12.sp)
                    }
                }

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (client.hasConsent) RobopixVioletContainer else Color(0xFF2E2010))
                            .clickable { onToggleConsent() }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (client.hasConsent) "Consent: Yes" else "Consent: Pending",
                            color = if (client.hasConsent) RobopixVioletBright else Color(0xFFFBBF24),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (client.outreachTopic.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(RobopixSurfaceVariant)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Topic: ${client.outreachTopic}", color = TextSecondary, fontSize = 10.sp)
                        }
                    }

                    if (client.businessType.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(RobopixSurfaceVariant)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(client.businessType, color = TextSecondary, fontSize = 10.sp)
                        }
                    }
                    if (client.location.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(RobopixSurfaceVariant)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(client.location, color = TextSecondary, fontSize = 10.sp)
                        }
                    }
                    if (client.country.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(RobopixSurfaceVariant)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(client.country, color = TextSecondary, fontSize = 10.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = onGenerateAi,
                        colors = ButtonDefaults.buttonColors(containerColor = RobopixVioletContainer),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !client.isOptedOut
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = RobopixVioletBright, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Draft Pitch", color = RobopixVioletBright, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onEdit,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(RobopixBorder))
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(13.dp))
                    }

                    OutlinedButton(
                        onClick = onDelete,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusDanger),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(RobopixBorder))
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(13.dp))
                    }
                }

                // Opt-out Switch
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onToggleOptOut() }
                ) {
                    Text(
                        text = if (client.isOptedOut) "Opted Out" else "Active",
                        color = if (client.isOptedOut) Color(0xFFF87171) else TextSecondary,
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Switch(
                        checked = !client.isOptedOut,
                        onCheckedChange = { onToggleOptOut() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = RobopixVioletBright,
                            checkedTrackColor = RobopixVioletContainer,
                            uncheckedThumbColor = Color(0xFFF87171),
                            uncheckedTrackColor = Color(0xFF331515)
                        ),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AddEditClientDialog(
    initialClient: ClientEntity,
    onSave: (ClientEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var company by remember { mutableStateOf(initialClient.companyName) }
    var contact by remember { mutableStateOf(initialClient.contactPerson) }
    var email by remember { mutableStateOf(initialClient.email) }
    var whatsapp by remember { mutableStateOf(initialClient.whatsapp) }
    var countryCode by remember { mutableStateOf(initialClient.countryCode) }
    var outreachTopic by remember { mutableStateOf(if (initialClient.outreachTopic.isNotBlank()) initialClient.outreachTopic else "Website Development") }
    var website by remember { mutableStateOf(initialClient.website) }
    var category by remember { mutableStateOf(initialClient.businessType) }
    var location by remember { mutableStateOf(initialClient.location) }
    var country by remember { mutableStateOf(initialClient.country) }
    var hasConsent by remember { mutableStateOf(initialClient.hasConsent) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = RobopixSurface,
        title = {
            Text(
                text = if (initialClient.id == 0L) "Add Client Record" else "Edit Client",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = company,
                    onValueChange = { company = it },
                    label = { Text("Company Name *") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = RobopixBackground,
                        unfocusedContainerColor = RobopixBackground,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = contact,
                    onValueChange = { contact = it },
                    label = { Text("Contact Person") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = RobopixBackground,
                        unfocusedContainerColor = RobopixBackground,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = countryCode,
                        onValueChange = { countryCode = it },
                        label = { Text("Country Code") },
                        placeholder = { Text("+1 / +44") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = RobopixBackground,
                            unfocusedContainerColor = RobopixBackground,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.weight(0.4f)
                    )
                    OutlinedTextField(
                        value = whatsapp,
                        onValueChange = { whatsapp = it },
                        label = { Text("WhatsApp/Phone") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = RobopixBackground,
                            unfocusedContainerColor = RobopixBackground,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.weight(0.6f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = RobopixBackground,
                            unfocusedContainerColor = RobopixBackground,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = outreachTopic,
                        onValueChange = { outreachTopic = it },
                        label = { Text("Outreach Topic") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = RobopixBackground,
                            unfocusedContainerColor = RobopixBackground,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = website,
                    onValueChange = { website = it },
                    label = { Text("Website") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = RobopixBackground,
                        unfocusedContainerColor = RobopixBackground,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = RobopixBackground,
                            unfocusedContainerColor = RobopixBackground,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Location/City") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = RobopixBackground,
                            unfocusedContainerColor = RobopixBackground,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { hasConsent = !hasConsent }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Contact Permission / Consent", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text("Recipient has given consent or permission to be contacted", color = TextSecondary, fontSize = 10.sp)
                    }
                    Switch(
                        checked = hasConsent,
                        onCheckedChange = { hasConsent = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = RobopixVioletBright,
                            checkedTrackColor = RobopixVioletContainer
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (company.isNotBlank()) {
                        onSave(
                            initialClient.copy(
                                companyName = company.trim(),
                                contactPerson = contact.trim(),
                                email = email.trim(),
                                whatsapp = whatsapp.trim(),
                                countryCode = countryCode.trim(),
                                outreachTopic = outreachTopic.trim(),
                                website = website.trim(),
                                businessType = category.trim(),
                                location = location.trim(),
                                country = country.trim(),
                                hasConsent = hasConsent
                            )
                        )
                    }
                },
                enabled = company.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = RobopixViolet),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Save Client")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
