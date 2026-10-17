package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.UserRole
import com.example.ui.components.PinAuthDialog
import com.example.ui.screens.AlarmsScreen
import com.example.ui.screens.CommDebuggerScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LogsScreen
import com.example.ui.screens.MachineDetailScreen
import com.example.ui.screens.TelemetryScreen
import com.example.ui.screens.UserManagementScreen
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.IndustrialBackground
import com.example.ui.theme.IndustrialSurface
import com.example.ui.theme.IndustrialSurfaceBorder
import com.example.ui.theme.IndustrialSurfaceElevated
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.OrangeSecondary
import com.example.ui.theme.StatusCritical
import com.example.ui.theme.StatusRunning
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.AppScreen
import com.example.viewmodel.PlcViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                PlcAppRoot()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlcAppRoot(
    viewModel: PlcViewModel = viewModel()
) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val machines by viewModel.machines.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val activeAlarms by viewModel.activeAlarms.collectAsStateWithLifecycle()
    val commDiag by viewModel.commDiagnostics.collectAsStateWithLifecycle()
    val livePackets by viewModel.livePackets.collectAsStateWithLifecycle()
    val recentTelemetryLogs by viewModel.recentTelemetryLogs.collectAsStateWithLifecycle()
    val alarmHistory by viewModel.alarmHistory.collectAsStateWithLifecycle()
    val selectedMachineId by viewModel.selectedMachineId.collectAsStateWithLifecycle()
    val selectedMetric by viewModel.selectedMetric.collectAsStateWithLifecycle()
    val logSearchQuery by viewModel.logSearchQuery.collectAsStateWithLifecycle()
    val alarmFilterSeverity by viewModel.alarmFilterSeverity.collectAsStateWithLifecycle()
    val pinPrompt by viewModel.pinAuthPrompt.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val unackAlarmsCount = activeAlarms.count { !it.isAcknowledged }

    LaunchedEffect(Unit) {
        viewModel.toastMessage.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "PLC Monitor",
                                    color = TextPrimary,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                // Connection Status Pill
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(IndustrialSurfaceElevated)
                                        .border(
                                            0.5.dp,
                                            if (commDiag.isConnected) StatusRunning else StatusCritical,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable { viewModel.navigateTo(AppScreen.DEBUGGER) }
                                        .padding(horizontal = 7.dp, vertical = 2.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    when {
                                                        !commDiag.isConnected -> StatusCritical
                                                        commDiag.isFailoverActive -> OrangeSecondary
                                                        else -> StatusRunning
                                                    }
                                                )
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (commDiag.isFailoverActive) "Failover (${commDiag.latencyMs}ms)" else "${commDiag.latencyMs}ms",
                                            color = if (commDiag.isFailoverActive) OrangeSecondary else CyanPrimary,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "스마트 산업설비 실시간 관제 시스템",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                },
                actions = {
                    // Role Switcher Quick Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(IndustrialSurfaceElevated)
                            .border(1.dp, IndustrialSurfaceBorder, RoundedCornerShape(8.dp))
                            .clickable { viewModel.navigateTo(AppScreen.USERS) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ManageAccounts,
                                contentDescription = "사용자 권한 관리",
                                tint = when (currentUser.role) {
                                    UserRole.ADMIN -> StatusCritical
                                    UserRole.ENGINEER -> OrangeSecondary
                                    UserRole.OPERATOR -> StatusRunning
                                },
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = currentUser.role.name,
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Alarm Bell with Badge
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.ALARMS) },
                        modifier = Modifier.testTag("nav_alarms_icon")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unackAlarmsCount > 0) {
                                    Badge(containerColor = StatusCritical) {
                                        Text(
                                            text = "$unackAlarmsCount",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "알람 센터",
                                tint = if (unackAlarmsCount > 0) StatusCritical else TextSecondary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = IndustrialSurface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = IndustrialSurface,
                contentColor = TextSecondary
            ) {
                val navItems = listOf(
                    NavItem(AppScreen.DASHBOARD, "대시보드", Icons.Default.Dashboard),
                    NavItem(AppScreen.MACHINES, "장비 제어", Icons.Default.PrecisionManufacturing),
                    NavItem(AppScreen.TELEMETRY, "실시간 차트", Icons.Default.ShowChart),
                    NavItem(AppScreen.ALARMS, "알람", Icons.Default.NotificationsActive, unackAlarmsCount),
                    NavItem(AppScreen.LOGS, "데이터 로그", Icons.Default.Storage),
                    NavItem(AppScreen.DEBUGGER, "통신 디버거", Icons.Default.BugReport)
                )

                navItems.forEach { item ->
                    val selected = currentScreen == item.screen
                    NavigationBarItem(
                        selected = selected,
                        onClick = { viewModel.navigateTo(item.screen) },
                        icon = {
                            if (item.badgeCount > 0) {
                                BadgedBox(badge = {
                                    Badge(containerColor = StatusCritical) {
                                        Text("${item.badgeCount}", fontSize = 9.sp)
                                    }
                                }) {
                                    Icon(item.icon, contentDescription = item.label)
                                }
                            } else {
                                Icon(item.icon, contentDescription = item.label)
                            }
                        },
                        label = {
                            Text(
                                text = item.label,
                                fontSize = 10.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyanPrimary,
                            selectedTextColor = CyanPrimary,
                            indicatorColor = CyanPrimary.copy(alpha = 0.2f),
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.testTag("nav_item_${item.screen.name.lowercase()}")
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(IndustrialBackground)
        ) {
            when (currentScreen) {
                AppScreen.DASHBOARD -> DashboardScreen(
                    machines = machines,
                    activeAlarms = activeAlarms,
                    commDiag = commDiag,
                    onSelectMachine = { machineId ->
                        viewModel.selectMachine(machineId)
                    },
                    onNavigate = { screen ->
                        viewModel.navigateTo(screen)
                    },
                    onAcknowledgeAlarm = { id ->
                        viewModel.acknowledgeAlarm(id)
                    }
                )

                AppScreen.MACHINES -> MachineDetailScreen(
                    machines = machines,
                    selectedMachineId = selectedMachineId,
                    currentUser = currentUser,
                    onSelectMachine = { viewModel.selectMachine(it) },
                    onUpdateTargetRpm = { id, rpm -> viewModel.updateTargetRpm(id, rpm) },
                    onUpdateTargetPressure = { id, p -> viewModel.updateTargetPressure(id, p) },
                    onToggleEmergencyStop = { id -> viewModel.toggleEmergencyStop(id) },
                    onNavigateBack = { viewModel.navigateTo(AppScreen.DASHBOARD) }
                )

                AppScreen.TELEMETRY -> TelemetryScreen(
                    machines = machines,
                    selectedMachineId = selectedMachineId,
                    selectedMetric = selectedMetric,
                    recentDbLogs = recentTelemetryLogs,
                    onSelectMachine = { viewModel.selectMachine(it) },
                    onSelectMetric = { viewModel.setSelectedMetric(it) }
                )

                AppScreen.ALARMS -> AlarmsScreen(
                    alarms = activeAlarms,
                    filterSeverity = alarmFilterSeverity,
                    onFilterChange = { viewModel.setAlarmFilterSeverity(it) },
                    onAcknowledgeAlarm = { viewModel.acknowledgeAlarm(it) },
                    onAcknowledgeAll = { viewModel.acknowledgeAllAlarms() },
                    onClearAcknowledged = { viewModel.clearAcknowledgedAlarms() }
                )

                AppScreen.LOGS -> LogsScreen(
                    telemetryLogs = recentTelemetryLogs,
                    alarmHistory = alarmHistory,
                    searchQuery = logSearchQuery,
                    onSearchChange = { viewModel.setLogSearchQuery(it) },
                    onExportCsv = { onReady -> viewModel.exportCsvLogs(onReady) },
                    onClearLogs = { viewModel.clearAllLogs() }
                )

                AppScreen.DEBUGGER -> CommDebuggerScreen(
                    diagnostics = commDiag,
                    livePackets = livePackets,
                    onToggleFaultSimulation = { viewModel.toggleFaultSimulation(it) },
                    onManualFailover = { viewModel.switchServerManual(it) },
                    onSendManualCommand = { addr, v ->
                        selectedMachineId?.let { mid ->
                            viewModel.requestPinAuth("레지스터 쓰기 [0x${String.format("%04X", addr)}]", UserRole.ENGINEER) {
                                viewModel.viewModelScopeLaunchManual(mid, addr, v)
                            }
                        }
                    }
                )

                AppScreen.USERS -> UserManagementScreen(
                    currentUser = currentUser,
                    accounts = accounts,
                    onSwitchUserByPin = { viewModel.switchUserByPin(it) },
                    onDirectSwitchUser = { viewModel.switchUserDirectly(it) }
                )
            }

            // PIN Authorization Dialog (if prompt active)
            if (pinPrompt != null) {
                PinAuthDialog(
                    actionTitle = pinPrompt!!.title,
                    requiredRole = pinPrompt!!.requiredRole,
                    onPinSubmit = { pin -> viewModel.submitPinAuth(pin) },
                    onDismiss = { viewModel.dismissPinAuth() }
                )
            }
        }
    }
}

private data class NavItem(
    val screen: AppScreen,
    val label: String,
    val icon: ImageVector,
    val badgeCount: Int = 0
)

private fun PlcViewModel.viewModelScopeLaunchManual(machineId: String, address: Int, value: Int) {
    this.repository.commEngine.let { engine ->
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).run {
            kotlinx.coroutines.runBlocking {
                engine.sendPlcCommand(machineId, address, value, "수동 디버깅 레지스터 쓰기")
            }
        }
    }
}
