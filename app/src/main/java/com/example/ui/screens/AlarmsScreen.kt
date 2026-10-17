package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AlarmItem
import com.example.data.model.AlarmSeverity
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.IndustrialBackground
import com.example.ui.theme.IndustrialSurface
import com.example.ui.theme.IndustrialSurfaceBorder
import com.example.ui.theme.IndustrialSurfaceElevated
import com.example.ui.theme.StatusCritical
import com.example.ui.theme.StatusRunning
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AlarmsScreen(
    alarms: List<AlarmItem>,
    filterSeverity: String?,
    onFilterChange: (String?) -> Unit,
    onAcknowledgeAlarm: (String) -> Unit,
    onAcknowledgeAll: () -> Unit,
    onClearAcknowledged: () -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredAlarms = when (filterSeverity) {
        "CRITICAL" -> alarms.filter { it.severity == AlarmSeverity.CRITICAL }
        "WARNING" -> alarms.filter { it.severity == AlarmSeverity.WARNING }
        "INFO" -> alarms.filter { it.severity == AlarmSeverity.INFO }
        else -> alarms
    }

    val unackCount = alarms.count { !it.isAcknowledged }
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(IndustrialBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Summary Card & Action Bar
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = IndustrialSurface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = if (unackCount > 0) StatusCritical else StatusRunning,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "설비 알람 센터",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "미확인: ${unackCount}건 / 전체: ${alarms.size}건",
                            color = if (unackCount > 0) StatusCritical else StatusRunning,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onAcknowledgeAll,
                            enabled = unackCount > 0,
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("btn_ack_all"),
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                        ) {
                            Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("전체 일괄 확인", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onClearAcknowledged,
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("확인 알람 정리", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Filter Tabs
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AlarmFilterChip("전체 (${alarms.size})", "ALL", filterSeverity) { onFilterChange("ALL") }
                AlarmFilterChip(
                    "위험 (${alarms.count { it.severity == AlarmSeverity.CRITICAL }})",
                    "CRITICAL",
                    filterSeverity
                ) { onFilterChange("CRITICAL") }
                AlarmFilterChip(
                    "경고 (${alarms.count { it.severity == AlarmSeverity.WARNING }})",
                    "WARNING",
                    filterSeverity
                ) { onFilterChange("WARNING") }
                AlarmFilterChip(
                    "정보 (${alarms.count { it.severity == AlarmSeverity.INFO }})",
                    "INFO",
                    filterSeverity
                ) { onFilterChange("INFO") }
            }
        }

        // Alarm Items
        if (filteredAlarms.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = StatusRunning,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "현재 조건에 해당하는 활성 알람이 없습니다.",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        } else {
            items(filteredAlarms, key = { it.id }) { alarm ->
                AlarmCardItem(
                    alarm = alarm,
                    formattedTime = sdf.format(Date(alarm.timestamp)),
                    onAcknowledge = { onAcknowledgeAlarm(alarm.id) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AlarmCardItem(
    alarm: AlarmItem,
    formattedTime: String,
    onAcknowledge: () -> Unit
) {
    val (severityColor, severityLabel) = when (alarm.severity) {
        AlarmSeverity.CRITICAL -> StatusCritical to "위험 (CRITICAL)"
        AlarmSeverity.WARNING -> StatusWarning to "경고 (WARNING)"
        AlarmSeverity.INFO -> CyanPrimary to "정보 (INFO)"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("alarm_item_${alarm.id}")
            .border(
                1.dp,
                if (!alarm.isAcknowledged) severityColor.copy(alpha = 0.6f) else IndustrialSurfaceBorder,
                RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (!alarm.isAcknowledged) severityColor.copy(alpha = 0.08f) else IndustrialSurface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Severity, Code, Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(severityColor)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = severityLabel,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = alarm.code,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = formattedTime,
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Machine & Message
            Text(
                text = "[${alarm.machineId}] ${alarm.machineName}",
                color = CyanPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = alarm.message,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Acknowledgment status / Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (alarm.isAcknowledged) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = StatusRunning,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "확인 완료: ${alarm.acknowledgedBy ?: "작업자"}",
                            color = StatusRunning,
                            fontSize = 11.sp
                        )
                    }
                } else {
                    Text(
                        text = "⚠️ 미확인 경보",
                        color = StatusCritical,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = onAcknowledge,
                        colors = ButtonDefaults.buttonColors(containerColor = severityColor),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("ack_btn_${alarm.id}")
                    ) {
                        Text("확인 (ACK)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun AlarmFilterChip(
    label: String,
    filterKey: String,
    currentFilter: String?,
    onClick: () -> Unit
) {
    val isSelected = (currentFilter == null && filterKey == "ALL") || currentFilter == filterKey
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) CyanPrimary.copy(alpha = 0.2f) else IndustrialSurface)
            .border(1.dp, if (isSelected) CyanPrimary else IndustrialSurfaceBorder, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) CyanPrimary else TextSecondary,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
