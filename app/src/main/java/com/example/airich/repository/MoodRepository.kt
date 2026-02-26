package com.example.airich.repository

import com.example.airich.data.FoodDatabase
import com.example.airich.data.MoodEntry
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class MoodRepository(private val database: FoodDatabase) {

    fun getAllMoodEntries(): Flow<List<MoodEntry>> {
        return database.moodEntryDao().getAllMoodEntries()
    }

    fun getMoodEntriesForLast7Days(): Flow<List<MoodEntry>> {
        val today = LocalDate.now()
        val startDate = today.minusDays(6)
        return database.moodEntryDao().getMoodEntriesForDateRange(startDate, today)
    }

    fun getRecentMoodEntries(limit: Int = 10): Flow<List<MoodEntry>> {
        return database.moodEntryDao().getRecentMoodEntries(limit)
    }

    suspend fun getMoodEntryForToday(): MoodEntry? {
        val today = LocalDate.now()
        return database.moodEntryDao().getMoodEntryForDate(today)
    }

    suspend fun insertMoodEntry(moodEntry: MoodEntry): Long {

        val existing = database.moodEntryDao().getMoodEntryForDate(moodEntry.date)
        return if (existing != null) {

            database.moodEntryDao().updateMoodEntry(existing.id, moodEntry.moodScore, moodEntry.note)
            existing.id
        } else {

            database.moodEntryDao().insertMoodEntry(moodEntry)
        }
    }

    suspend fun deleteMoodEntry(id: Long) {
        database.moodEntryDao().deleteMoodEntry(id)
    }
}
