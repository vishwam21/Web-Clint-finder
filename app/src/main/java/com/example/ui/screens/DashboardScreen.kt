package com.example.ui.screens

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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.MessageEntity
import com.example.ui.components.ChannelBadge
import com.example.ui.components.MetricCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.RobopixBackground
import com.example.ui.theme.RobopixBorder
import com.example.ui.theme.RobopixBorderLight
import com.example.ui.theme.RobopixSurface
import com.example.ui.theme.RobopixSurfaceVariant
import com.example.ui.theme.RobopixViolet
import com.example.ui.theme.RobopixVioletBright
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MessengerViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: MessengerViewModel,
    onNavigateToImport: () -> Unit,
    onNavigateToClients: () -> Unit,
    onNavigateToAiGen: () -> Unit,
    onNavigateToApprovals: () -> Unit
) {
    val metrics by viewModel.dashboardMetrics.collectAsStateWithLifecycle()
    val allMessages by viewModel.allMessages.collectAsStateWithLifecycle()
    val allClients by viewModel.allClients.collectAsStateWithLifecycle()
    val profile by viewModel.profile.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(RobopixBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Hero / Brand Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, RobopixBorderLight, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = RobopixSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(StatusSuccess)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "STANDALONE CLIENT MESSENGER",
                                    color = RobopixVioletBright,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "ROBOPIX Outreach Suite",
                                color = TextPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(RobopixSurfaceVariant)
                                .border(1.dp, RobopixBorder, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = profile.companyName,
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Import leads from your lead finder spreadsheet, let AI draft personalized website offers, review each pitch individually, and send via WhatsApp or Email.",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onNavigateToImport,
                            colors = ButtonDefaults.buttonColors(containerColor = RobopixViolet),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_dashboard_import")
                        ) {
                            Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Import Excel", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = onNavigateToAiGen,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(RobopixBorderLight)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_dashboard_ai")
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = RobopixVioletBright, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("AI Generator", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // Metrics Grid (2 columns)
        item {
            Text(
                text = "PIPELINE OVERVIEW",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Total Clients",
                    value = metrics.totalClients.toString(),
                    icon = Icons.Default.People,
                    iconTint = RobopixVioletBright,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Awaiting Review",
                    value = metrics.pendingReview.toString(),
                    icon = Icons.Default.HourglassEmpty,
                    iconTint = StatusWarning,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Approved",
                    value = metrics.approved.toString(),
                    icon = Icons.Default.CheckCircle,
                    iconTint = RobopixVioletBright,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Sent",
                    value = metrics.sent.toString(),
                    icon = Icons.Default.Send,
                    iconTint = StatusSuccess,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Failed",
                    value = metrics.failed.toString(),
                    icon = Icons.Default.Close,
                    iconTint = StatusDanger,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Opted Out",
                    value = metrics.optedOut.toString(),
                    icon = Icons.Default.Warning,
                    iconTint = Color(0xFFF87171),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Pipeline Conversion Funnel
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
                        text = "Outreach Funnel Progress",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val total = if (metrics.totalClients > 0) metrics.totalClients.toFloat() else 1f
                    val approvedRatio = (metrics.approved + metrics.sent) / total
                    val sentRatio = metrics.sent / total

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Drafts & Review", color = TextSecondary, fontSize = 11.sp)
                        Text("${metrics.pendingReview} in review", color = StatusWarning, fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { (metrics.pendingReview / total).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = StatusWarning,
                        trackColor = RobopixSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Approved & Sent", color = TextSecondary, fontSize = 11.sp)
                        Text("${metrics.sent} sent / ${metrics.approved} ready", color = StatusSuccess, fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { sentRatio.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = StatusSuccess,
                        trackColor = RobopixSurfaceVariant
                    )
                }
            }
        }

        // Empty state or Quick demo loader if 0 clients
        if (allClients.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, RobopixBorderLight, RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = RobopixSurfaceVariant),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = RobopixVioletBright,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No Clients Imported Yet",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Upload an Excel/CSV file from your lead finder or load verified demo leads to try the AI workflow.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = onNavigateToImport,
                                colors = ButtonDefaults.buttonColors(containerColor = RobopixViolet),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Upload File", fontSize = 12.sp)
                            }
                            OutlinedButton(
                                onClick = { viewModel.loadDemoLeads() },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Load Demo Leads", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Recent Activity Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT ACTIVITY",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                if (allMessages.isNotEmpty()) {
                    Text(
                        text = "View All (${allMessages.size})",
                        color = RobopixVioletBright,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onNavigateToApprovals() }
                    )
                }
            }
        }

        if (allMessages.isEmpty() && allClients.isNotEmpty()) {
            item {
                Text(
                    text = "No outreach drafted yet. Go to AI Generator to prepare personalized proposals.",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        items(allMessages.take(5)) { msg ->
            ActivityMessageRow(msg = msg, onClick = onNavigateToApprovals)
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ActivityMessageRow(
    msg: MessageEntity,
    onClick: () -> Unit
) {
    val dateStr = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(msg.updatedAt))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, RobopixBorder, RoundedCornerShape(10.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = RobopixSurface),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = msg.clientCompanyName,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    ChannelBadge(channel = msg.channel)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = msg.serviceOffered + " • " + dateStr,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
            StatusBadge(status = msg.status)
        }
    }
}
