package com.example.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Dao
interface TelemetryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(telemetry: TelemetryEntity): Long

    @Query("SELECT * FROM telemetry_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentTelemetry(limit: Int = 100): Flow<List<TelemetryEntity>>

    @Query("SELECT * FROM telemetry_logs WHERE machineId = :machineId ORDER BY timestamp DESC LIMIT :limit")
    fun getTelemetryForMachine(machineId: String, limit: Int = 60): Flow<List<TelemetryEntity>>

    @Query("SELECT * FROM telemetry_logs ORDER BY timestamp DESC")
    suspend fun getAllForExport(): List<TelemetryEntity>

    @Query("DELETE FROM telemetry_logs WHERE timestamp < :cutoffTime")
    suspend fun deleteOldLogs(cutoffTime: Long): Int

    @Query("DELETE FROM telemetry_logs")
    suspend fun clearAll()
}

@Dao
interface AlarmDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(alarm: AlarmEntity): Long

    @Query("SELECT * FROM alarm_logs ORDER BY timestamp DESC")
    fun getAllAlarms(): Flow<List<AlarmEntity>>

    @Query("UPDATE alarm_logs SET isAcknowledged = 1, acknowledgedBy = :user, acknowledgedAt = :time WHERE alarmId = :alarmId")
    suspend fun acknowledgeAlarm(alarmId: String, user: String, time: Long)

    @Query("DELETE FROM alarm_logs")
    suspend fun clearAll()
}

@Dao
interface CommLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: CommLogEntity): Long

    @Query("SELECT * FROM comm_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentPackets(limit: Int = 150): Flow<List<CommLogEntity>>

    @Query("DELETE FROM comm_logs")
    suspend fun clearAll()
}

@Database(
    entities = [TelemetryEntity::class, AlarmEntity::class, CommLogEntity::class],
    version = 1,
    exportSchema = false
)
abstract class PlcDatabase : RoomDatabase() {
    abstract fun telemetryDao(): TelemetryDao
    abstract fun alarmDao(): AlarmDao
    abstract fun commLogDao(): CommLogDao
}
