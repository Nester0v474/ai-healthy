package com.example.airich.repository

import com.example.airich.data.FoodDatabase
import com.example.airich.data.HealthyTaskEntry
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class HealthyTaskRepository(private val database: FoodDatabase) {
    
    fun getAllHealthyTaskEntries(): Flow<List<HealthyTaskEntry>> {
        return database.healthyTaskEntryDao().getAllHealthyTaskEntries()
    }
    
    fun getHealthyTaskEntriesForLast30Days(): Flow<List<HealthyTaskEntry>> {
        val today = LocalDate.now()
        val startDate = today.minusDays(29) // последние 30 дней включая сегодня
        return database.healthyTaskEntryDao().getHealthyTaskEntriesForDateRange(startDate, today)
    }
    
    fun getActiveHealthyTaskEntries(): Flow<List<HealthyTaskEntry>> {
        return database.healthyTaskEntryDao().getActiveHealthyTaskEntries()
    }
    
    fun getCompletedHealthyTaskEntries(): Flow<List<HealthyTaskEntry>> {
        return database.healthyTaskEntryDao().getCompletedHealthyTaskEntries()
    }
    
    fun getRecentHealthyTaskEntries(limit: Int = 20): Flow<List<HealthyTaskEntry>> {
        return database.healthyTaskEntryDao().getRecentHealthyTaskEntries(limit)
    }
    
    suspend fun insertHealthyTaskEntry(healthyTaskEntry: HealthyTaskEntry): Long {
        return database.healthyTaskEntryDao().insertHealthyTaskEntry(healthyTaskEntry)
    }
    
    suspend fun updateHealthyTaskEntry(healthyTaskEntry: HealthyTaskEntry) {
        database.healthyTaskEntryDao().updateHealthyTaskEntry(healthyTaskEntry)
    }
    
    suspend fun deleteHealthyTaskEntry(id: Long) {
        database.healthyTaskEntryDao().deleteHealthyTaskEntry(id)
    }
    
    suspend fun toggleCompletionStatus(id: Long, isCompleted: Boolean) {
        database.healthyTaskEntryDao().updateCompletionStatus(id, isCompleted)
    }
}

