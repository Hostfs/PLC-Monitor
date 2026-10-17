package com.example.data.model

import java.util.UUID

enum class ProtocolType(val label: String, val defaultPort: Int) {
    MODBUS_TCP("Modbus TCP/IP", 502),
    OPC_UA("OPC UA Binary", 4840),
    MQTT_GATEWAY("Industrial MQTT", 1883),
    REST_TELEMETRY("HTTP REST JSON", 8080)
}

enum class PacketDirection {
    TX, RX
}

enum class PacketStatus {
    SUCCESS,
    TIMEOUT,
    CRC_ERROR,
    FRAME_ERROR,
    RETRY_QUEUED,
    SERVER_FAILOVER
}

data class CommPacket(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val direction: PacketDirection,
    val protocol: ProtocolType,
    val endpoint: String,
    val hexDump: String,
    val description: String,
    val status: PacketStatus,
    val latencyMs: Long
)

data class CommDiagnostics(
    val isConnected: Boolean = true,
    val activeEndpoint: String = "192.168.1.100:502 (Primary)",
    val primaryHost: String = "192.168.1.100",
    val primaryPort: Int = 502,
    val secondaryHost: String = "192.168.1.101",
    val secondaryPort: Int = 502,
    val isFailoverActive: Boolean = false,
    val protocol: ProtocolType = ProtocolType.MODBUS_TCP,
    val latencyMs: Long = 18,
    val jitterMs: Long = 4,
    val packetLossPct: Float = 0.2f,
    val healthScore: Int = 98, // 0 - 100
    val totalPacketsTx: Long = 14205,
    val totalPacketsRx: Long = 14188,
    val retryCount: Long = 17,
    val errorCount: Long = 4,
    val bufferQueuedCount: Int = 0,
    val lastHeartbeatTs: Long = System.currentTimeMillis(),
    val socketTimeoutMs: Int = 1500,
    val maxRetries: Int = 3,
    val keepAliveIntervalSec: Int = 3,
    val simulateFaultMode: Boolean = false
)
