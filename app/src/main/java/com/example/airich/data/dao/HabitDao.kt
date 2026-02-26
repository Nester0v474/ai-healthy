package com.example.airich.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.airich.data.Habit
import com.example.airich.data.HabitCompletion
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY id ASC")
    fun getAllHabits(): Flow<List<Habit>>

    @Insert
    suspend fun insertHabit(habit: Habit): Long

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteHabit(id: Long)
}

@Dao
interface HabitCompletionDao {
    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId AND date = :date LIMIT 1")
    suspend fun getCompletion(habitId: Long, date: LocalDate): HabitCompletion?

    @Query("DELETE FROM habit_completions WHERE habitId = :habitId")
    suspend fun deleteCompletionsForHabit(habitId: Long)

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId AND date >= :startDate ORDER BY date DESC")
    fun getCompletionsForHabit(habitId: Long, startDate: LocalDate): Flow<List<HabitCompletion>>

    @Query("SELECT * FROM habit_completions WHERE date = :date")
    fun getCompletionsForDate(date: LocalDate): Flow<List<HabitCompletion>>

    @Insert
    suspend fun insertCompletion(completion: HabitCompletion): Long

    @Query("DELETE FROM habit_completions WHERE habitId = :habitId AND date = :date")
    suspend fun deleteCompletion(habitId: Long, date: LocalDate)

    @Query("DELETE FROM habit_completions WHERE id = :id")
    suspend fun deleteCompletionById(id: Long)
}
