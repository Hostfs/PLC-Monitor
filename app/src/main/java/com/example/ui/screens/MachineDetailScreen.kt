package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PlcMachine
import com.example.data.model.PlcRegister
import com.example.data.model.UserAccount
import com.example.ui.components.EmergencyStopButton
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
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.AppScreen

@Composable
fun MachineDetailScreen(
    machines: List<PlcMachine>,
    selectedMachineId: String?,
    currentUser: UserAccount,
    onSelectMachine: (String) -> Unit,
    onUpdateTargetRpm: (String, Float) -> Unit,
    onUpdateTargetPressure: (String, Float) -> Unit,
    onToggleEmergencyStop: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }

    val currentMachine = machines.find { it.id == selectedMachineId } ?: machines.firstOrNull()

    if (currentMachine == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("등록된 설비가 없습니다.", color = TextMuted)
        }
        return
    }

    var sliderRpm by remember(currentMachine.id) { mutableFloatStateOf(currentMachine.targetRpm) }
    var sliderPressure by remember(currentMachine.id) { mutableFloatStateOf(currentMachine.targetPressure) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(IndustrialBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Machine Selector Tab Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                machines.forEach { m ->
                    val isSelected = m.id == currentMachine.id
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) CyanPrimary.copy(alpha = 0.2f) else IndustrialSurface)
                            .border(
                                1.dp,
                                if (isSelected) CyanPrimary else IndustrialSurfaceBorder,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onSelectMachine(m.id) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "${m.id} (${m.name.take(6)}..)",
                            color = if (isSelected) CyanPrimary else TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        // Header Card with Status
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
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = currentMachine.id,
                                    color = CyanPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = currentMachine.name,
                                    color = TextPrimary,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "통신 IP: ${currentMachine.ipAddress}:${currentMachine.port} | ${currentMachine.lineName}",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        }
                        StatusPill(status = currentMachine.status)
                    }
                }
            }
        }

        // Real-time Dual Gauges
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = IndustrialSurface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "실시간 주요 계측 게이지",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadialGauge(
                            value = currentMachine.telemetry.rpm,
                            minValue = 0f,
                            maxValue = 3000f,
                            unit = "RPM",
                            label = "주축 속도",
                            size = 130.dp,
                            warningThreshold = 2200f,
                            criticalThreshold = 2600f
                        )
                        RadialGauge(
                            value = currentMachine.telemetry.temperature,
                            minValue = 20f,
                            maxValue = 120f,
                            unit = "°C",
                            label = "설비 온도",
                            size = 130.dp,
                            warningThreshold = 80f,
                            criticalThreshold = 90f
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Secondary metrics row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricSmallBox(
                            label = "유압/공압",
                            value = "${String.format("%.1f", currentMachine.telemetry.pressure)} Bar",
                            modifier = Modifier.weight(1f)
                        )
                        MetricSmallBox(
                            label = "냉각 유량",
                            value = "${String.format("%.1f", currentMachine.telemetry.flowRate)} L/m",
                            modifier = Modifier.weight(1f)
                        )
                        MetricSmallBox(
                            label = "소비 전력",
                            value = "${String.format("%.1f", currentMachine.telemetry.powerKw)} kW",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Emergency Stop Safety Button
        item {
            EmergencyStopButton(
                isEngaged = currentMachine.emergencyStopEngaged,
                machineName = currentMachine.name,
                onConfirmToggle = {
                    onToggleEmergencyStop(currentMachine.id)
                }
            )
        }

        // Setpoint Tuning (Engineer / Admin PIN Protected)
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
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = OrangeSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "PLC 설정값 제어 (Setpoint Tuning)",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (!currentUser.role.canEditSetpoints) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = OrangeSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "엔지니어 인증 필요",
                                    color = OrangeSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Target RPM Slider
                    Text(
                        text = "목표 주축 속도 (Target RPM): ${sliderRpm.toInt()} RPM",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Slider(
                        value = sliderRpm,
                        onValueChange = { sliderRpm = it },
                        valueRange = 0f..3000f,
                        steps = 59,
                        colors = SliderDefaults.colors(
                            thumbColor = CyanPrimary,
                            activeTrackColor = CyanPrimary
                        ),
                        modifier = Modifier.testTag("slider_target_rpm")
                    )
                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { onUpdateTargetRpm(currentMachine.id, sliderRpm) },
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("RPM 설정 반영 (Write Reg: 40001)", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Target Pressure Slider
                    Text(
                        text = "목표 유압 (Target Pressure): ${String.format("%.1f", sliderPressure)} Bar",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Slider(
                        value = sliderPressure,
                        onValueChange = { sliderPressure = it },
                        valueRange = 0f..20f,
                        steps = 39,
                        colors = SliderDefaults.colors(
                            thumbColor = OrangeSecondary,
                            activeTrackColor = OrangeSecondary
                        ),
                        modifier = Modifier.testTag("slider_target_pressure")
                    )
                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { onUpdateTargetPressure(currentMachine.id, sliderPressure) },
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("압력 설정 반영 (Write Reg: 40010)", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Live PLC Registers Table
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = IndustrialSurface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "PLC 모드버스 레지스터 맵 (Live Register Map)",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Table Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(IndustrialSurfaceElevated)
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text("주소", color = TextMuted, fontSize = 11.sp, modifier = Modifier.width(60.dp))
                        Text("레지스터 명칭", color = TextMuted, fontSize = 11.sp, modifier = Modifier.weight(1f))
                        Text("타입", color = TextMuted, fontSize = 11.sp, modifier = Modifier.width(64.dp))
                        Text("현재값", color = TextMuted, fontSize = 11.sp, modifier = Modifier.width(70.dp))
                    }

                    currentMachine.registers.forEach { reg ->
                        RegisterRowItem(reg)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MetricSmallBox(label: String, value: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(IndustrialSurfaceElevated)
            .padding(10.dp)
    ) {
        Column {
            Text(text = label, color = TextMuted, fontSize = 10.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun RegisterRowItem(reg: PlcRegister) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, IndustrialSurfaceBorder)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${reg.address}",
            color = CyanPrimary,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.width(60.dp)
        )
        Text(
            text = reg.name,
            color = TextPrimary,
            fontSize = 12.sp,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = reg.type,
            color = if (reg.isWritable) OrangeSecondary else TextMuted,
            fontSize = 10.sp,
            modifier = Modifier.width(64.dp)
        )
        Text(
            text = "${reg.value} ${reg.unit}",
            color = StatusRunning,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.width(70.dp)
        )
    }
}
