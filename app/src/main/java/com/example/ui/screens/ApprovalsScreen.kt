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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.local.MessageEntity
import com.example.ui.components.ChannelBadge
import com.example.ui.components.ConfirmSentDialog
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
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.viewmodel.MessengerViewModel

@Composable
fun ApprovalsScreen(
    viewModel: MessengerViewModel,
    onNavigateToAiGen: () -> Unit
) {
    val context = LocalContext.current
    val pendingReviewMessages by viewModel.pendingReviewMessages.collectAsStateWithLifecycle()
    val approvedMessages by viewModel.approvedMessages.collectAsStateWithLifecycle()
    val pendingConfirmationMsg by viewModel.pendingSendConfirmationMessage.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Pending Review, 1: Approved & Ready to Send
    var editingMessage by remember { mutableStateOf<MessageEntity?>(null) }
    var previewSendingMessage by remember { mutableStateOf<MessageEntity?>(null) }

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
                text = "APPROVAL & SEND WORKFLOW",
                color = RobopixVioletBright,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "Review & Outbound Dispatch",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Draft → Review → Approved → Send → Result Recorded. Explicit approval is mandatory before any pitch can be dispatched via WhatsApp or Email.",
                color = TextSecondary,
                fontSize = 13.sp
            )
        }

        // Tab Row
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = RobopixSurface,
                contentColor = RobopixVioletBright,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = RobopixViolet
                    )
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, RobopixBorder, RoundedCornerShape(10.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Pending Review", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(StatusWarning.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("${pendingReviewMessages.size}", color = StatusWarning, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                )

                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Approved & Ready", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(RobopixVioletContainer)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("${approvedMessages.size}", color = RobopixVioletBright, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                )
            }
        }

        // Tab Content
        if (selectedTab == 0) {
            // Pending Review List
            if (pendingReviewMessages.isEmpty()) {
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
                            Icon(Icons.Default.HourglassEmpty, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Approval Queue is Empty", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "All generated pitches have been reviewed. Use the AI Generator to draft proposals for more clients.",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = onNavigateToAiGen,
                                colors = ButtonDefaults.buttonColors(containerColor = RobopixViolet),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Open AI Generator")
                            }
                        }
                    }
                }
            } else {
                items(pendingReviewMessages, key = { it.id }) { msg ->
                    ReviewMessageCard(
                        message = msg,
                        onApprove = { viewModel.approveMessage(msg.id) },
                        onReject = { viewModel.rejectMessage(msg.id) },
                        onSkip = { viewModel.skipMessage(msg.id) },
                        onEdit = { editingMessage = msg },
                        onCopy = {
                            val text = if (msg.channel == "EMAIL") "Subject: ${msg.subject}\n\n${msg.body}" else msg.body
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Outreach", text))
                            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        } else {
            // Approved & Ready to Send List
            if (approvedMessages.isEmpty()) {
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
                            Text("No Approved Messages Waiting", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Approve messages from the Pending Review tab to prepare them for sending.",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(approvedMessages, key = { it.id }) { msg ->
                    ApprovedMessageCard(
                        message = msg,
                        onSendClick = { previewSendingMessage = msg },
                        onEdit = { editingMessage = msg },
                        onCopy = {
                            val text = if (msg.channel == "EMAIL") "Subject: ${msg.subject}\n\n${msg.body}" else msg.body
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Outreach", text))
                            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Edit Message Dialog
    if (editingMessage != null) {
        val target = editingMessage!!
        var subject by remember { mutableStateOf(target.subject) }
        var body by remember { mutableStateOf(target.body) }

        AlertDialog(
            onDismissRequest = { editingMessage = null },
            containerColor = RobopixSurface,
            title = {
                Text(
                    "Edit Outreach for ${target.clientCompanyName}",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (target.channel == "EMAIL") {
                        OutlinedTextField(
                            value = subject,
                            onValueChange = { subject = it },
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
                    OutlinedTextField(
                        value = body,
                        onValueChange = { body = it },
                        label = { Text("Message Body") },
                        minLines = 8,
                        maxLines = 14,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = RobopixBackground,
                            unfocusedContainerColor = RobopixBackground,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateMessageDraft(target.id, subject, body)
                        editingMessage = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RobopixViolet),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingMessage = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // Pre-Send Confirmation Modal (Shows recipient & final text before opening WhatsApp / Email)
    if (previewSendingMessage != null) {
        val target = previewSendingMessage!!
        AlertDialog(
            onDismissRequest = { previewSendingMessage = null },
            containerColor = RobopixSurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = null,
                        tint = if (target.channel == "WHATSAPP") WhatsAppGreen else RobopixVioletBright
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Dispatch to ${target.clientCompanyName}",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "You are about to launch ${if (target.channel == "WHATSAPP") "WhatsApp" else "your Email App"} with this approved message:",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(RobopixBackground)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Recipient: ", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(target.recipient, color = RobopixVioletBright, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    if (target.channel == "EMAIL" && target.subject.isNotBlank()) {
                        Text("Subject: ${target.subject}", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(RobopixBackground)
                            .border(1.dp, RobopixBorder, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = target.body,
                            color = TextPrimary,
                            fontSize = 11.sp,
                            maxLines = 8
                        )
                    }

                    Text(
                        text = "Note: Opening WhatsApp or Email will set the status to 'Awaiting Confirmation'. Please manually review and hit Send in the app.",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        previewSendingMessage = null
                        viewModel.initiateSend(context, target)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (target.channel == "WHATSAPP") WhatsAppGreen else RobopixViolet
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("btn_confirm_launch_app")
                ) {
                    Text(
                        text = if (target.channel == "WHATSAPP") "Open in WhatsApp" else "Open Email Draft",
                        color = if (target.channel == "WHATSAPP") Color.Black else Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { previewSendingMessage = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // Awaiting confirmation modal after returning from WhatsApp/Email
    if (pendingConfirmationMsg != null) {
        val target = pendingConfirmationMsg!!
        ConfirmSentDialog(
            message = target,
            onConfirmSent = { viewModel.confirmSentResult(target.id, actuallySent = true) },
            onKeepPending = { viewModel.confirmSentResult(target.id, actuallySent = false) },
            onMarkFailed = { viewModel.confirmSentResult(target.id, actuallySent = false, isFailed = true) },
            onDismiss = { viewModel.pendingSendConfirmationMessage.value = null }
        )
    }
}

@Composable
private fun ReviewMessageCard(
    message: MessageEntity,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onSkip: () -> Unit,
    onEdit: () -> Unit,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, RobopixBorder, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = RobopixSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = message.clientCompanyName,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        ChannelBadge(channel = message.channel)
                    }
                    Text(
                        text = "Recipient: ${message.recipient} • Tone: ${message.tone}",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                StatusBadge(status = message.status)
            }

            if (message.channel == "EMAIL" && message.subject.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
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
                    .clip(RoundedCornerShape(8.dp))
                    .background(RobopixBackground)
                    .border(1.dp, RobopixBorder, RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = message.body,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = onEdit,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(RobopixBorder))
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = onCopy,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(RobopixBorder))
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(13.dp))
                    }

                    OutlinedButton(
                        onClick = onSkip,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(RobopixBorder))
                    ) {
                        Text("Skip", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = onReject,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusDanger),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(RobopixBorder))
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reject", fontSize = 11.sp)
                    }
                }

                Button(
                    onClick = onApprove,
                    colors = ButtonDefaults.buttonColors(containerColor = RobopixViolet),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("btn_approve_message_${message.id}")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Approve", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun ApprovedMessageCard(
    message: MessageEntity,
    onSendClick: () -> Unit,
    onEdit: () -> Unit,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, RobopixViolet.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = RobopixSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = message.clientCompanyName,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        ChannelBadge(channel = message.channel)
                    }
                    Text(
                        text = "To: ${message.recipient}",
                        color = RobopixVioletBright,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                StatusBadge(status = message.status)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(RobopixBackground)
                    .border(1.dp, RobopixBorder, RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = message.body,
                    color = TextPrimary,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = onEdit,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(RobopixBorder))
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(13.dp))
                    }
                    OutlinedButton(
                        onClick = onCopy,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(RobopixBorder))
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(13.dp))
                    }
                }

                Button(
                    onClick = onSendClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (message.channel == "WHATSAPP") WhatsAppGreen else RobopixViolet
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("btn_send_approved_${message.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = null,
                        tint = if (message.channel == "WHATSAPP") Color.Black else Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (message.channel == "WHATSAPP") "Send via WhatsApp" else "Send via Email",
                        color = if (message.channel == "WHATSAPP") Color.Black else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
