package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.local.PlcDatabase
import com.example.data.model.AlarmItem
import com.example.data.model.CommDiagnostics
import com.example.data.model.CommPacket
import com.example.data.model.PlcMachine
import com.example.data.model.ProtocolType
import com.example.data.model.UserAccount
import com.example.data.model.UserRole
import com.example.data.repository.PlcRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen(val title: String) {
    DASHBOARD("대시보드"),
    TELEMETRY("실시간 차트"),
    MACHINES("장비 제어"),
    ALARMS("알람 현황"),
    LOGS("데이터 로그"),
    DEBUGGER("통신 디버거"),
    USERS("권한 관리")
}

data class PinAuthPrompt(
    val title: String,
    val requiredRole: UserRole,
    val onAuthorized: () -> Unit
)

class PlcViewModel(application: Application) : AndroidViewModel(application) {

    private val database: PlcDatabase = Room.databaseBuilder(
        application,
        PlcDatabase::class.java,
        "plc_monitor.db"
    ).fallbackToDestructiveMigration().build()

    val repository = PlcRepository(database, viewModelScope)

    private val _currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _selectedMachineId = MutableStateFlow<String?>("PLC-101")
    val selectedMachineId: StateFlow<String?> = _selectedMachineId.asStateFlow()

    // PIN Authentication Dialog state
    private val _pinAuthPrompt = MutableStateFlow<PinAuthPrompt?>(null)
    val pinAuthPrompt: StateFlow<PinAuthPrompt?> = _pinAuthPrompt.asStateFlow()

    // Toast/Snackbar notifications
    private val _toastMessage = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    // Log & Alarm Filters
    private val _logSearchQuery = MutableStateFlow("")
    val logSearchQuery: StateFlow<String> = _logSearchQuery.asStateFlow()

    private val _alarmFilterSeverity = MutableStateFlow<String?>("ALL")
    val alarmFilterSeverity: StateFlow<String?> = _alarmFilterSeverity.asStateFlow()

    // Telemetry trend metric selector: "RPM", "TEMP", "PRESSURE", "FLOW", "POWER"
    private val _selectedMetric = MutableStateFlow("TEMP")
    val selectedMetric: StateFlow<String> = _selectedMetric.asStateFlow()

    // Repository flows
    val machines: StateFlow<List<PlcMachine>> = repository.machines
    val currentUser: StateFlow<UserAccount> = repository.currentUser
    val accounts: StateFlow<List<UserAccount>> = repository.accounts
    val activeAlarms: StateFlow<List<AlarmItem>> = repository.activeAlarms
    val commDiagnostics: StateFlow<CommDiagnostics> = repository.commEngine.diagnostics
    val livePackets: StateFlow<List<CommPacket>> = repository.livePackets

    val recentTelemetryLogs = repository.getRecentTelemetryLogs(100)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val alarmHistory = repository.getAllAlarmHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun selectMachine(machineId: String) {
        _selectedMachineId.value = machineId
    }

    fun setSelectedMetric(metric: String) {
        _selectedMetric.value = metric
    }

    fun setLogSearchQuery(query: String) {
        _logSearchQuery.value = query
    }

    fun setAlarmFilterSeverity(severity: String?) {
        _alarmFilterSeverity.value = severity
    }

    fun requestPinAuth(title: String, requiredRole: UserRole, action: () -> Unit) {
        val user = currentUser.value
        // If current user already satisfies the role, execute directly
        val hasPermission = when (requiredRole) {
            UserRole.OPERATOR -> true
            UserRole.ENGINEER -> user.role == UserRole.ENGINEER || user.role == UserRole.ADMIN
            UserRole.ADMIN -> user.role == UserRole.ADMIN
        }

        if (hasPermission) {
            action()
        } else {
            _pinAuthPrompt.value = PinAuthPrompt(title, requiredRole, action)
        }
    }

    fun submitPinAuth(pin: String): Boolean {
        val prompt = _pinAuthPrompt.value ?: return false
        val accounts = accounts.value
        val matchedUser = accounts.find { it.pinCode == pin }

        if (matchedUser == null) {
            _toastMessage.tryEmit("PIN 코드가 올바르지 않습니다.")
            return false
        }

        val hasPermission = when (prompt.requiredRole) {
            UserRole.OPERATOR -> true
            UserRole.ENGINEER -> matchedUser.role == UserRole.ENGINEER || matchedUser.role == UserRole.ADMIN
            UserRole.ADMIN -> matchedUser.role == UserRole.ADMIN
        }

        if (hasPermission) {
            repository.switchUserDirectly(matchedUser)
            prompt.onAuthorized()
            _pinAuthPrompt.value = null
            _toastMessage.tryEmit("${matchedUser.username} 인증 완료")
            return true
        } else {
            _toastMessage.tryEmit("해당 작업에 필요한 권한(${prompt.requiredRole.title})이 부족합니다.")
            return false
        }
    }

