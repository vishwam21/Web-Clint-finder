package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.MessageEntity
import com.example.ui.theme.EmailBlue
import com.example.ui.theme.EmailContainer
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
import com.example.ui.theme.WhatsAppContainer
import com.example.ui.theme.WhatsAppGreen

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconTint: Color = RobopixVioletBright,
    modifier: Modifier = Modifier,
    subtext: String? = null
) {
    Card(
        modifier = modifier
            .border(1.dp, RobopixBorder, RoundedCornerShape(14.dp))
            .testTag("metric_${title.lowercase().replace(" ", "_")}"),
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
                    text = title.uppercase(),
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            if (subtext != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtext,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (bg, fg, label, icon) = when (status) {
        "NOT_GENERATED", "NO_MESSAGE" -> Quad(
            RobopixSurfaceVariant,
            TextSecondary,
            "Not Generated",
            null
        )
        "DRAFT" -> Quad(
            RobopixSurfaceVariant,
            TextSecondary,
            "Draft",
            null
        )
        "PENDING_REVIEW" -> Quad(
            StatusWarning.copy(alpha = 0.15f),
            StatusWarning,
            "Pending Review",
            Icons.Default.HourglassEmpty
        )
        "APPROVED" -> Quad(
            RobopixVioletContainer,
            RobopixVioletBright,
            "Approved",
            Icons.Default.CheckCircle
        )
        "WHATSAPP_OPENED", "OPENED_WHATSAPP", "AWAITING_CONFIRMATION" -> Quad(
            EmailContainer,
            EmailBlue,
            "WhatsApp Opened",
            Icons.Default.Send
        )
        "MARKED_SENT", "SENT" -> Quad(
            WhatsAppContainer,
            StatusSuccess,
            "Marked Sent",
            Icons.Default.CheckCircle
        )
        "SKIPPED" -> Quad(
            RobopixSurfaceVariant,
            TextSecondary,
            "Skipped",
            null
        )
        "OPTED_OUT" -> Quad(
            Color(0xFF2E1A1A),
            Color(0xFFF87171),
            "Opted Out",
            Icons.Default.Close
        )
        "FAILED" -> Quad(
            StatusDanger.copy(alpha = 0.15f),
            StatusDanger,
            "Failed",
            Icons.Default.Close
        )
        else -> Quad(
            RobopixSurfaceVariant,
            TextSecondary,
            status.replace("_", " "),
            null
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .border(0.5.dp, fg.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = fg,
                    modifier = Modifier.size(11.dp)
                )
            }
            Text(
                text = label,
                color = fg,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun ChannelBadge(
    channel: String,
    modifier: Modifier = Modifier
) {
    val isWa = channel.equals("WHATSAPP", ignoreCase = true)
    val bg = if (isWa) WhatsAppContainer else EmailContainer
    val fg = if (isWa) WhatsAppGreen else EmailBlue
    val text = if (isWa) "WhatsApp" else "Email"

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            color = fg,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun ConfirmSentDialog(
    message: MessageEntity,
    onConfirmSent: () -> Unit,
    onKeepPending: () -> Unit,
    onMarkFailed: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = RobopixSurface,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary,
        shape = RoundedCornerShape(16.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = null,
                    tint = RobopixVioletBright,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Confirm Sending Result", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
        },
        text = {
            Column {
                Text(
                    text = "You opened ${if (message.channel == "WHATSAPP") "WhatsApp" else "Email"} for ${message.clientCompanyName}.",
                    fontSize = 13.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Did you manually press Send in the external app?",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Note: 'Marked Sent' is recorded as a manual user confirmation.",
                    fontSize = 11.sp,
                    color = RobopixVioletBright
                )
                Spacer(modifier = Modifier.height(10.dp))
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
                        fontSize = 11.sp,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmSent,
                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("confirm_sent_yes")
            ) {
                Text("Mark as Sent (Manual)", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onMarkFailed,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusDanger),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(StatusDanger)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Failed / Cancelled")
                }
                TextButton(onClick = onKeepPending) {
                    Text("Keep Awaiting", color = TextSecondary)
                }
            }
        }
    )
}
