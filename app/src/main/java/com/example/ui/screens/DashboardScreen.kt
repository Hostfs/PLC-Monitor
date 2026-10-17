package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.data.model.AlarmItem
import com.example.data.model.CommDiagnostics
import com.example.data.model.MachineStatus
import com.example.data.model.PlcMachine
import com.example.ui.components.RadialGauge
import com.example.ui.components.StatusPill
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.IndustrialBackground
import com.example.ui.theme.IndustrialSurface
import com.example.ui.theme.IndustrialSurfaceBorder
import com.example.ui.theme.IndustrialSurfaceElevated
import com.example.ui.theme.OrangeSecondary
import com.example.ui.theme.StatusCritical
import com.example.ui.theme.StatusRunning
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.AppScreen

@Composable
fun DashboardScreen(
    machines: List<PlcMachine>,
    activeAlarms: List<AlarmItem>,
    commDiag: CommDiagnostics,
    onSelectMachine: (String) -> Unit,
    onNavigate: (AppScreen) -> Unit,
    onAcknowledgeAlarm: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val unackAlarms = activeAlarms.filter { !it.isAcknowledged }
    val runningCount = machines.count { it.status == MachineStatus.RUNNING }
    val warningCount = machines.count { it.status == MachineStatus.WARNING }
    val faultCount = machines.count { it.status == MachineStatus.ALARM }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(IndustrialBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // High-level Plant KPI Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KpiCard(
                    title = "가동률 (OEE)",
                    value = "94.8%",
                    subText = "총 $runningCount/${machines.size}대 가동 중",
                    icon = Icons.Default.PrecisionManufacturing,
                    accentColor = StatusRunning,
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "통신 신뢰도",
                    value = "${commDiag.healthScore}%",
                    subText = if (commDiag.isFailoverActive) "Secondary 절체 가동" else "Primary 연결 정상",
                    icon = Icons.Default.Sensors,
                    accentColor = if (commDiag.healthScore > 80) CyanPrimary else OrangeSecondary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Active Alarms Alert Banner (if any)
        if (unackAlarms.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("alarm_alert_banner")
                        .border(1.dp, StatusCritical.copy(alpha = 0.6f), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = StatusCritical.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = StatusCritical,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "미확인 이상 경보 ${unackAlarms.size}건 감지됨",
                                color = StatusCritical,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "전체보기",
                                color = CyanPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.clickable { onNavigate(AppScreen.ALARMS) }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        val topAlarm = unackAlarms.first()
                        Text(
                            text = "[${topAlarm.machineName}] ${topAlarm.message}",
                            color = TextPrimary,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { onAcknowledgeAlarm(topAlarm.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = StatusCritical),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("알람 확인 (ACK)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Section Title: Plant Floor Equipment
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "스마트 팩토리 설비 현황",
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "실시간 모니터링 및 상태 텔레메트리",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    StatusBadgeMini("정상 $runningCount", StatusRunning)
                    if (warningCount > 0) StatusBadgeMini("주의 $warningCount", StatusWarning)
                    if (faultCount > 0) StatusBadgeMini("정지 $faultCount", StatusCritical)
                }
            }
        }

        // Equipment Cards List
        items(machines) { machine ->
            MachineDashboardCard(
                machine = machine,
                onClick = {
                    onSelectMachine(machine.id)
                    onNavigate(AppScreen.MACHINES)
                }
            )
        }

        // Bottom Safe Padding
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun KpiCard(
    title: String,
    value: String,
    subText: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.border(1.dp, IndustrialSurfaceBorder, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = IndustrialSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subText,
                color = TextMuted,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun MachineDashboardCard(
    machine: PlcMachine,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("machine_card_${machine.id}")
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, IndustrialSurfaceBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = IndustrialSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Machine Name, Line, Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = machine.id,
                            color = CyanPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(IndustrialSurfaceElevated)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = machine.name,
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "${machine.lineName} • ${machine.ipAddress}:${machine.port}",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }

                StatusPill(status = machine.status)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Gauge & Telemetry Quick Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mini Radial Gauge for Temperature
                RadialGauge(
                    value = machine.telemetry.temperature,
                    minValue = 20f,
                    maxValue = 120f,
                    unit = "°C",
                    label = "온도",
                    size = 100.dp,
                    warningThreshold = 80f,
                    criticalThreshold = 90f
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Telemetry Quick Specs
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TelemetryRowMini(label = "주축 속도", value = "${machine.telemetry.rpm.toInt()} RPM")
                    TelemetryRowMini(label = "유압/압력", value = "${String.format("%.1f", machine.telemetry.pressure)} Bar")
                    TelemetryRowMini(label = "실시간 전력", value = "${String.format("%.1f", machine.telemetry.powerKw)} kW")
                    TelemetryRowMini(label = "공급 전압", value = "${String.format("%.1f", machine.telemetry.voltage)} V")
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "상세 보기",
                    tint = TextMuted,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun TelemetryRowMini(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextSecondary, fontSize = 11.sp)
        Text(
            text = value,
            color = TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun StatusBadgeMini(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(text = text, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}
