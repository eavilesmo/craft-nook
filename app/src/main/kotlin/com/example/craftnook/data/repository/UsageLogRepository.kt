package com.example.craftnook.data.repository

import com.example.craftnook.data.database.UsageLog
import com.example.craftnook.data.database.UsageLogDao
import kotlinx.coroutines.flow.Flow

interface IUsageLogRepository {
    fun getAllLogs(): Flow<List<UsageLog>>

    suspend fun insertLog(log: UsageLog)

    suspend fun clearAllLogs()

    suspend fun deleteLogById(id: String)
}

class RoomUsageLogRepository(
    private val usageLogDao: UsageLogDao
) : IUsageLogRepository {

    override fun getAllLogs(): Flow<List<UsageLog>> =
        usageLogDao.getAllLogs()

    override suspend fun insertLog(log: UsageLog) =
        usageLogDao.insert(log)

    override suspend fun clearAllLogs() =
        usageLogDao.deleteAll()

    override suspend fun deleteLogById(id: String) =
        usageLogDao.deleteById(id)
}
