package com.example.airich.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.airich.data.SleepEntry
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface SleepEntryDao {
    @Query("SELECT * FROM sleep_entries ORDER BY date DESC")
    fun getAllSleepEntries(): Flow<List<SleepEntry>>

    @Query("SELECT * FROM sleep_entries WHERE date = :date LIMIT 1")
    suspend fun getSleepEntryForDate(date: LocalDate): SleepEntry?

    @Query("SELECT * FROM sleep_entries ORDER BY date DESC LIMIT :limit")
    fun getRecentSleepEntries(limit: Int): Flow<List<SleepEntry>>

    @Query("SELECT * FROM sleep_entries WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getSleepEntriesForDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<SleepEntry>>

    @Insert
    suspend fun insertSleepEntry(entry: SleepEntry): Long

    @Query("DELETE FROM sleep_entries WHERE id = :id")
    suspend fun deleteSleepEntry(id: Long)

    @Query("UPDATE sleep_entries SET bedTime = :bedTime, wakeTime = :wakeTime, quality = :quality, note = :note WHERE id = :id")
    suspend fun updateSleepEntry(id: Long, bedTime: String, wakeTime: String, quality: Int, note: String?)
}
