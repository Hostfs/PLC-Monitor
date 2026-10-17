package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TelemetryEntity
import com.example.data.model.PlcMachine
import com.example.ui.components.TelemetryChart
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
import kotlin.math.sqrt

@Composable
fun TelemetryScreen(
    machines: List<PlcMachine>,
    selectedMachineId: String?,
    selectedMetric: String,
    recentDbLogs: List<TelemetryEntity>,
    onSelectMachine: (String) -> Unit,
    onSelectMetric: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentMachine = machines.find { it.id == selectedMachineId } ?: machines.firstOrNull()

    // Sliding window of live points (up to 40 data points)
    val liveDataBuffer = remember(currentMachine?.id, selectedMetric) { mutableStateListOf<Float>() }

    LaunchedEffect(currentMachine?.telemetry?.timestamp) {
        val t = currentMachine?.telemetry ?: return@LaunchedEffect
        val newVal = when (selectedMetric) {
            "RPM" -> t.rpm
            "TEMP" -> t.temperature
            "PRESSURE" -> t.pressure
            "FLOW" -> t.flowRate
            "POWER" -> t.powerKw
            else -> t.temperature
        }
        liveDataBuffer.add(newVal)
        if (liveDataBuffer.size > 36) {
            liveDataBuffer.removeAt(0)
        }
    }

    val (metricLabel, unit, warnLimit, critLimit, chartColor) = when (selectedMetric) {
        "RPM" -> Tuple5("주축 회전 속도 (Spindle Speed)", "RPM", 2200f, 2600f, CyanPrimary)
        "TEMP" -> Tuple5("설비 코어 온도 (Core Temp)", "°C", 80f, 90f, OrangeSecondary)
        "PRESSURE" -> Tuple5("유압/공압 라인 압력 (Pressure)", "Bar", 12f, 15f, Color(0xFF00E5FF))
        "FLOW" -> Tuple5("냉각수 순환 유량 (Flow Rate)", "L/min", null, null, Color(0xFF10B981))
        "POWER" -> Tuple5("설비 소비 전력 (Active Power)", "kW", 25f, 32f, Color(0xFFFFB74D))
        else -> Tuple5("온도", "°C", 80f, 90f, OrangeSecondary)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(IndustrialBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Machine Switcher
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                machines.forEach { m ->
                    val isSelected = m.id == currentMachine?.id
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
                            text = m.id,
                            color = if (isSelected) CyanPrimary else TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Metric Filter Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricChip("온도 (°C)", "TEMP", selectedMetric) { onSelectMetric("TEMP") }
                MetricChip("속도 (RPM)", "RPM", selectedMetric) { onSelectMetric("RPM") }
                MetricChip("압력 (Bar)", "PRESSURE", selectedMetric) { onSelectMetric("PRESSURE") }
                MetricChip("유량 (L/m)", "FLOW", selectedMetric) { onSelectMetric("FLOW") }
                MetricChip("전력 (kW)", "POWER", selectedMetric) { onSelectMetric("POWER") }
            }
        }

        // Live Telemetry Chart
        item {
            TelemetryChart(
                dataPoints = liveDataBuffer.toList(),
                unit = unit,
                title = "$metricLabel [${currentMachine?.id ?: ""}]",
                lineColor = chartColor,
                warningLimit = warnLimit,
                criticalLimit = critLimit
            )
        }

        // Real-time Statistical Analysis
        item {
            val pts = liveDataBuffer.toList()
            val maxVal = pts.maxOrNull() ?: 0f
            val minVal = pts.minOrNull() ?: 0f
            val avgVal = if (pts.isNotEmpty()) pts.average().toFloat() else 0f
            val variance = if (pts.isNotEmpty()) {
                pts.map { (it - avgVal) * (it - avgVal) }.average().toFloat()
            } else 0f
            val stdDev = sqrt(variance)

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = IndustrialSurface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ShowChart,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "실시간 통계 및 분포 분석 (Statistical Metrics)",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatBox("최고 피크 (MAX)", "${String.format("%.1f", maxVal)} $unit", StatusCritical, Modifier.weight(1f))
                        StatBox("최저점 (MIN)", "${String.format("%.1f", minVal)} $unit", StatusRunning, Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatBox("평균값 (MEAN)", "${String.format("%.1f", avgVal)} $unit", CyanPrimary, Modifier.weight(1f))
                        StatBox("표준편차 (σ)", String.format("%.2f", stdDev), OrangeSecondary, Modifier.weight(1f))
                    }
                }
            }
        }

        // Limit Guidelines Info Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = IndustrialSurfaceElevated),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "임계치 경보 기준 (Safety Threshold Limits)",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• 주의 경보 (점선 경계): ${warnLimit?.let { "$it $unit 이상" } ?: "설정 없음"}\n• 위험 이상 정지: ${critLimit?.let { "$it $unit 돌파 시 자동 비상 알람 트리거" } ?: "설정 없음"}",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MetricChip(
    label: String,
    metricKey: String,
    selectedMetric: String,
    onClick: () -> Unit
) {
    val isSelected = metricKey == selectedMetric
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) CyanPrimary else IndustrialSurface)
            .border(1.dp, if (isSelected) CyanPrimary else IndustrialSurfaceBorder, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) Color(0xFF090D16) else TextSecondary,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun StatBox(label: String, value: String, accentColor: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(IndustrialSurfaceElevated)
            .border(0.5.dp, IndustrialSurfaceBorder, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Column {
            Text(text = label, color = TextMuted, fontSize = 10.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                color = accentColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

private data class Tuple5<A, B, C, D, E>(val a: A, val b: B, val c: C, val d: D, val e: E)
