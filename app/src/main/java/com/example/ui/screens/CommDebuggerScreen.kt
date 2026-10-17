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
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.example.data.model.CommDiagnostics
import com.example.data.model.CommPacket
import com.example.data.model.PacketDirection
import com.example.data.model.PacketStatus
import com.example.data.model.ProtocolType
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CommDebuggerScreen(
    diagnostics: CommDiagnostics,
    livePackets: List<CommPacket>,
    onToggleFaultSimulation: (Boolean) -> Unit,
    onManualFailover: (Boolean) -> Unit,
    onSendManualCommand: (address: Int, value: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val sdf = SimpleDateFormat("HH:mm:ss.SSS", Locale.KOREA)
    var testAddress by remember { mutableStateOf("40001") }
    var testValue by remember { mutableStateOf("1500") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(IndustrialBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Redundant Server Architecture & Health Card
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
                                imageVector = Icons.Default.Cable,
                                contentDescription = null,
                                tint = CyanPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "이중화 통신 아키텍처 및 헬스 체크",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Connection Health Score Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (diagnostics.healthScore > 80) StatusRunning.copy(alpha = 0.2f) else StatusCritical.copy(alpha = 0.2f))
                                .border(
                                    1.dp,
                                    if (diagnostics.healthScore > 80) StatusRunning else StatusCritical,
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "통신 신뢰도: ${diagnostics.healthScore}%",
                                color = if (diagnostics.healthScore > 80) StatusRunning else StatusCritical,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Active Endpoint Status
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(IndustrialSurfaceElevated)
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("현재 활성 엔드포인트:", color = TextSecondary, fontSize = 11.sp)
                                Text(
                                    text = if (diagnostics.isFailoverActive) "SECONDARY 절체" else "PRIMARY 운용",
                                    color = if (diagnostics.isFailoverActive) OrangeSecondary else CyanPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${diagnostics.protocol.label} : ${diagnostics.activeEndpoint}",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Metrics 4-Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricSmallBox("응답 지연(RTT)", "${diagnostics.latencyMs} ms", CyanPrimary, Modifier.weight(1f))
                        MetricSmallBox("지터 (Jitter)", "±${diagnostics.jitterMs} ms", TextSecondary, Modifier.weight(1f))
                        MetricSmallBox("패킷 손실률", "${diagnostics.packetLossPct}%", if (diagnostics.packetLossPct < 1f) StatusRunning else StatusCritical, Modifier.weight(1f))
                        MetricSmallBox("버퍼 큐 적재", "${diagnostics.bufferQueuedCount} 개", if (diagnostics.bufferQueuedCount == 0) TextMuted else OrangeSecondary, Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Server Failover Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onManualFailover(false) },
                            enabled = diagnostics.isFailoverActive,
                            modifier = Modifier.weight(1f).height(36.dp)
                        ) {
                            Text("Primary 주 서버 복귀", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = { onManualFailover(true) },
                            enabled = !diagnostics.isFailoverActive,
                            modifier = Modifier.weight(1f).height(36.dp)
                        ) {
                            Text("Secondary 예비 서버 절체", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Fault Injection & Resilient Recovery Testing Rig
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, if (diagnostics.simulateFaultMode) StatusCritical else IndustrialSurfaceBorder, RoundedCornerShape(14.dp)),
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
                                imageVector = Icons.Default.BugReport,
                                contentDescription = null,
                                tint = OrangeSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "통신 예외 및 무중단 장애 복구 시험",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Switch(
                            checked = diagnostics.simulateFaultMode,
                            onCheckedChange = onToggleFaultSimulation,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = StatusCritical,
                                checkedTrackColor = StatusCritical.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.testTag("switch_fault_simulation")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (diagnostics.simulateFaultMode) {
                            "🚨 장애 발생 시뮬레이션 중: 3회 이상 타임아웃 감지 시 보조(Secondary) 서버로 무중단 자동 절체(Failover)되며 전송 명령은 메모리 버퍼 큐에 안전하게 보호됩니다."
                        } else {
                            "스위치를 켜면 네트워크 선로 단선 및 패킷 유실을 시뮬레이션하여 자동 복구와 오프라인 버퍼링을 실시간 검증할 수 있습니다."
                        },
                        color = if (diagnostics.simulateFaultMode) StatusCritical else TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        // Manual Modbus Register Read/Write Tool
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = IndustrialSurface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "수동 레지스터 송수신 테스트 (Manual Register Test)",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = testAddress,
                            onValueChange = { testAddress = it },
                            label = { Text("주소 (Dec)", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanPrimary,
                                unfocusedBorderColor = IndustrialSurfaceBorder
                            )
                        )

                        OutlinedTextField(
                            value = testValue,
                            onValueChange = { testValue = it },
                            label = { Text("값 (Value)", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanPrimary,
                                unfocusedBorderColor = IndustrialSurfaceBorder
                            )
                        )

                        Button(
                            onClick = {
                                val addr = testAddress.toIntOrNull() ?: 40001
                                val v = testValue.toIntOrNull() ?: 0
                                onSendManualCommand(addr, v)
                            },
                            modifier = Modifier.height(52.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("전송", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Live Packet Inspector Terminal
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CompareArrows,
                        contentDescription = null,
                        tint = CyanPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "실시간 패킷 인스펙터 (Live Packet Terminal)",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "최근 ${livePackets.size}건",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }

        if (livePackets.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("패킷 대기 중...", color = TextMuted)
                }
            }
        } else {
            items(livePackets.take(30), key = { it.id }) { packet ->
                PacketTerminalRow(packet = packet, formattedTime = sdf.format(Date(packet.timestamp)))
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PacketTerminalRow(packet: CommPacket, formattedTime: String) {
    val dirColor = when (packet.direction) {
        PacketDirection.TX -> CyanPrimary
        PacketDirection.RX -> if (packet.status == PacketStatus.SUCCESS) StatusRunning else StatusCritical
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = IndustrialSurface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(dirColor.copy(alpha = 0.2f))
                            .border(1.dp, dirColor, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = packet.direction.name,
                            color = dirColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = packet.protocol.label,
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (packet.latencyMs > 0) {
                        Text(
                            text = "${packet.latencyMs}ms | ",
                            color = CyanPrimary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Text(
                        text = formattedTime,
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Hex Dump
            Text(
                text = "HEX: [${packet.hexDump}]",
                color = TextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Parsed Description
            Text(
                text = packet.description,
                color = if (packet.status == PacketStatus.SUCCESS) TextPrimary else StatusCritical,
                fontSize = 11.sp,
                fontWeight = if (packet.status == PacketStatus.SUCCESS) FontWeight.Normal else FontWeight.Bold
            )
        }
    }
}

@Composable
private fun MetricSmallBox(label: String, value: String, accentColor: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(IndustrialSurfaceElevated)
            .border(0.5.dp, IndustrialSurfaceBorder, RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Column {
            Text(text = label, color = TextMuted, fontSize = 10.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                color = accentColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
