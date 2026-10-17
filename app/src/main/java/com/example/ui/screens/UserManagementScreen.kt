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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserAccount
import com.example.data.model.UserRole
import com.example.ui.components.PinAuthDialog
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

@Composable
fun UserManagementScreen(
    currentUser: UserAccount,
    accounts: List<UserAccount>,
    onSwitchUserByPin: (String) -> Boolean,
    onDirectSwitchUser: (UserAccount) -> Unit,
    modifier: Modifier = Modifier
) {
    var showPinDialogForRole by remember { mutableStateOf<UserRole?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(IndustrialBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Current User Profile Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyanPrimary.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = IndustrialSurface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // User Avatar
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(CyanPrimary.copy(alpha = 0.2f))
                                .border(2.dp, CyanPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (currentUser.role) {
                                    UserRole.ADMIN -> Icons.Default.AdminPanelSettings
                                    UserRole.ENGINEER -> Icons.Default.Engineering
                                    UserRole.OPERATOR -> Icons.Default.Person
                                },
                                contentDescription = null,
                                tint = CyanPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentUser.username,
                                color = TextPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "소속: ${currentUser.department} | ID: ${currentUser.id}",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        }

                        // Role Tag
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    when (currentUser.role) {
                                        UserRole.ADMIN -> StatusCritical.copy(alpha = 0.2f)
                                        UserRole.ENGINEER -> OrangeSecondary.copy(alpha = 0.2f)
                                        UserRole.OPERATOR -> StatusRunning.copy(alpha = 0.2f)
                                    }
                                )
                                .border(
                                    1.dp,
                                    when (currentUser.role) {
                                        UserRole.ADMIN -> StatusCritical
                                        UserRole.ENGINEER -> OrangeSecondary
                                        UserRole.OPERATOR -> StatusRunning
                                    },
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = currentUser.role.name,
                                color = when (currentUser.role) {
                                    UserRole.ADMIN -> StatusCritical
                                    UserRole.ENGINEER -> OrangeSecondary
                                    UserRole.OPERATOR -> StatusRunning
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = currentUser.role.description,
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Fast Account Switcher List
        item {
            Text(
                text = "사용자 계정 전환 (Role Fast Switch)",
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        items(accounts) { account ->
            val isCurrent = account.id == currentUser.id
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        1.dp,
                        if (isCurrent) CyanPrimary else IndustrialSurfaceBorder,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable {
                        onDirectSwitchUser(account)
                    },
                colors = CardDefaults.cardColors(containerColor = IndustrialSurface)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = account.username,
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (isCurrent) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "[현재 활성]",
                                    color = CyanPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = "${account.department} • 기본 PIN: ${account.pinCode}",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Button(
                        onClick = { onDirectSwitchUser(account) },
                        enabled = !isCurrent,
                        modifier = Modifier.height(34.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                    ) {
                        Text(if (isCurrent) "활성화됨" else "전환", fontSize = 11.sp)
                    }
                }
            }
        }

        // Permissions Matrix Table
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = IndustrialSurface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "권한 등급 매트릭스 (Role Privilege Matrix)",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Matrix Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(IndustrialSurfaceElevated)
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text("기능 및 제어 권한", color = TextMuted, fontSize = 11.sp, modifier = Modifier.weight(1f))
                        Text("운영자", color = TextMuted, fontSize = 11.sp, modifier = Modifier.width(44.dp))
                        Text("엔지니어", color = TextMuted, fontSize = 11.sp, modifier = Modifier.width(52.dp))
                        Text("관리자", color = TextMuted, fontSize = 11.sp, modifier = Modifier.width(44.dp))
                    }

                    PermissionRow("실시간 텔레메트리 모니터링", true, true, true)
                    PermissionRow("설비 알람 확인(ACK) 처리", true, true, true)
                    PermissionRow("주축/유압 목표 설정값(Setpoint) 튜닝", false, true, true)
                    PermissionRow("설비 비상 정지(E-STOP) 및 인터록 해제", false, true, true)
                    PermissionRow("통신 파라미터 및 이중화 IP 설정", false, false, true)
                    PermissionRow("전체 데이터 로그 삭제 및 사용자 관리", false, false, true)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showPinDialogForRole != null) {
        val role = showPinDialogForRole!!
        PinAuthDialog(
            actionTitle = "${role.title} 계정 전환",
            requiredRole = role,
            onPinSubmit = { pin ->
                val ok = onSwitchUserByPin(pin)
                if (ok) showPinDialogForRole = null
                ok
            },
            onDismiss = { showPinDialogForRole = null }
        )
    }
}

@Composable
private fun PermissionRow(
    feature: String,
    operator: Boolean,
    engineer: Boolean,
    admin: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, IndustrialSurfaceBorder)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = feature, color = TextPrimary, fontSize = 11.sp, modifier = Modifier.weight(1f))
        PermissionCheckIcon(operator, Modifier.width(44.dp))
        PermissionCheckIcon(engineer, Modifier.width(52.dp))
        PermissionCheckIcon(admin, Modifier.width(44.dp))
    }
}

@Composable
private fun PermissionCheckIcon(allowed: Boolean, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (allowed) {
            Icon(Icons.Default.Check, contentDescription = "허용", tint = StatusRunning, modifier = Modifier.size(16.dp))
        } else {
            Icon(Icons.Default.Close, contentDescription = "불가", tint = TextMuted, modifier = Modifier.size(14.dp))
        }
    }
}
