package com.example.data.repository

import com.example.data.local.AlarmEntity
import com.example.data.local.CommLogEntity
import com.example.data.local.PlcDatabase
import com.example.data.local.TelemetryEntity
import com.example.data.model.AlarmItem
import com.example.data.model.AlarmSeverity
import com.example.data.model.CommDiagnostics
import com.example.data.model.CommPacket
import com.example.data.model.MachineStatus
import com.example.data.model.PlcMachine
import com.example.data.model.PlcRegister
import com.example.data.model.TelemetryPoint
import com.example.data.model.UserAccount
import com.example.data.model.UserRole
import com.example.data.network.PlcCommEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.max
import kotlin.math.sin
import kotlin.random.Random

class PlcRepository(
    private val database: PlcDatabase,
    private val scope: CoroutineScope
) {
    val commEngine = PlcCommEngine(scope)

    // Current active user account
    private val _currentUser = MutableStateFlow(
        UserAccount(
            id = "op01",
            username = "김운영 (Operator)",
            role = UserRole.OPERATOR,
            pinCode = "1111",
            department = "생산 1팀"
        )
    )
    val currentUser: StateFlow<UserAccount> = _currentUser.asStateFlow()

    private val allAccounts = mutableListOf(
        UserAccount("op01", "김운영 (Operator)", UserRole.OPERATOR, "1111", "생산 1팀"),
        UserAccount("eng01", "박엔지니어 (Engineer)", UserRole.ENGINEER, "2222", "설비 기술팀"),
        UserAccount("adm01", "최관리자 (Admin)", UserRole.ADMIN, "9999", "스마트팩토리 운영본부")
    )
    private val _accountsState = MutableStateFlow<List<UserAccount>>(allAccounts.toList())
    val accounts: StateFlow<List<UserAccount>> = _accountsState.asStateFlow()

    // Machines
    private val _machines = MutableStateFlow<List<PlcMachine>>(emptyList())
    val machines: StateFlow<List<PlcMachine>> = _machines.asStateFlow()

    // Active alarms (in-memory + synced with Room)
    private val _activeAlarms = MutableStateFlow<List<AlarmItem>>(emptyList())
    val activeAlarms: StateFlow<List<AlarmItem>> = _activeAlarms.asStateFlow()

    // In-memory packet stream history for real-time debugger
    private val _livePackets = MutableStateFlow<List<CommPacket>>(emptyList())
    val livePackets: StateFlow<List<CommPacket>> = _livePackets.asStateFlow()

    private var simulationJob: Job? = null
    private var telemetryLogCounter = 0

    init {
        initDefaultMachines()
        startTelemetrySimulation()
        observeCommPackets()
    }

    private fun initDefaultMachines() {
        val initialList = listOf(
            PlcMachine(
                id = "PLC-101",
                name = "CNC 고속 밀링 머신 1호기",
                lineName = "제1가공라인",
                ipAddress = "192.168.1.101",
                port = 502,
                status = MachineStatus.RUNNING,
                telemetry = TelemetryPoint(
                    rpm = 1450f,
                    temperature = 68.4f,
                    pressure = 6.2f,
                    flowRate = 42.5f,
                    voltage = 380.2f,
                    current = 24.8f,
                    frequency = 60.0f,
                    powerKw = 16.3f
                ),
                targetRpm = 1450f,
                targetTemp = 68.0f,
                targetPressure = 6.2f,
                registers = listOf(
                    PlcRegister(40001, "주축 속도 (Spindle RPM)", "HOLDING", "1450", "RPM", true),
                    PlcRegister(40002, "절삭유 압력 (Coolant Bar)", "HOLDING", "6.2", "Bar", true),
                    PlcRegister(40003, "모터 권선 온도 (Temp)", "INPUT", "68.4", "°C", false),
                    PlcRegister(1, "인터록 안전 커버 (Interlock)", "COIL", "ON", "", true),
                    PlcRegister(2, "비상 정지 (E-STOP)", "COIL", "OFF", "", true)
                )
            ),
            PlcMachine(
                id = "PLC-102",
                name = "유압 프레스 성형 2호기",
                lineName = "제2프레스라인",
                ipAddress = "192.168.1.102",
                port = 502,
                status = MachineStatus.RUNNING,
                telemetry = TelemetryPoint(
                    rpm = 980f,
                    temperature = 74.2f,
                    pressure = 11.5f,
                    flowRate = 58.0f,
                    voltage = 381.0f,
                    current = 32.1f,
                    frequency = 59.9f,
                    powerKw = 21.2f
                ),
                targetRpm = 980f,
                targetTemp = 72.0f,
                targetPressure = 11.0f,
                registers = listOf(
                    PlcRegister(40010, "유압 메인 압력 (Pressure)", "HOLDING", "11.5", "Bar", true),
                    PlcRegister(40011, "작동유 온도 (Oil Temp)", "INPUT", "74.2", "°C", false),
                    PlcRegister(40012, "가압 램 속도 (Ram Speed)", "HOLDING", "980", "mm/s", true),
                    PlcRegister(10, "광전자 안전 센서 (Light Curtain)", "COIL", "SAFE", "", false)
                )
            ),
            PlcMachine(
                id = "PLC-103",
                name = "자동 고속 패키징 소터 3호기",
                lineName = "포장/출하 라인",
                ipAddress = "192.168.1.103",
                port = 502,
                status = MachineStatus.WARNING,
                telemetry = TelemetryPoint(
                    rpm = 1820f,
                    temperature = 84.1f,
                    pressure = 4.8f,
                    flowRate = 22.4f,
                    voltage = 379.5f,
                    current = 19.4f,
                    frequency = 60.1f,
                    powerKw = 12.8f
                ),
                targetRpm = 1750f,
                targetTemp = 75.0f,
                targetPressure = 5.0f,
                registers = listOf(
                    PlcRegister(40020, "컨베이어 이송 속도", "HOLDING", "1820", "RPM", true),
                    PlcRegister(40021, "베어링 온도 경고 임계", "INPUT", "84.1", "°C", false),
                    PlcRegister(20, "재료 부족 감지 센서", "COIL", "TRIGGERED", "", false)
                )
            ),
            PlcMachine(
                id = "PLC-104",
                name = "산업용 열교환 보일러 4호기",
                lineName = "유틸리티 설비",
                ipAddress = "192.168.1.104",
                port = 502,
                status = MachineStatus.RUNNING,
                telemetry = TelemetryPoint(
                    rpm = 600f,
                    temperature = 92.0f,
                    pressure = 8.4f,
                    flowRate = 85.0f,
                    voltage = 380.0f,
                    current = 45.0f,
                    frequency = 60.0f,
                    powerKw = 29.5f
                ),
                targetRpm = 600f,
                targetTemp = 90.0f,
                targetPressure = 8.2f,
                registers = listOf(
                    PlcRegister(40030, "스팀 보일러 압력", "HOLDING", "8.4", "Bar", true),
                    PlcRegister(40031, "보일러 코어 온도", "INPUT", "92.0", "°C", false),
                    PlcRegister(40032, "급수 밸브 개폐율", "HOLDING", "78", "%", true)
                )
            )
        )
        _machines.value = initialList

        // Initial alert
        val initAlarm = AlarmItem(
            id = "ALM-INIT-01",
            machineId = "PLC-103",
            machineName = "자동 고속 패키징 소터 3호기",
            code = "WARN_TEMP_HIGH",
            message = "서보 베어링 온도 기준치(80°C) 초과 경보 (현재: 84.1°C)",
            severity = AlarmSeverity.WARNING,
            timestamp = System.currentTimeMillis() - 120_000,
            isAcknowledged = false
        )
        _activeAlarms.value = listOf(initAlarm)
    }

    private fun observeCommPackets() {
        scope.launch {
            commEngine.packetStream.collect { packet ->
                _livePackets.update { current ->
                    (listOf(packet) + current).take(120)
                }
                // Persist packet log asynchronously
                database.commLogDao().insert(
                    CommLogEntity(
                        timestamp = packet.timestamp,
                        direction = packet.direction.name,
                        protocol = packet.protocol.label,
                        endpoint = packet.endpoint,
                        hexDump = packet.hexDump,
                        description = packet.description,
                        status = packet.status.name,
                        latencyMs = packet.latencyMs
                    )
                )
            }
        }
    }

    private fun startTelemetrySimulation() {
        simulationJob?.cancel()
        simulationJob = scope.launch(Dispatchers.Default) {
            var wave = 0.0
            while (isActive) {
                delay(1000)
                wave += 0.15

                _machines.update { list ->
                    list.map { m ->
                        if (m.emergencyStopEngaged) {
                            // Machine is emergency stopped
                            val t = m.telemetry.copy(
                                rpm = max(0f, m.telemetry.rpm * 0.7f),
                                pressure = max(0f, m.telemetry.pressure * 0.85f),
                                current = 0f,
                                powerKw = 0f
                            )
                            m.copy(status = MachineStatus.ALARM, telemetry = t)
                        } else {
                            // Normal slight industrial fluctuation around targets
                            val noise = (sin(wave + m.name.hashCode()) * 2.5f).toFloat()
                            val rpmFluct = (m.targetRpm + noise * 5f + Random.nextInt(-3, 4)).coerceAtLeast(0f)
                            val tempFluct = (m.targetTemp + noise * 0.4f).coerceAtLeast(20f)
                            val pressFluct = (m.targetPressure + noise * 0.08f).coerceAtLeast(0f)
                            val flowFluct = (40f + noise * 1.5f).coerceAtLeast(0f)
                            val voltFluct = 380f + (sin(wave) * 1.2f).toFloat()
                            val currFluct = (18f + (m.targetRpm / 100f) + (pressFluct * 0.8f)).toFloat()
                            val power = (voltFluct * currFluct * 1.732f * 0.88f) / 1000f

                            val status = when {
                                tempFluct > 88f -> MachineStatus.ALARM
                                tempFluct > 80f || pressFluct > 12f -> MachineStatus.WARNING
                                else -> MachineStatus.RUNNING
                            }

                            val newTelemetry = TelemetryPoint(
                                timestamp = System.currentTimeMillis(),
                                rpm = (rpmFluct * 10).toInt() / 10f,
                                temperature = (tempFluct * 10).toInt() / 10f,
                                pressure = (pressFluct * 100).toInt() / 100f,
                                flowRate = (flowFluct * 10).toInt() / 10f,
                                voltage = (voltFluct * 10).toInt() / 10f,
                                current = (currFluct * 10).toInt() / 10f,
                                frequency = 60.0f + Random.nextInt(-1, 2) * 0.05f,
                                powerKw = (power * 10).toInt() / 10f
                            )

                            // Check auto threshold alerts
                            checkThresholdAlarm(m.id, m.name, newTelemetry)

                            m.copy(
                                status = status,
                                telemetry = newTelemetry,
                                uptimeSeconds = m.uptimeSeconds + 1
                            )
                        }
                    }
                }

                // Log to Room Database periodically (every 5 seconds)
                telemetryLogCounter++
                if (telemetryLogCounter >= 5) {
                    telemetryLogCounter = 0
                    logAllTelemetryToDb(_machines.value)
                }
            }
        }
    }

    private fun checkThresholdAlarm(machineId: String, machineName: String, t: TelemetryPoint) {
        if (t.temperature > 90f) {
            val exists = _activeAlarms.value.any { it.machineId == machineId && it.code == "ERR_OVERHEAT" && !it.isAcknowledged }
            if (!exists) {
                val newAlarm = AlarmItem(
                    id = UUID.randomUUID().toString(),
                    machineId = machineId,
                    machineName = machineName,
                    code = "ERR_OVERHEAT",
                    message = "긴급: $machineName 고온 임계치(90°C) 돌파! [현재 ${t.temperature}°C]",
                    severity = AlarmSeverity.CRITICAL,
                    timestamp = System.currentTimeMillis()
                )
                _activeAlarms.update { listOf(newAlarm) + it }
                scope.launch {
                    database.alarmDao().insert(
                        AlarmEntity(
                            alarmId = newAlarm.id,
                            machineId = newAlarm.machineId,
                            machineName = newAlarm.machineName,
                            code = newAlarm.code,
                            message = newAlarm.message,
                            severity = newAlarm.severity.name,
                            timestamp = newAlarm.timestamp,
                            isAcknowledged = false,
                            acknowledgedBy = null,
                            acknowledgedAt = null
                        )
                    )
                }
            }
        }
    }

    private suspend fun logAllTelemetryToDb(machines: List<PlcMachine>) {
        machines.forEach { m ->
            database.telemetryDao().insert(
                TelemetryEntity(
                    machineId = m.id,
                    machineName = m.name,
                    timestamp = m.telemetry.timestamp,
                    rpm = m.telemetry.rpm,
                    temperature = m.telemetry.temperature,
                    pressure = m.telemetry.pressure,
                    flowRate = m.telemetry.flowRate,
                    voltage = m.telemetry.voltage,
                    current = m.telemetry.current,
                    powerKw = m.telemetry.powerKw,
                    status = m.status.name
                )
            )
        }
    }

    // Role switching and validation
    fun authenticateAndSwitchUser(pin: String): Boolean {
        val target = allAccounts.find { it.pinCode == pin } ?: return false
        _currentUser.value = target
        return true
    }

    fun switchUserDirectly(account: UserAccount) {
        _currentUser.value = account
    }

    // Alarm management
    fun acknowledgeAlarm(alarmId: String) {
        val user = _currentUser.value
        val now = System.currentTimeMillis()
        _activeAlarms.update { list ->
            list.map {
                if (it.id == alarmId) it.copy(isAcknowledged = true, acknowledgedBy = user.username, acknowledgedAt = now)
                else it
            }
        }
        scope.launch {
            database.alarmDao().acknowledgeAlarm(alarmId, user.username, now)
        }
    }

    fun acknowledgeAllAlarms() {
        val user = _currentUser.value
        val now = System.currentTimeMillis()
        _activeAlarms.update { list ->
            list.map { it.copy(isAcknowledged = true, acknowledgedBy = user.username, acknowledgedAt = now) }
        }
        scope.launch {
            _activeAlarms.value.forEach {
                database.alarmDao().acknowledgeAlarm(it.id, user.username, now)
            }
        }
    }

    fun clearAcknowledgedAlarms() {
        _activeAlarms.update { list -> list.filterNot { it.isAcknowledged } }
    }

    // Machine Controls
    suspend fun setTargetRpm(machineId: String, newRpm: Float): Result<Boolean> {
        val user = _currentUser.value
        if (!user.role.canEditSetpoints) {
            return Result.failure(IllegalAccessException("권한 부족: 엔지니어 이상의 권한이 필요합니다."))
        }
        commEngine.sendPlcCommand(machineId, 40001, newRpm.toInt(), "목표 주축 속도 설정 -> ${newRpm}RPM")
        _machines.update { list ->
            list.map { if (it.id == machineId) it.copy(targetRpm = newRpm) else it }
        }
        return Result.success(true)
    }

    suspend fun setTargetPressure(machineId: String, newPressure: Float): Result<Boolean> {
        val user = _currentUser.value
        if (!user.role.canEditSetpoints) {
            return Result.failure(IllegalAccessException("권한 부족: 엔지니어 이상의 권한이 필요합니다."))
        }
        commEngine.sendPlcCommand(machineId, 40010, (newPressure * 10).toInt(), "유압 기준 압력 설정 -> ${newPressure}Bar")
        _machines.update { list ->
            list.map { if (it.id == machineId) it.copy(targetPressure = newPressure) else it }
        }
        return Result.success(true)
    }

    suspend fun toggleEmergencyStop(machineId: String): Result<Boolean> {
        val user = _currentUser.value
        if (!user.role.canControlEStop) {
            return Result.failure(IllegalAccessException("권한 부족: 비상 정지 제어 권한이 없습니다."))
        }
        val target = _machines.value.find { it.id == machineId } ?: return Result.failure(NoSuchElementException())
        val newEngaged = !target.emergencyStopEngaged
        val cmd = if (newEngaged) "비상 정지(E-STOP) 발령" else "비상 정지 해제 및 인터록 복구"

        commEngine.sendPlcCommand(machineId, 0x0002, if (newEngaged) 1 else 0, cmd)

        _machines.update { list ->
            list.map {
                if (it.id == machineId) {
                    it.copy(
                        emergencyStopEngaged = newEngaged,
                        status = if (newEngaged) MachineStatus.ALARM else MachineStatus.RUNNING
                    )
                } else it
            }
        }

        if (newEngaged) {
            val eStopAlarm = AlarmItem(
                id = UUID.randomUUID().toString(),
                machineId = target.id,
                machineName = target.name,
                code = "CRIT_ESTOP_ACTIVE",
                message = "수동 비상 정지(E-STOP) 활성화됨 [실행자: ${user.username}]",
                severity = AlarmSeverity.CRITICAL,
                timestamp = System.currentTimeMillis()
            )
            _activeAlarms.update { listOf(eStopAlarm) + it }
        }

        return Result.success(newEngaged)
    }

    // Room DB Flow helpers
    fun getRecentTelemetryLogs(limit: Int = 100): Flow<List<TelemetryEntity>> =
        database.telemetryDao().getRecentTelemetry(limit)

    fun getAllAlarmHistory(): Flow<List<AlarmEntity>> =
        database.alarmDao().getAllAlarms()

    suspend fun exportLogsAsCsv(): String {
        val logs = database.telemetryDao().getAllForExport()
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA)
        val sb = StringBuilder()
        sb.append("Timestamp,MachineID,MachineName,RPM,Temperature(C),Pressure(Bar),Flow(L/min),Voltage(V),Current(A),Power(kW),Status\n")
        logs.forEach { l ->
            sb.append("${sdf.format(Date(l.timestamp))},${l.machineId},${l.machineName},${l.rpm},${l.temperature},${l.pressure},${l.flowRate},${l.voltage},${l.current},${l.powerKw},${l.status}\n")
        }
        return sb.toString()
    }

    suspend fun clearOldLogs() {
        database.telemetryDao().clearAll()
        database.alarmDao().clearAll()
        database.commLogDao().clearAll()
    }
}
