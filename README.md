# 🏭 PLC Monitor - 스마트 산업설비 실시간 관제 시스템

안드로이드 기반의 산업용 PLC 및 자동화 생산 설비 실시간 원격 모니터링, 텔레메트리 시각화, 알림, 사용자 권한 제어, 데이터 로깅 및 무중단 이중화 통신 디버깅 애플리케이션입니다.

---

## 📱 대시보드 화면

<div align="center">
  <img src="image.png" alt="PLC Monitor Dashboard" width="360" style="border-radius: 12px; box-shadow: 0 4px 16px rgba(0,0,0,0.4);" />
  <p><em>▲ PLC Monitor 메인 관제 대시보드 (실시간 RTT 지연시간, OEE 가동률, 이상 경보 배너 및 장비별 텔레메트리 게이지)</em></p>
</div>

---

## 🌟 핵심 기능 및 상세 스펙

### 1. 실시간 대시보드 및 설비 관제 (Dashboard)
- **종합 설비 효율 (OEE)**: 가동 라인 수, 전체 가동률(94.8%) 및 설비별 상태(정상 가동 / 경보 주의 / 이상 정지 / 통신 단절) 한눈에 파악.
- **실시간 왕복 지연시간 (RTT)**: 상단 인디케이터에서 현재 통신 지연(`16ms`) 및 이중화 연결 상태를 즉각 표시.
- **미확인 이상 경보 배너**: 기준치(예: 90°C) 초과 시 고휘도 붉은색 경보 배너 자동 노출 및 즉각적인 `알람 확인 (ACK)` 원클릭 지원.
- **설비 카드별 실시간 텔레메트리**:
  - **PLC-101 (CNC 고속 밀링 머신 1호기)**: 온도 커스텀 게이지(`67.5°C`), 주축 회전속도(`1441 RPM`), 유압/절삭유 압력(`6.1 Bar`), 소비 전력(`21.7 kW`), 공급 전압(`380.9 V`).
  - **PLC-102 (유압 프레스 성형 2호기)**: 유압 램 속도(`979 RPM`), 작동유 온도(`71.9°C`), 메인 압력 모니터링.
  - **PLC-103 (자동 고속 패키징 소터 3호기)**: 컨베이어 속도 및 베어링 온도 경고 모니터링.
  - **PLC-104 (산업용 열교환 보일러 4호기)**: 스팀 압력 및 코어 고온 임계치 감지.

---

### 2. 고신뢰성 무중단 통신 아키텍처 및 디버거 (Comm Diagnostics)
산업 현장에서 통신 단절이나 패킷 유실로 인한 다운타임을 방지하기 위한 5단계 예외 처리 구조가 적용되어 있습니다.

1. **이중화 서버 자동 절체 (Zero-Loss Auto-Failover)**:
   - 주 서버(`Primary: 192.168.1.100:502`)와 예비 서버(`Secondary: 192.168.1.101:502`)를 상시 핑 모니터링.
   - 연속 3회 이상 응답 타임아웃 발생 시 데이터 누락 없이 예비 서버로 무중단 자동 절체.
2. **오프라인 원형 버퍼 (Store-and-Forward Circular Buffer)**:
   - 선로 단선 및 네트워크 단절 시 제어 명령과 센서 데이터를 로컬 링 버퍼(최대 200건)에 안전하게 보관.
   - 선로 복구 시 누적된 패킷을 일괄 전송(Flush)하여 데이터 무결성 보장.
3. **Keep-Alive 하트비트 및 지수 백오프**:
   - 지터(Jitter), 왕복 지연시간(Latency), 패킷 손실률(Packet Loss %)을 실시간 계산하여 통신 신뢰도 헬스 스코어(0~100%) 산출.
4. **실시간 패킷 인스펙터 (Live Packet Terminal)**:
   - Modbus TCP / OPC UA / Industrial MQTT 프로토콜 프레임의 TX/RX 16진수(Hex Dump), 함수 코드(Function Code), 파싱된 레지스터 데이터 및 통신 상태를 실시간 터미널로 확인.
