package com.example.craftnook.data.repository

import com.example.craftnook.data.database.UsageLog
import com.example.craftnook.data.database.UsageLogDao
import kotlinx.coroutines.flow.Flow

// ── Interface ────────────────────────────────────────────────────────────────

interface IUsageLogRepository {
    /** All log entries, newest first. */
    fun getAllLogs(): Flow<List<UsageLog>>

    /** Insert a single log entry. */
    suspend fun insertLog(log: UsageLog)
}

// ── Room implementation ──────────────────────────────────────────────────────

class RoomUsageLogRepository(
    private val usageLogDao: UsageLogDao
) : IUsageLogRepository {

    override fun getAllLogs(): Flow<List<UsageLog>> =
        usageLogDao.getAllLogs()

    override suspend fun insertLog(log: UsageLog) =
        usageLogDao.insert(log)
}
