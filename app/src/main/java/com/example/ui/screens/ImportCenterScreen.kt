package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.importer.ColumnMapping
import com.example.data.importer.SpreadsheetParser
import com.example.data.importer.ValidatedLeadRow
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
import com.example.ui.viewmodel.MessengerViewModel

@Composable
fun ImportCenterScreen(
    viewModel: MessengerViewModel,
    onNavigateToClients: () -> Unit
) {
    val context = LocalContext.current
    val rawSheet by viewModel.rawParsedSheet.collectAsStateWithLifecycle()
    val columnMapping by viewModel.currentColumnMapping.collectAsStateWithLifecycle()
    val validatedRows by viewModel.validatedRows.collectAsStateWithLifecycle()
    val isImporting by viewModel.isImporting.collectAsStateWithLifecycle()
    val importSummary by viewModel.importSummary.collectAsStateWithLifecycle()

    var showManualMapping by remember { mutableStateOf(false) }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            var fileName = "spreadsheet"
            var fileSize = 0L
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                    if (nameIndex != -1) fileName = it.getString(nameIndex)
                    if (sizeIndex != -1) fileSize = it.getLong(sizeIndex)
                }
            }
            viewModel.parseSelectedFile(uri, fileName, fileSize)
        }
    }

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
                text = "EXCEL IMPORT CENTER",
                color = RobopixVioletBright,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Lead Spreadsheet Importer",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Import business contacts exported from your lead finder (.xlsx or .csv). We automatically detect headers, validate emails/phones, and flag duplicates.",
                color = TextSecondary,
                fontSize = 13.sp
            )
        }

        // Upload Dropzone Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, RobopixViolet.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .clickable { filePicker.launch("*/*") }
                    .testTag("upload_dropzone"),
                colors = CardDefaults.cardColors(containerColor = RobopixSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(RobopixVioletContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileUpload,
                            contentDescription = null,
                            tint = RobopixVioletBright,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Browse & Select Excel or CSV File",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Supports .xlsx and .csv files up to 50MB",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { filePicker.launch("*/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = RobopixViolet),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Choose File", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = { viewModel.loadDemoLeads() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(RobopixBorderLight)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Load Sample Leads", fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Import Summary Alert if available
        if (importSummary != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, StatusSuccess, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = StatusSuccess.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = StatusSuccess)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = importSummary ?: "",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        TextButton(onClick = onNavigateToClients) {
                            Text("View Clients", color = RobopixVioletBright, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Download Template Helper
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, RobopixBorder, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = RobopixSurfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(14.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Standard Excel / CSV Template",
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Columns: Company Name, Contact Person, Email, WhatsApp, Website, Business Type, Country, Location",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    Button(
                        onClick = {
                            val template = SpreadsheetParser.generateSampleCsvTemplate()
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("ROBOPIX Template", template))
                            Toast.makeText(context, "CSV template copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RobopixVioletContainer),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = RobopixVioletBright, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy Template", color = RobopixVioletBright, fontSize = 11.sp)
                    }
                }
            }
        }

        // When a file is loaded: Show metadata, mapping review & row preview
        if (rawSheet != null) {
            val sheet = rawSheet!!
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, RobopixBorderLight, RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = RobopixSurface),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = sheet.fileName,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "${sheet.rows.size} rows parsed • ${(sheet.fileSize / 1024)} KB",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }

                            Row {
                                OutlinedButton(
                                    onClick = { showManualMapping = !showManualMapping },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(if (showManualMapping) "Hide Mapping" else "Review Mapping", fontSize = 11.sp)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                TextButton(onClick = { viewModel.clearImportPreview() }) {
                                    Icon(Icons.Default.Close, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        // Column Mapping Configuration
                        AnimatedVisibility(visible = showManualMapping) {
                            Column(modifier = Modifier.padding(top = 14.dp)) {
                                Text(
                                    text = "COLUMN MAPPING",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                MappingRowSelector("Company Name *", columnMapping.companyNameCol, sheet.headers) {
                                    viewModel.updateColumnMapping(columnMapping.copy(companyNameCol = it))
                                }
                                MappingRowSelector("Contact Person", columnMapping.contactNameCol, sheet.headers) {
                                    viewModel.updateColumnMapping(columnMapping.copy(contactNameCol = it))
                                }
                                MappingRowSelector("Email", columnMapping.emailCol, sheet.headers) {
                                    viewModel.updateColumnMapping(columnMapping.copy(emailCol = it))
                                }
                                MappingRowSelector("WhatsApp / Phone", columnMapping.whatsappCol, sheet.headers) {
                                    viewModel.updateColumnMapping(columnMapping.copy(whatsappCol = it))
                                }
                                MappingRowSelector("Country Code", columnMapping.countryCodeCol, sheet.headers) {
                                    viewModel.updateColumnMapping(columnMapping.copy(countryCodeCol = it))
                                }
                                MappingRowSelector("Website", columnMapping.websiteCol, sheet.headers) {
                                    viewModel.updateColumnMapping(columnMapping.copy(websiteCol = it))
                                }
                                MappingRowSelector("Business Type", columnMapping.businessTypeCol, sheet.headers) {
                                    viewModel.updateColumnMapping(columnMapping.copy(businessTypeCol = it))
                                }
                                MappingRowSelector("Country", columnMapping.countryCol, sheet.headers) {
                                    viewModel.updateColumnMapping(columnMapping.copy(countryCol = it))
                                }
                                MappingRowSelector("Location", columnMapping.locationCol, sheet.headers) {
                                    viewModel.updateColumnMapping(columnMapping.copy(locationCol = it))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Stats about parsed data
                        val duplicateCount = validatedRows.count { it.isDuplicate }
                        val fatalCount = validatedRows.count { it.companyName.isBlank() }
                        val readyCount = validatedRows.size - fatalCount

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(RobopixBackground)
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Ready to Import", color = TextSecondary, fontSize = 11.sp)
                                Text("$readyCount", color = StatusSuccess, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Duplicates Detected", color = TextSecondary, fontSize = 11.sp)
                                Text("$duplicateCount", color = if (duplicateCount > 0) StatusWarning else TextSecondary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Missing Name (Skip)", color = TextSecondary, fontSize = 11.sp)
                                Text("$fatalCount", color = if (fatalCount > 0) StatusDanger else TextSecondary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Commit Import Button
                        Button(
                            onClick = { viewModel.commitImport() },
                            colors = ButtonDefaults.buttonColors(containerColor = RobopixViolet),
                            shape = RoundedCornerShape(10.dp),
                            enabled = !isImporting && readyCount > 0,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_commit_import")
                        ) {
                            if (isImporting) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Importing Records...")
                            } else {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Import $readyCount Verified Leads", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Preview Table of Rows
            item {
                Text(
                    text = "DATA PREVIEW (FIRST ${minOf(sheet.rows.size, 10)} ROWS)",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            itemsIndexed(validatedRows.take(10)) { idx, row ->
                LeadPreviewRow(row = row, index = idx + 1)
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MappingRowSelector(
    targetField: String,
    selectedColumn: String?,
    allHeaders: List<String>,
    onSelect: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = targetField, color = TextPrimary, fontSize = 12.sp, modifier = Modifier.weight(1f))

        Box {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(RobopixSurfaceVariant)
                    .border(1.dp, RobopixBorder, RoundedCornerShape(6.dp))
                    .clickable { expanded = true }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedColumn ?: "— None / Skip —",
                    color = if (selectedColumn != null) RobopixVioletBright else TextSecondary,
                    fontSize = 11.sp
                )
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextSecondary)
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(RobopixSurface)
            ) {
                DropdownMenuItem(
                    text = { Text("— None / Skip —", color = TextSecondary, fontSize = 12.sp) },
                    onClick = {
                        onSelect(null)
                        expanded = false
                    }
                )
                allHeaders.forEach { header ->
                    DropdownMenuItem(
                        text = { Text(header, color = TextPrimary, fontSize = 12.sp) },
                        onClick = {
                            onSelect(header)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LeadPreviewRow(
    row: ValidatedLeadRow,
    index: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (row.companyName.isBlank()) StatusDanger
                else if (row.isDuplicate) StatusWarning
                else RobopixBorder,
                RoundedCornerShape(10.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = RobopixSurface),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(RobopixSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("$index", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (row.companyName.isNotBlank()) row.companyName else "[MISSING COMPANY NAME]",
                        color = if (row.companyName.isNotBlank()) TextPrimary else StatusDanger,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                if (row.isDuplicate) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(StatusWarning.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("Duplicate", color = StatusWarning, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (row.contactPerson.isNotBlank()) {
                    Text("Person: ${row.contactPerson}", color = TextSecondary, fontSize = 11.sp)
                }
                if (row.email.isNotBlank()) {
                    Text("Email: ${row.email}", color = TextSecondary, fontSize = 11.sp)
                }
                if (row.whatsapp.isNotBlank()) {
                    Text("WhatsApp: ${row.whatsapp}", color = TextSecondary, fontSize = 11.sp)
                }
                if (row.businessType.isNotBlank()) {
                    Text("Category: ${row.businessType}", color = TextSecondary, fontSize = 11.sp)
                }
                if (row.location.isNotBlank()) {
                    Text("Location: ${row.location}", color = TextSecondary, fontSize = 11.sp)
                }
            }

            if (row.issues.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                row.issues.forEach { issue ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(
                            imageVector = if (issue.isFatal) Icons.Default.Close else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (issue.isFatal) StatusDanger else StatusWarning,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${issue.field}: ${issue.message}",
                            color = if (issue.isFatal) StatusDanger else StatusWarning,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}
