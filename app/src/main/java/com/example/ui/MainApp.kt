package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.AiGeneratorScreen
import com.example.ui.screens.ApprovalsScreen
import com.example.ui.screens.ClientManagementScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ImportCenterScreen
import com.example.ui.screens.MessageHistoryScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.RobopixBackground
import com.example.ui.theme.RobopixBorder
import com.example.ui.theme.RobopixSurface
import com.example.ui.theme.RobopixSurfaceVariant
import com.example.ui.theme.RobopixViolet
import com.example.ui.theme.RobopixVioletBright
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MessengerViewModel
import kotlinx.coroutines.launch

enum class AppDestination(val title: String, val icon: ImageVector) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard),
    IMPORT("Import", Icons.Default.FileUpload),
    CLIENTS("Clients", Icons.Default.People),
    AI_GEN("AI Pitch", Icons.Default.AutoAwesome),
    APPROVALS("Approvals", Icons.Default.CheckCircle),
    HISTORY("History", Icons.Default.History),
    SETTINGS("Settings", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(viewModel: MessengerViewModel) {
    var currentDestination by remember { mutableStateOf(AppDestination.DASHBOARD) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val pendingReviewMessages by viewModel.pendingReviewMessages.collectAsStateWithLifecycle()
    val pendingCount = pendingReviewMessages.size

    // Handle Android system back button
    BackHandler(enabled = currentDestination != AppDestination.DASHBOARD || drawerState.isOpen) {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else {
            currentDestination = AppDestination.DASHBOARD
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 600.dp

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    drawerContainerColor = RobopixSurface,
                    drawerContentColor = TextPrimary,
                    modifier = Modifier.width(280.dp)
                ) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(RobopixViolet)
                                .border(1.dp, RobopixVioletBright, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("R", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("ROBOPIX", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp, letterSpacing = 1.sp)
                            Text("Client Messenger", color = RobopixVioletBright, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    AppDestination.entries.forEach { destination ->
                        NavigationDrawerItem(
                            label = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(destination.title, fontWeight = FontWeight.SemiBold)
                                    if (destination == AppDestination.APPROVALS && pendingCount > 0) {
                                        Badge(containerColor = StatusWarning) {
                                            Text("$pendingCount", color = Color.Black)
                                        }
                                    }
                                }
                            },
                            icon = {
                                Icon(destination.icon, contentDescription = destination.title)
                            },
                            selected = currentDestination == destination,
                            onClick = {
                                currentDestination = destination
                                scope.launch { drawerState.close() }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = RobopixViolet.copy(alpha = 0.2f),
                                selectedIconColor = RobopixVioletBright,
                                selectedTextColor = TextPrimary,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary
                            ),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Wide Screen Navigation Rail (Tablets & Desktops)
                if (isWideScreen) {
                    NavigationRail(
                        containerColor = RobopixSurface,
                        contentColor = TextPrimary,
                        modifier = Modifier.border(1.dp, RobopixBorder),
                        header = {
                            Spacer(modifier = Modifier.height(16.dp))
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(RobopixViolet),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("R", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    ) {
                        AppDestination.entries.forEach { destination ->
                            NavigationRailItem(
                                selected = currentDestination == destination,
                                onClick = { currentDestination = destination },
                                icon = {
                                    if (destination == AppDestination.APPROVALS && pendingCount > 0) {
                                        BadgedBox(badge = {
                                            Badge(containerColor = StatusWarning) {
                                                Text("$pendingCount", color = Color.Black)
                                            }
                                        }) {
                                            Icon(destination.icon, contentDescription = destination.title)
                                        }
                                    } else {
                                        Icon(destination.icon, contentDescription = destination.title)
                                    }
                                },
                                label = { Text(destination.title, fontSize = 10.sp) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = RobopixVioletBright,
                                    selectedTextColor = RobopixVioletBright,
                                    indicatorColor = RobopixViolet.copy(alpha = 0.25f),
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                )
                            )
                        }
                    }
                }

                // Main Content Scaffold
                Scaffold(
                    contentWindowInsets = WindowInsets.safeDrawing,
                    topBar = {
                        TopAppBar(
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (!isWideScreen) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(RobopixViolet),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("R", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                    }
                                    Column {
                                        Text(
                                            text = currentDestination.title,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 17.sp
                                        )
                                        Text(
                                            text = "ROBOPIX Client Messenger",
                                            color = RobopixVioletBright,
                                            fontSize = 10.sp,
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                }
                            },
                            navigationIcon = {
                                IconButton(
                                    onClick = { scope.launch { drawerState.open() } },
                                    modifier = Modifier.testTag("btn_open_nav_drawer")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = "Menu",
                                        tint = TextPrimary
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = RobopixBackground
                            )
                        )
                    },
                    bottomBar = {
                        // Mobile Bottom Navigation Bar (Phones)
                        if (!isWideScreen) {
                            NavigationBar(
                                containerColor = RobopixSurface,
                                contentColor = TextPrimary,
                                tonalElevation = 0.dp,
                                modifier = Modifier
                                    .border(1.dp, RobopixBorder)
                                    .navigationBarsPadding()
                            ) {
                                val mobileDestinations = listOf(
                                    AppDestination.DASHBOARD,
                                    AppDestination.IMPORT,
                                    AppDestination.CLIENTS,
                                    AppDestination.AI_GEN,
                                    AppDestination.APPROVALS
                                )

                                mobileDestinations.forEach { destination ->
                                    val isSelected = currentDestination == destination
                                    NavigationBarItem(
                                        selected = isSelected,
                                        onClick = { currentDestination = destination },
                                        icon = {
                                            if (destination == AppDestination.APPROVALS && pendingCount > 0) {
                                                BadgedBox(badge = {
                                                    Badge(containerColor = StatusWarning) {
                                                        Text("$pendingCount", color = Color.Black)
                                                    }
                                                }) {
                                                    Icon(destination.icon, contentDescription = destination.title)
                                                }
                                            } else {
                                                Icon(destination.icon, contentDescription = destination.title)
                                            }
                                        },
                                        label = { Text(destination.title, fontSize = 10.sp) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = RobopixVioletBright,
                                            selectedTextColor = RobopixVioletBright,
                                            indicatorColor = RobopixViolet.copy(alpha = 0.25f),
                                            unselectedIconColor = TextSecondary,
                                            unselectedTextColor = TextSecondary
                                        )
                                    )
                                }
                            }
                        }
                    },
                    containerColor = RobopixBackground
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentDestination) {
                            AppDestination.DASHBOARD -> DashboardScreen(
                                viewModel = viewModel,
                                onNavigateToImport = { currentDestination = AppDestination.IMPORT },
                                onNavigateToClients = { currentDestination = AppDestination.CLIENTS },
                                onNavigateToAiGen = { currentDestination = AppDestination.AI_GEN },
                                onNavigateToApprovals = { currentDestination = AppDestination.APPROVALS }
                            )
                            AppDestination.IMPORT -> ImportCenterScreen(
                                viewModel = viewModel,
                                onNavigateToClients = { currentDestination = AppDestination.CLIENTS }
                            )
                            AppDestination.CLIENTS -> ClientManagementScreen(
                                viewModel = viewModel,
                                onNavigateToAiGenForClient = { client ->
                                    viewModel.selectClientForAi(client)
                                    currentDestination = AppDestination.AI_GEN
                                }
                            )
                            AppDestination.AI_GEN -> AiGeneratorScreen(
                                viewModel = viewModel,
                                onNavigateToApprovals = { currentDestination = AppDestination.APPROVALS }
                            )
                            AppDestination.APPROVALS -> ApprovalsScreen(
                                viewModel = viewModel,
                                onNavigateToAiGen = { currentDestination = AppDestination.AI_GEN }
                            )
                            AppDestination.HISTORY -> MessageHistoryScreen(
                                viewModel = viewModel
                            )
                            AppDestination.SETTINGS -> SettingsScreen(
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
    }
}
