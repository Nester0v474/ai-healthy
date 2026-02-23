package com.example.airich.repository

import com.example.airich.data.FoodDatabase
import com.example.airich.data.SleepEntry
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class SleepRepository(private val database: FoodDatabase) {

    fun getAllSleepEntries(): Flow<List<SleepEntry>> = database.sleepEntryDao().getAllSleepEntries()

    fun getRecentSleepEntries(limit: Int = 14): Flow<List<SleepEntry>> =
        database.sleepEntryDao().getRecentSleepEntries(limit)

    suspend fun getSleepEntryForDate(date: LocalDate): SleepEntry? =
        database.sleepEntryDao().getSleepEntryForDate(date)

    suspend fun insertOrUpdateSleepEntry(entry: SleepEntry): Long {
        val existing = database.sleepEntryDao().getSleepEntryForDate(entry.date)
        return if (existing != null) {
            database.sleepEntryDao().updateSleepEntry(
                existing.id, entry.bedTime, entry.wakeTime, entry.quality, entry.note
            )
            existing.id
        } else {
            database.sleepEntryDao().insertSleepEntry(entry)
        }
    }

    suspend fun deleteSleepEntry(id: Long) = database.sleepEntryDao().deleteSleepEntry(id)
}