    fun dismissPinAuth() {
        _pinAuthPrompt.value = null
    }

    fun switchUserByPin(pin: String): Boolean {
        val success = repository.authenticateAndSwitchUser(pin)
        if (success) {
            _toastMessage.tryEmit("${currentUser.value.username}으로 계정 전환 완료")
        } else {
            _toastMessage.tryEmit("잘못된 PIN 번호입니다.")
        }
        return success
    }

    fun switchUserDirectly(account: UserAccount) {
        repository.switchUserDirectly(account)
        _toastMessage.tryEmit("${account.username} 계정으로 전환되었습니다.")
    }

    fun acknowledgeAlarm(alarmId: String) {
        repository.acknowledgeAlarm(alarmId)
        _toastMessage.tryEmit("알람 확인 처리 완료")
    }

    fun acknowledgeAllAlarms() {
        repository.acknowledgeAllAlarms()
        _toastMessage.tryEmit("모든 경보 일괄 확인 완료")
    }

    fun clearAcknowledgedAlarms() {
        repository.clearAcknowledgedAlarms()
        _toastMessage.tryEmit("확인된 알람 목록 정리 완료")
    }

    fun toggleEmergencyStop(machineId: String) {
        requestPinAuth("비상 정지(E-STOP) 전환", UserRole.ENGINEER) {
            viewModelScope.launch {
                val res = repository.toggleEmergencyStop(machineId)
                res.onSuccess { engaged ->
                    val msg = if (engaged) "⚠️ 긴급 정지(E-STOP)가 작동되었습니다!" else "✅ 비상 정지가 정상 해제되었습니다."
                    _toastMessage.tryEmit(msg)
                }.onFailure { err ->
                    _toastMessage.tryEmit("오류: ${err.message}")
                }
            }
        }
    }

    fun updateTargetRpm(machineId: String, newRpm: Float) {
        requestPinAuth("주축 속도(RPM) 변경", UserRole.ENGINEER) {
            viewModelScope.launch {
                val res = repository.setTargetRpm(machineId, newRpm)
                res.onSuccess {
                    _toastMessage.tryEmit("목표 RPM이 ${newRpm.toInt()}으로 변경되었습니다.")
                }.onFailure {
                    _toastMessage.tryEmit("변경 실패: ${it.message}")
                }
            }
        }
    }

    fun updateTargetPressure(machineId: String, newPressure: Float) {
        requestPinAuth("유압 설정 압력 변경", UserRole.ENGINEER) {
            viewModelScope.launch {
                val res = repository.setTargetPressure(machineId, newPressure)
                res.onSuccess {
                    _toastMessage.tryEmit("목표 압력이 ${newPressure}Bar로 변경되었습니다.")
                }.onFailure {
                    _toastMessage.tryEmit("변경 실패: ${it.message}")
                }
            }
        }
    }

    fun toggleFaultSimulation(enabled: Boolean) {
        repository.commEngine.toggleFaultSimulation(enabled)
        val msg = if (enabled) "🚨 통신 장애 시뮬레이션 활성화 (타임아웃 및 자동 Failover 테스트 중)"
        else "🟢 정상 통신 복구 및 Primary 서버 재연결"
        _toastMessage.tryEmit(msg)
    }

    fun switchServerManual(toSecondary: Boolean) {
        repository.commEngine.switchServerManual(toSecondary)
        val target = if (toSecondary) "Secondary (예비 서버)" else "Primary (주 서버)"
        _toastMessage.tryEmit("통신 엔드포인트를 $target 로 수동 전환했습니다.")
    }

    fun updateCommConfig(
        primaryHost: String,
        primaryPort: Int,
        secondaryHost: String,
        secondaryPort: Int,
        protocol: ProtocolType,
        socketTimeout: Int,
        keepAliveSec: Int
    ) {
        requestPinAuth("통신 인프라 설정 수정", UserRole.ADMIN) {
            repository.commEngine.updateCommConfig(
                primaryHost, primaryPort, secondaryHost, secondaryPort,
                protocol, socketTimeout, keepAliveSec
            )
            _toastMessage.tryEmit("통신 네트워크 설정이 저장되었습니다.")
        }
    }

    fun exportCsvLogs(onCsvReady: (String) -> Unit) {
        viewModelScope.launch {
            val csv = repository.exportLogsAsCsv()
            onCsvReady(csv)
            _toastMessage.tryEmit("데이터 로그 CSV 생성 완료 (${csv.lines().size - 1}건)")
        }
    }

    fun clearAllLogs() {
        requestPinAuth("전체 로그 데이터 삭제", UserRole.ADMIN) {
            viewModelScope.launch {
                repository.clearOldLogs()
                _toastMessage.tryEmit("전체 텔레메트리 및 통신 로그가 정리되었습니다.")
            }
        }
    }
}