5. **통신 장애 시뮬레이터 (Fault Injection Rig)**:
   - 스위치 조작 한 번으로 네트워크 타임아웃을 강제 유발하여 자동 페일오버 및 복구 과정을 테스트 가능.

---

### 3. 실시간 센서 데이터 시각화 (Telemetry Trend Charts)
- **부드러운 베지어 곡선 및 면적 그라디언트**:
  - 주축 속도(RPM), 설비 온도(°C), 유압(Bar), 냉각수 유량(L/min), 소비 전력(kW)을 실시간 캔버스 차트로 렌더링.
- **안전 임계 가이드라인**:
  - 주의 경보 점선(Warning Limit) 및 비상 정지 한계선(Critical Limit)을 표시.
- **실시간 통계 분석 패널**:
  - 최대 피크(MAX), 최저점(MIN), 평균값(MEAN), 표준편차(σ) 실시간 계산.

---

### 4. 안전 인터록 및 비상 정지 (Safety Emergency Stop)
- 설비 상세 화면에서 직관적인 **산업용 머쉬룸 E-STOP 버튼** 제공.
- 비상 정지 발령 시 즉시 인터록 차단, 주축 모터 정지 및 비상 경보 자동 트리거.
- 해제 시 2차 안전 확인 팝업을 통해 작업자 안전 확보 후 복구.

---

### 5. 3단계 사용자 권한 관리 (Role-Based Access Control)
보안과 조작 실수를 방지하기 위해 4자리 PIN 패드 인증 시스템이 탑재되어 있습니다.

| 권한 등급 | 직무 명칭 | 기본 PIN | 허용 권한 |
| :--- | :--- | :--- | :--- |
| **OPERATOR** | 운영자 (김운영) | `1111` | 실시간 모니터링, 알람 확인(ACK) 처리, 데이터 로그 열람 |
| **ENGINEER** | 엔지니어 (박엔지니어) | `2222` | 목표 설정값(RPM, 압력) 튜닝, 비상 정지(E-STOP) 제어, I/O 테스트 |
| **ADMIN** | 최고 관리자 (최관리자) | `9999` | 통신 IP/포트 설정, 서버 수동 절체, 로그 영구 삭제, 전체 계정 관리 |

---

### 6. 로컬 데이터베이스 로깅 및 CSV 내보내기 (Room DB)
- **영구 저장 엔티티**:
  - 센서 텔레메트리 로그 (`TelemetryEntity`)
  - 설비 알람 발생 및 조치 이력 (`AlarmEntity`)
  - 통신 패킷 송수신 로그 (`CommLogEntity`)
- **검색 및 CSV 내보내기**:
  - 장비 ID, 명칭, 상태 키워드 검색 지원.
  - 외부 분석 및 보고서 작성을 위한 원클릭 CSV 파일 생성 및 클립보드 복사 지원.

---

## 🛠 기술 스택

- **UI Framework**: Android Jetpack Compose (Kotlin)
- **Design System**: Material Design 3 (M3) Cyber Industrial Dark Theme
- **Local Database**: Android Jetpack Room (KSP SQLite)
- **Architecture**: MVVM (Model-View-ViewModel) + Clean Architecture Repository Pattern
- **Async & Reactive**: Kotlin Coroutines & StateFlow / SharedFlow
- **Communication Protocol**: Modbus TCP/IP, OPC UA, Industrial MQTT Gateway Client
- **Testing**: Robolectric Local JVM Testing, JUnit 4

---

## 🚀 빠른 시작 가이드

1. **설비 모니터링**: 앱 실행 시 메인 `대시보드`에서 실시간 가동 현황 및 게이지를 확인합니다.
2. **알람 확인**: 미확인 경보 발생 시 `알람 확인 (ACK)` 버튼을 누르거나 `알람` 탭에서 일괄 확인합니다.
3. **설정값 튜닝**: `장비 제어` 탭에서 목표 RPM/압력 슬라이더를 조정합니다 (엔지니어 PIN `2222` 필요).
4. **통신 장애 테스트**: `통신 디버거` 탭에서 `통신 예외 및 무중단 장애 복구 시험` 스위치를 켜서 자동 페일오버를 검증합니다.
