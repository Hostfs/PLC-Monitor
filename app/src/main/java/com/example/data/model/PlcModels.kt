package com.example.data.model

import java.util.UUID

enum class MachineStatus(val displayName: String, val colorHex: Long) {
    RUNNING("정상 가동", 0xFF10B981),
    WARNING("주의 경보", 0xFFF59E0B),
    ALARM("이상 정지", 0xFFEF4444),
    MAINTENANCE("유지 점검", 0xFF0284C7),
    OFFLINE("통신 단절", 0xFF64748B)
}

enum class AlarmSeverity(val displayName: String, val colorHex: Long) {
    CRITICAL("위험 (Critical)", 0xFFEF4444),
    WARNING("경고 (Warning)", 0xFFF59E0B),
    INFO("정보 (Info)", 0xFF0284C7)
}

data class TelemetryPoint(
    val timestamp: Long = System.currentTimeMillis(),
    val rpm: Float,
    val temperature: Float,
    val pressure: Float,
    val flowRate: Float,
    val voltage: Float,
    val current: Float,
    val frequency: Float,
    val powerKw: Float
)

data class PlcRegister(
    val address: Int,
    val name: String,
    val type: String, // "HOLDING", "COIL", "INPUT"
    val value: String,
    val unit: String,
    val isWritable: Boolean
)

data class PlcMachine(
    val id: String,
    val name: String,
    val lineName: String,
    val ipAddress: String,
    val port: Int,
    val status: MachineStatus,
    val telemetry: TelemetryPoint,
    val targetRpm: Float = 1450f,
    val targetTemp: Float = 65f,
    val targetPressure: Float = 6.0f,
    val emergencyStopEngaged: Boolean = false,
    val interlockSafe: Boolean = true,
    val uptimeSeconds: Long = 34560,
    val registers: List<PlcRegister> = emptyList()
)

data class AlarmItem(
    val id: String = UUID.randomUUID().toString(),
    val machineId: String,
    val machineName: String,
    val code: String,
    val message: String,
    val severity: AlarmSeverity,
    val timestamp: Long = System.currentTimeMillis(),
    val isAcknowledged: Boolean = false,
    val acknowledgedBy: String? = null,
    val acknowledgedAt: Long? = null
)
