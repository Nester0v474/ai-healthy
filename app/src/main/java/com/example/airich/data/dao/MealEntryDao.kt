package com.example.airich.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.airich.data.MealEntry
import com.example.airich.data.MealEntryWithFood
import kotlinx.coroutines.flow.Flow

@Dao
interface MealEntryDao {
    @Transaction
    @Query("""
        SELECT * FROM meal_entries 
        WHERE date >= :startOfDay AND date < :endOfDay 
        ORDER BY date DESC
    """)
    fun getMealEntriesForDay(startOfDay: Long, endOfDay: Long): Flow<List<MealEntry>>
    
    @Transaction
    @Query("""
        SELECT * FROM meal_entries 
        WHERE date >= :startOfDay AND date < :endOfDay 
        ORDER BY date DESC
    """)
    fun getMealEntriesWithFoodForDay(startOfDay: Long, endOfDay: Long): Flow<List<MealEntryWithFood>>
    
    @Insert
    suspend fun insertMealEntry(mealEntry: MealEntry): Long
    
    @Query("DELETE FROM meal_entries WHERE id = :id")
    suspend fun deleteMealEntry(id: Long)
}

