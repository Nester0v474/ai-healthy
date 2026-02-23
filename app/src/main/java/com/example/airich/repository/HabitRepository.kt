package com.example.airich.repository

import com.example.airich.data.FoodDatabase
import com.example.airich.data.Habit
import com.example.airich.data.HabitCompletion
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class HabitRepository(private val database: FoodDatabase) {

    fun getAllHabits(): Flow<List<Habit>> = database.habitDao().getAllHabits()

    suspend fun addHabit(name: String): Long {
        val habit = Habit(name = name.trim())
        return database.habitDao().insertHabit(habit)
    }

    suspend fun deleteHabit(id: Long) {
        database.habitCompletionDao().deleteCompletionsForHabit(id)
        database.habitDao().deleteHabit(id)
    }

    suspend fun toggleCompletion(habitId: Long, date: LocalDate) {
        val existing = database.habitCompletionDao().getCompletion(habitId, date)
        if (existing != null) {
            database.habitCompletionDao().deleteCompletion(habitId, date)
        } else {
            database.habitCompletionDao().insertCompletion(
                HabitCompletion(habitId = habitId, date = date)
            )
        }
    }

    fun getCompletionsForDate(date: LocalDate): Flow<List<HabitCompletion>> =
        database.habitCompletionDao().getCompletionsForDate(date)

    suspend fun isCompleted(habitId: Long, date: LocalDate): Boolean =
        database.habitCompletionDao().getCompletion(habitId, date) != null
}
