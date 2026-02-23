package com.example.airich.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.airich.data.MoodEntry
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface MoodEntryDao {
    @Query("SELECT * FROM mood_entries ORDER BY date DESC")
    fun getAllMoodEntries(): Flow<List<MoodEntry>>
    
    @Query("""
        SELECT * FROM mood_entries 
        WHERE date >= :startDate AND date <= :endDate 
        ORDER BY date ASC
    """)
    fun getMoodEntriesForDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<MoodEntry>>
    
    @Query("SELECT * FROM mood_entries WHERE date = :date LIMIT 1")
    suspend fun getMoodEntryForDate(date: LocalDate): MoodEntry?
    
    @Query("SELECT * FROM mood_entries ORDER BY date DESC LIMIT :limit")
    fun getRecentMoodEntries(limit: Int): Flow<List<MoodEntry>>
    
    @Insert
    suspend fun insertMoodEntry(moodEntry: MoodEntry): Long
    
    @Query("DELETE FROM mood_entries WHERE id = :id")
    suspend fun deleteMoodEntry(id: Long)
    
    @Query("UPDATE mood_entries SET moodScore = :score, note = COALESCE(:note, note) WHERE id = :id")
    suspend fun updateMoodEntry(id: Long, score: Int, note: String?)
}

