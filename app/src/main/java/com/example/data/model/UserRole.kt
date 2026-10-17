package com.example.data.model

enum class UserRole(
    val title: String,
    val description: String,
    val canAcknowledgeAlarms: Boolean,
    val canEditSetpoints: Boolean,
    val canControlEStop: Boolean,
    val canModifyCommConfig: Boolean,
    val canExportLogs: Boolean,
    val canManageUsers: Boolean
) {
    OPERATOR(
        title = "Operator (운영자)",
        description = "실시간 모니터링 및 알람 확인 처리 권한",
        canAcknowledgeAlarms = true,
        canEditSetpoints = false,
        canControlEStop = false,
        canModifyCommConfig = false,
        canExportLogs = true,
        canManageUsers = false
    ),
    ENGINEER(
        title = "Engineer (엔지니어)",
        description = "장비 설정값 제어, I/O 테스트 및 알람 리셋 권한",
        canAcknowledgeAlarms = true,
        canEditSetpoints = true,
        canControlEStop = true,
        canModifyCommConfig = false,
        canExportLogs = true,
        canManageUsers = false
    ),
    ADMIN(
        title = "Administrator (관리자)",
        description = "통신 프로토콜 설정, 전체 시스템 제어 및 계정 관리 최고 권한",
        canAcknowledgeAlarms = true,
        canEditSetpoints = true,
        canControlEStop = true,
        canModifyCommConfig = true,
        canExportLogs = true,
        canManageUsers = true
    )
}

data class UserAccount(
    val id: String,
    val username: String,
    val role: UserRole,
    val pinCode: String,
    val department: String
)
