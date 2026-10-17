package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "telemetry_logs")
data class TelemetryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val machineId: String,
    val machineName: String,
    val timestamp: Long,
    val rpm: Float,
    val temperature: Float,
    val pressure: Float,
    val flowRate: Float,
    val voltage: Float,
    val current: Float,
    val powerKw: Float,
    val status: String
)

@Entity(tableName = "alarm_logs")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val alarmId: String,
    val machineId: String,
    val machineName: String,
    val code: String,
    val message: String,
    val severity: String,
    val timestamp: Long,
    val isAcknowledged: Boolean,
    val acknowledgedBy: String?,
    val acknowledgedAt: Long?
)

@Entity(tableName = "comm_logs")
data class CommLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val direction: String,
    val protocol: String,
    val endpoint: String,
    val hexDump: String,
    val description: String,
    val status: String,
    val latencyMs: Long
)
