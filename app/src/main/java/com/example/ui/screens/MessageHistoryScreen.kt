package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.MessageEntity
import com.example.ui.components.ChannelBadge
import com.example.ui.components.StatusBadge
import com.example.ui.theme.RobopixBackground
import com.example.ui.theme.RobopixBorder
import com.example.ui.theme.RobopixSurface
import com.example.ui.theme.RobopixSurfaceVariant
import com.example.ui.theme.RobopixViolet
import com.example.ui.theme.RobopixVioletBright
import com.example.ui.theme.RobopixVioletContainer
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MessengerViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MessageHistoryScreen(
    viewModel: MessengerViewModel
) {
    val context = LocalContext.current
    val allMessages by viewModel.allMessages.collectAsStateWithLifecycle()

    var statusFilter by remember { mutableStateOf("ALL") }
    var channelFilter by remember { mutableStateOf("ALL") }

    val filteredMessages = remember(allMessages, statusFilter, channelFilter) {
        allMessages.filter { msg ->
            val matchesStatus = statusFilter == "ALL" || msg.status == statusFilter
            val matchesChannel = channelFilter == "ALL" || msg.channel.equals(channelFilter, ignoreCase = true)
            matchesStatus && matchesChannel
        }
    }

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
                text = "AUDIT TRAIL & LOGS",
                color = RobopixVioletBright,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "Message History & Tracking",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Comprehensive audit log of every outreach attempt across WhatsApp and Email with timestamps, recipients, and confirmed delivery states.",
                color = TextSecondary,
                fontSize = 13.sp
            )
        }

        // Filters Row
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, RobopixBorder, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = RobopixSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("CHANNEL", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("ALL", "WHATSAPP", "EMAIL").forEach { ch ->
                            FilterChip(
                                selected = channelFilter == ch,
                                onClick = { channelFilter = ch },
                                label = { Text(ch, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RobopixVioletContainer,
                                    selectedLabelColor = RobopixVioletBright
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("STATUS", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("ALL", "SENT", "AWAITING_CONFIRMATION", "APPROVED", "PENDING_REVIEW", "DRAFT", "REJECTED", "FAILED").forEach { st ->
                            FilterChip(
                                selected = statusFilter == st,
                                onClick = { statusFilter = st },
                                label = { Text(st.replace("_", " "), fontSize = 11.sp) },
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

        item {
            Text(
                text = "LOGGED ENTRIES (${filteredMessages.size})",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        if (filteredMessages.isEmpty()) {
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
                        Icon(Icons.Default.History, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No Message History Found", color = TextPrimary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Sent or reviewed messages will appear here with full timestamps.", color = TextSecondary, fontSize = 12.sp)
                    }
                }
            }
        }

        items(filteredMessages, key = { it.id }) { msg ->
            HistoryCard(
                message = msg,
                onCopy = {
                    val full = if (msg.channel == "EMAIL") "Subject: ${msg.subject}\n\n${msg.body}" else msg.body
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("History Entry", full))
                    Toast.makeText(context, "Copied message text", Toast.LENGTH_SHORT).show()
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HistoryCard(
    message: MessageEntity,
    onCopy: () -> Unit
) {
    val createdStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(message.createdAt))
    val sentStr = if (message.sentAt != null) {
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(message.sentAt))
    } else null

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, RobopixBorder, RoundedCornerShape(10.dp)),
        colors = CardDefaults.cardColors(containerColor = RobopixSurface),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = message.clientCompanyName,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    ChannelBadge(channel = message.channel)
                }
                StatusBadge(status = message.status)
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Recipient: ${message.recipient} • Created: $createdStr" + if (sentStr != null) " • Sent: $sentStr" else "",
                color = TextSecondary,
                fontSize = 11.sp
            )

            if (message.subject.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Subject: ${message.subject}",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(RobopixBackground)
                    .border(0.5.dp, RobopixBorder, RoundedCornerShape(6.dp))
                    .padding(8.dp)
            ) {
                Text(
                    text = message.body,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 4
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Service: ${message.serviceOffered} (${message.tone})",
                    color = RobopixVioletBright,
                    fontSize = 11.sp
                )

                IconButton(onClick = onCopy, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = TextSecondary, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}
