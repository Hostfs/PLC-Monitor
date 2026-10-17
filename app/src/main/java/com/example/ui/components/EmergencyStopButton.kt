package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.IndustrialSurface
import com.example.ui.theme.IndustrialSurfaceBorder
import com.example.ui.theme.StatusCritical
import com.example.ui.theme.StatusRunning
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun EmergencyStopButton(
    isEngaged: Boolean,
    machineName: String,
    onConfirmToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    val buttonBgColor by animateColorAsState(
        targetValue = if (isEngaged) StatusCritical else Color(0xFFD32F2F),
        label = "estop_color"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(IndustrialSurface)
            .border(2.dp, if (isEngaged) StatusCritical else IndustrialSurfaceBorder, RoundedCornerShape(14.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "SAFETY EMERGENCY INTERLOCK",
                color = if (isEngaged) StatusCritical else TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Mushroom Button
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .testTag("emergency_stop_button")
                    .shadow(8.dp, CircleShape)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = if (isEngaged) {
                                listOf(Color(0xFFFF5252), Color(0xFFB71C1C))
                            } else {
                                listOf(Color(0xFFE53935), Color(0xFF8E0000))
                            }
                        )
                    )
                    .border(4.dp, Color(0xFFFFD600), CircleShape)
                    .clickable { showDialog = true },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "비상 정지",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = if (isEngaged) "RELEASE" else "E-STOP",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = if (isEngaged) "⚠️ 비상 정지 작동 중 (설비 정지 상태)" else "정상 가동 가능 (비상정지 대기)",
                color = if (isEngaged) StatusCritical else StatusRunning,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    text = if (isEngaged) "비상 정지 해제 확인" else "⚠️ 비상 정지(E-STOP) 발령",
                    color = if (isEngaged) StatusRunning else StatusCritical,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (isEngaged) {
                        "[$machineName]의 비상 정지를 해제하고 인터록 회로를 복구하시겠습니까?\n작업자의 안전을 사전에 반드시 확인하십시오."
                    } else {
                        "[$machineName]의 주축 모터와 유압 공급을 즉시 차단하고 설비를 긴급 정지합니다.\n계속 진행하시겠습니까?"
                    },
                    color = TextPrimary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog = false
                        onConfirmToggle()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isEngaged) StatusRunning else StatusCritical
                    )
                ) {
                    Text(
                        text = if (isEngaged) "안전 확인 및 복구" else "즉시 긴급 정지 실행",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDialog = false }) {
                    Text("취소")
                }
            }
        )
    }
}
