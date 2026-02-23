package com.example.airich.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.airich.data.HealthyTaskEntry
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface HealthyTaskEntryDao {
    @Query("SELECT * FROM healthy_task_entries ORDER BY date DESC, id DESC")
    fun getAllHealthyTaskEntries(): Flow<List<HealthyTaskEntry>>
    
    @Query("SELECT * FROM healthy_task_entries WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getHealthyTaskEntriesForDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<HealthyTaskEntry>>
    
    @Query("SELECT * FROM healthy_task_entries WHERE date = :date ORDER BY id DESC")
    fun getHealthyTaskEntriesForDate(date: LocalDate): Flow<List<HealthyTaskEntry>>
    
    @Query("SELECT * FROM healthy_task_entries WHERE isCompleted = 0 ORDER BY date DESC")
    fun getActiveHealthyTaskEntries(): Flow<List<HealthyTaskEntry>>
    
    @Query("SELECT * FROM healthy_task_entries WHERE isCompleted = 1 ORDER BY date DESC")
    fun getCompletedHealthyTaskEntries(): Flow<List<HealthyTaskEntry>>
    
    @Query("SELECT * FROM healthy_task_entries ORDER BY date DESC LIMIT :limit")
    fun getRecentHealthyTaskEntries(limit: Int): Flow<List<HealthyTaskEntry>>
    
    @Insert
    suspend fun insertHealthyTaskEntry(healthyTaskEntry: HealthyTaskEntry): Long
    
    @Update
    suspend fun updateHealthyTaskEntry(healthyTaskEntry: HealthyTaskEntry)
    
    @Query("DELETE FROM healthy_task_entries WHERE id = :id")
    suspend fun deleteHealthyTaskEntry(id: Long)
    
    @Query("UPDATE healthy_task_entries SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateCompletionStatus(id: Long, isCompleted: Boolean)
}

